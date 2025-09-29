package com.example.sentinelle

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.sentinelle.api.AppValues
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class FirebaseAuthInterceptor : Interceptor {



    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {
            // Pas connecté
            throw Exception("Utilisateur non connecté")
        }

        // Obtenir un token valide
        val tokenTask = user.getIdToken(false)
        val tokenResult = Tasks.await(tokenTask)
        val token = tokenResult.token ?: throw Exception("Token Firebase invalide")

        // Ajouter le token à la requête
        val requestWithToken = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(requestWithToken)

        // Si le token a expiré, on tente de le rafraîchir et rejouer la requête
        if (response.code == 401) {
            response.close()

            // Force le rafraîchissement du token
            val refreshedToken = Tasks.await(user.getIdToken(true)).token
                ?: throw Exception("Impossible de rafraîchir le token Firebase")

            val retriedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $refreshedToken")
                .build()

            return chain.proceed(retriedRequest)
        }

        return response
    }

    public fun printFirebaseToken() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.getIdToken(false)?.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.token
                Log.d("FIREBASE_TOKEN", "Token: $token")
                // 👉 Tu peux aussi faire un Toast si tu veux le voir directement
                // Toast.makeText(context, token, Toast.LENGTH_LONG).show()
            } else {
                Log.e("FIREBASE_TOKEN", "Impossible de récupérer le token", task.exception)
            }
        })
    }
}

object ApiHelper {

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(FirebaseAuthInterceptor())
            .build()
    }

    suspend fun getToken(): String {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw Exception("Utilisateur non connecté")

        return suspendCoroutine { cont ->
            user.getIdToken(false) // false = utilise le cache si valide
                .addOnSuccessListener { result ->
                    val token = result.token
                    if (token != null) {
                        cont.resume(token)
                    } else {
                        cont.resumeWithException(Exception("Token Firebase invalide"))
                    }
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        }
    }

    fun apiGet(context: Context, endpoint: String, onSuccess: (JSONObject) -> Unit, onError: () -> Unit) {
        val request = Request.Builder()
            .url("${AppValues.base_url}/api/$endpoint/")
            .build()

        buildClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onError()

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (response.isSuccessful && body != null) {
                    try {
                        onSuccess(JSONObject(body))
                    } catch (e: JSONException) {
                        onError()
                    }
                } else {
                    onError()
                }
            }
        })
    }

    fun apiPost(
        context: Context,
        endpoint: String,
        json: JSONObject,
        onSuccess: (JSONObject) -> Unit,
        onError: () -> Unit
    ) {
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("${AppValues.base_url}/api/$endpoint/")
            .post(body)
            .build()

        buildClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onError()

            override fun onResponse(call: Call, response: Response) {
                val bodys = response.body?.string()
                if (response.isSuccessful && bodys != null) {
                    try {
                        onSuccess(JSONObject(bodys))
                    } catch (e: JSONException) {
                        onError()
                    }
                } else {
                    onError()
                }
            }
        })
    }

    fun downloadFileToDocuments(
        context: Context,
        url: String,
        trajetName: String,
        fileName: String,
        onSuccess: (Uri) -> Unit,
        onError: () -> Unit
    ) {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        buildClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful || response.body == null) {
                    onError()
                    return
                }

                try {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            "${Environment.DIRECTORY_DOCUMENTS}/Sentinelle/$trajetName/"
                        )
                    }

                    val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    if (uri == null) {
                        onError()
                        return
                    }

                    resolver.openOutputStream(uri).use { outStream ->
                        response.body!!.byteStream().copyTo(outStream!!)
                    }

                    onSuccess(uri)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onError()
                }
            }
        })
    }


    fun apiPostFile(
        context: Context,
        endpoint: String,
        file: File,
        onSuccess: (JSONObject) -> Unit,
        onError: () -> Unit
    ) {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "audio",
                file.name,
                RequestBody.create("audio/*".toMediaType(), file)
            )
            .addFormDataPart("timestamp", System.currentTimeMillis().toString())
            .build()

        val request = Request.Builder()
            .url("${AppValues.base_url}/api/$endpoint/")
            .post(requestBody)
            .build()

        buildClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) = onError()

            override fun onResponse(call: Call, response: Response) {
                val bodys = response.body?.string()
                if (response.isSuccessful && bodys != null) {
                    try {
                        onSuccess(JSONObject(bodys))
                    } catch (e: JSONException) {
                        onError()
                    }
                } else {
                    onError()
                }
            }
        })
    }
}