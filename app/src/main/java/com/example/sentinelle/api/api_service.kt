package com.example.sentinelle.api

import TokenManager
import android.content.Context
import android.util.Log
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

    private val token = tokenManager.getToken()

    fun login(email: String, password: String) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://192.168.1.9:8000/api/login/") // si tu utilises l'émulateur Android
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Login", "Erreur : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val token = JSONObject(responseData).getString("access")
                    // Sauvegarder le token dans SharedPreferences
                    tokenManager.saveToken(token)
                    Log.d("API", "Connexion réussie. Token : $token")
                } else {
                    Log.e("API", "Échec de connexion : ${response.code}")
                }
            }
        })
    }


    fun register(email: String, password: String) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://192.168.1.9:8000/api/register/") // si tu utilises l'émulateur Android
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Login", "Erreur : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val token = JSONObject(responseData).getString("access")
                    // Sauvegarder le token dans SharedPreferences
                    tokenManager.saveToken(token)
                    Log.d("API", "Inscription réussie. Token : $token")
                } else {
                    Log.e("API", "Échec de l'inscription : ${response.code}")
                }
            }
        })
    }

    companion object

}