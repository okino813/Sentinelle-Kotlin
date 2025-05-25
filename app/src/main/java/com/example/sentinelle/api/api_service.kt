package com.example.sentinelle.api

import ApiClient
import TokenManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.sentinelle.login_activity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class api_service(val context: Context) {

    private val tokenManager = TokenManager(context)

    fun login(email: String, password: String, callback: (Boolean) -> Unit) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }
        val body = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("${AppValues.base_url}/api/login/")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Login", "Erreur : ${e.message}")
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    val json = JSONObject(responseData)
                    val token = json.getString("token")
                    tokenManager.saveToken(token)
                    Log.d("API", "Connexion réussie. Token : $token")
                    callback(true)
                } else {
                    Log.e("API", "Échec de connexion : ${response.code}")
                    callback(false)
                }
            }
        })
    }

    fun register(context: Context, email: String, password: String, callback: (Boolean) -> Unit) {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${AppValues.base_url}/api/register/")  // à adapter à ton endpoint
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Register", "Erreur : ${e.message}")
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseData = response.body?.string()
                    try {
                        val jsonResponse = JSONObject(responseData)
                        val token = jsonResponse.getString("token")

                        // Enregistre le token avec TokenManager
                        TokenManager(context).saveToken(token)

                        Log.d("Register", "Inscription réussie. Token : $token")
                        callback(true)
                    } catch (e: JSONException) {
                        Log.e("Register", "Erreur JSON : ${e.message}")
                        callback(false)
                    }
                } else {
                    Log.e("Register", "Erreur de réponse : ${response.code}")
                    callback(false)
                }
            }
        })
    }

    fun getInfo(context: Context) {
        val request = Request.Builder()
            .url("${AppValues.base_url}/api/getinfoaccount/")
            .build()

        ApiClient.getClient(context).newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("APICACA", "Erreur : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    Log.d("APICACA", "Réponse : $body")
                    try {
                        val jsonObject = JSONObject(body)
                        AppValues.firstname = jsonObject.getString("firstname")
                        AppValues.lastname = jsonObject.getString("lastname")
                        AppValues.phone = jsonObject.getString("phones")
                        AppValues.email = jsonObject.getString("email")
                        AppValues.message = jsonObject.getString("message")

                        val contactsJsonArray = jsonObject.getJSONArray("contacts")
                        val contactsList = mutableListOf<Contact>()

                        for (i in 0 until contactsJsonArray.length()) {
                            val contactJson = contactsJsonArray.getJSONObject(i)
                            val contact = Contact(
                                id = contactJson.getInt("id"),
                                name = contactJson.getString("name"),
                                phone = contactJson.getString("phone"),
                                selected = contactJson.getBoolean("selected")
                            )
                            contactsList.add(contact)
                        }
                    } catch (e: JSONException) {
                        Log.e("APICACA", "Erreur parsing JSON : ${e.message}")
                    }
                    // Parse JSON ici
                } else if (response.code == 401) {
                    Log.w("APICACA", "Token invalide ou expiré")
                    // Rediriger vers login ?
                } else {
                    Log.e("APICACA", "Erreur inconnue : ${response.code}")
                }
            }
        })
    }

    fun SaveInfoAcount(context: Context,firstname: String, lastname: String,phone: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("firstname", firstname)
            put("lastname", lastname)
            put("phone", phone)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${AppValues.base_url}/api/updateInfoAccount/")
            .post(body)
            .build()

        ApiClient.getClient(context).newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("APICACA", "Erreur : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodys = response.body?.string()
                    Log.d("APICACA", "Réponse : $bodys")
                    try {
                        val jsonObject = JSONObject(bodys)
                        val status = jsonObject.getBoolean("status")
                        if (status) {
                            Log.d("APICACA", "Mise à jour réussie")
                            callback(true)
                        } else {
                            Log.d("APICACA", "Mise à jour échouée")
                            callback(false)
                        }
                    } catch (e: JSONException) {
                        Log.e("APICACA", "Erreur parsing JSON : ${e.message}")
                    }
                    // Parse JSON ici
                } else if (response.code == 401) {
                    Log.w("APICACA", "Token invalide ou expiré")
                    // Rediriger vers login ?
                } else {
                    Log.e("APICACA", "Erreur inconnue : ${response.code}")
                }
            }
        })
    }

    fun logout(context: Context, activity: Activity) {
        val request = Request.Builder()
            .url("${AppValues.base_url}/api/logout/")
            .build()

        ApiClient.getClient(context).newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("APICACA", "Erreur : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    Log.d("APICACA", "Réponse : $body")
                    tokenManager.delToken()
                    activity.runOnUiThread {
                        val intent = Intent(activity,  login_activity::class.java)
                        activity.startActivity(intent)
                        activity.finish()
                    }
                    // Parse JSON ici
                } else if (response.code == 401) {
                    Log.w("APICACA", "Token invalide ou expiré")
                    // Rediriger vers login ?
                } else {
                    Log.e("APICACA", "Erreur inconnue : ${response.code}")
                }
            }
        })
    }

}