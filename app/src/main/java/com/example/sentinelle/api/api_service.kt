package com.example.sentinelle.api

import ApiClient
import TokenManager
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
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
                Log.e("APIRESULTAT", "Erreur : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    Log.d("APIRESULTAT", "Réponse : $body")
                    try {
                        val jsonObject = JSONObject(body)
                        AppValues.firstname = jsonObject.getString("firstname")
                        AppValues.lastname = jsonObject.getString("lastname")
                        AppValues.phone = jsonObject.getString("phones")
                        AppValues.email = jsonObject.getString("email")
                        AppValues.message = jsonObject.getString("message")

                        val contactsJsonArray = jsonObject.getJSONArray("contacts")
                        val contactsList = mutableStateListOf<Contact>()

                        for (i in 0 until contactsJsonArray.length()) {
                            val contactJson = contactsJsonArray.getJSONObject(i)
                            val contact = Contact(
                                id = contactJson.getInt("id"),
                                name = contactJson.getString("name"),
                                phone = contactJson.getString("phone"),
                                selected = mutableStateOf(contactJson.getBoolean("selected"))
                            )
                            contactsList.add(contact)
                        }

                        AppValues.contacts = contactsList.toMutableStateList()
                    } catch (e: JSONException) {
                        Log.e("APIRESULTAT", "Erreur parsing JSON : ${e.message}")
                    }
                    // Parse JSON ici
                } else if (response.code == 401) {
                    Log.w("APIRESULTAT", "Token invalide ou expiré")
                    // Rediriger vers login ?
                } else {
                    Log.e("APIRESULTAT", "Erreur inconnue : ${response.code}")
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
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodys = response.body?.string()
                    try {
                        val jsonObject = JSONObject(bodys)
                        val status = jsonObject.getBoolean("status")
                        if (status) {
                            callback(true)
                        } else {
                            callback(false)
                        }
                    } catch (e: JSONException) {
                        callback(false)
                    }
                } else if (response.code == 401) {
                    callback(false)
                    // Rediriger vers login ?

                } else {
                    callback(false)
                }
            }
        })
    }

    fun SaveMessage(context: Context,message: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("message", message)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${AppValues.base_url}/api/updatemessage/")
            .post(body)
            .build()

        ApiClient.getClient(context).newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodys = response.body?.string()
                    try {
                        val jsonObject = JSONObject(bodys)
                        val status = jsonObject.getBoolean("status")
                        if (status) {
                            callback(true)
                        } else {
                            callback(false)
                        }
                    } catch (e: JSONException) {
                        callback(false)
                    }
                } else if (response.code == 401) {
                    callback(false)
                } else {
                    callback(false)
                }
            }
        })
    }

    fun AddContact(context: Context,name: String, phone: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("name", name)
            put("phone", phone)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${AppValues.base_url}/api/addcontact/")
            .post(body)
            .build()

        ApiClient.getClient(context).newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodys = response.body?.string()
                    try {
                        val jsonObject = JSONObject(bodys)
                        val status = jsonObject.getBoolean("status")
                        if (status) {
                           getInfo(context)
                            callback(true)
                        } else {
                            callback(false)
                        }
                    } catch (e: JSONException) {
                        callback(false)
                    }
                } else if (response.code == 401) {
                    callback(false)
                } else {
                    callback(false)
                }
            }
        })
    }

    fun selectedContact(context: Context,id: Int, select: Boolean, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("id_contact", id)
            put("selected", select)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${AppValues.base_url}/api/updateselectcontact/")
            .post(body)
            .build()

        ApiClient.getClient(context).newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodys = response.body?.string()
                    try {
                        val jsonObject = JSONObject(bodys)
                        val status = jsonObject.getBoolean("status")
                        if (status) {
                            getInfo(context)
                            callback(true)
                        } else {
                            callback(false)
                        }
                    } catch (e: JSONException) {
                        callback(false)
                    }
                } else if (response.code == 401) {
                    callback(false)
                } else {
                    callback(false)
                }
            }
        })
    }

    fun deleteContact(context: Context,id: Int, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("id_contact", id)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${AppValues.base_url}/api/deletecontact/")
            .post(body)
            .build()

        ApiClient.getClient(context).newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodys = response.body?.string()
                    try {
                        val jsonObject = JSONObject(bodys)
                        val status = jsonObject.getBoolean("status")
                        if (status) {
                            getInfo(context)
                            callback(true)
                        } else {
                            callback(false)
                        }
                    } catch (e: JSONException) {
                        callback(false)
                    }
                } else if (response.code == 401) {
                    callback(false)
                } else {
                    callback(false)
                }
            }
        })
    }



    fun saveNewPassword(context: Context,password: String, newPassword: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("password", password)
            put("newPassword", newPassword)
        }

        val body = json.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("${AppValues.base_url}/api/updatePassword/")
            .post(body)
            .build()

        ApiClient.getClient(context).newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("APIRESULTAT", "Erreur : ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val bodys = response.body?.string()
                    try {
                        val jsonObject = JSONObject(bodys)
                        val status = jsonObject.getBoolean("status")
                        if (status) {
                            callback(true)
                        } else {
                            callback(false)
                        }
                    } catch (e: JSONException) {
                        callback(false)

                    }
                } else if (response.code == 401) {
                    callback(false)
                    // Rediriger vers login ?
                }else if (response.code == 400) {
                    callback(false)
                } else {
                    callback(false)
                }
            }
        })
    }

    fun logout(
        context: Context,
        onLogoutSuccess: () -> Unit,
        onLogoutFailure: (String) -> Unit = {}
    ) {
        tokenManager.delToken()
        onLogoutSuccess()
    }

}