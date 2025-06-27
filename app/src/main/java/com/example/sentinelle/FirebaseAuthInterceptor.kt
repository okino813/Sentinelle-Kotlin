package com.example.sentinelle

import android.content.Context
import com.example.sentinelle.api.AppValues
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

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
}

object ApiHelper {

    private fun buildClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(FirebaseAuthInterceptor())
            .build()
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
}