package com.example.sentinelle.api

import TokenManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class api_service(val context: Context) {

    private val tokenManager = TokenManager(context)
    private val token_access = tokenManager.getToken(0)

    suspend fun login(email: String, password: String): Boolean {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("${AppValues.base_url}/api/login/") // si tu utilises l'émulateur Android
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val token_access = JSONObject(responseData).getString("access")
                    val token_refresh = JSONObject(responseData).getString("refresh")
                    // Sauvegarder les tokens dans SharedPreferences
                    tokenManager.saveToken(token_access, token_refresh)
                    Log.d("API", "Connexion réussie. Token : $token_access")
                    true
                } else {
                    Log.e("API", "Échec de connexion : ${response.code}")
                    false
                }
            } catch (e: IOException) {
                Log.e("API", "Erreur : ${e.message}")
                false
            }
        }
    }

    fun register(email: String, password: String) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("${AppValues.base_url}/api/register/") // si tu utilises l'émulateur Android
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Login", "Erreur : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val token_access = JSONObject(responseData).getString("access")
                    val token_refresh = JSONObject(responseData).getString("refresh")
                    // Sauvegarder le token dans SharedPreferences
                    tokenManager.saveToken(token_access, token_refresh)
                    Log.d("API", "Inscription réussie. Token : $token_access")
                } else {
                    Log.e("API", "Échec de l'inscription : ${response.code}")
                }
            }
        })
    }
//
//    fun testToken() {
//        // Créer le client HTTP
//        Log.d("API", "Cn : $token_access")
//
//        val client = OkHttpClient()
//
//        // Construire la requête avec le token dans l'en-tête Authorization
//        val request = Request.Builder()
//            .url("${AppValues.base_url}/api/test/")
//            .addHeader("Authorization", "Bearer $token_access")  // Ajouter le token
//            .build()
//
//        // Exécuter la requête
//        client.newCall(request).enqueue(object : Callback {
////            override fun onFailure(call: Call, e: IOException) {
////                // Si la requête échoue (par exemple, pas de connexion Internet)
////                callback(false)
////            }
//
//            override fun onResponse(call: Call, response: Response) {
//                // Vérifie le code de réponse HTTP
//                if (response.code == 200) {
//                    // Token valide
//                    print("Token valide")
//
//                } else if (response.code == 401) {
//                    // Token invalide ou expiré
//                    print("Token invalide")
//
//                } else {
//                    // Autres erreurs HTTP
//                    print("Erreur")
//
//                }
//            }
//        })
//    }

    companion object

}