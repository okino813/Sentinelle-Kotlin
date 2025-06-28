package com.example.sentinelle.api

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import com.example.sentinelle.ApiHelper
import org.json.JSONObject

class api_service(val context: Context) {

    fun getInfo(context: Context) {
        ApiHelper.apiGet(context, "getinfoaccount", { jsonObject ->

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

        }, {
            Log.e("APIRESULTAT", "Erreur lors de la récupération des infos")
        })
    }

    fun SaveInfoAccount(context: Context, firstname: String, lastname: String, phone: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("firstname", firstname)
            put("lastname", lastname)
            put("phone", phone)
        }

        ApiHelper.apiPost(context, "updateInfoAccount", json, { jsonObj ->
            callback(jsonObj.optBoolean("status", false))
        }, {
            callback(false)
        })
    }

    fun SaveMessage(context: Context,message: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("message", message)
        }

        ApiHelper.apiPost(context, "updatemessage", json, { jsonObj ->
            callback(jsonObj.optBoolean("status", false))
        }, {
            callback(false)
        })
    }

    fun AddContact(context: Context,name: String, phone: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("name", name)
            put("phone", phone)
        }

        ApiHelper.apiPost(context, "addcontact", json, { jsonObj ->
            getInfo(context)
            callback(jsonObj.optBoolean("status", false))
        }, {
            callback(false)
        })
    }

    fun selectedContact(context: Context,id: Int, select: Boolean, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("id_contact", id)
            put("selected", select)
        }

        ApiHelper.apiPost(context, "updateselectcontact", json, { jsonObj ->
            getInfo(context)
            callback(jsonObj.optBoolean("status", false))
        }, {
            callback(false)
        })
    }

    fun deleteContact(context: Context,id: Int, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("id_contact", id)
        }

        ApiHelper.apiPost(context, "deletecontact", json, { jsonObj ->
            getInfo(context)
            callback(jsonObj.optBoolean("status", false))
        }, {
            callback(false)
        })
    }

    fun startTimer(
        context: Context,
        hour: Int,
        minute: Int,
        seconde: Int,
        callback: (Boolean, String?) -> Unit
    ) {
        val totalSecond = hour * 60 * 60 + minute * 60 + seconde
        val json = JSONObject().apply {
            put("total_second", totalSecond)
        }

        ApiHelper.apiPost(
            context = context,
            endpoint = "starttimer",
            json = json,
            onSuccess = { responseJson ->
                val success = responseJson.optBoolean("success", false)
                if (success) {
                    callback(true, null)
                } else {
                    val error = responseJson.optString("error", "Erreur inconnue")
                    callback(false, error)
                }
            },
            onError = {
                callback(false, "Erreur lors de l'appel à l'API")
            }
        )
    }

    fun stopTimer(
        context: Context,
        callback: (Boolean, String?) -> Unit
    ) {
        ApiHelper.apiGet(
            context = context,
            endpoint = "stoptimer",
            onSuccess = { responseJson ->
                val success = responseJson.optBoolean("success", false)
                if (success) {
                    callback(true, null)
                } else {
                    val error = responseJson.optString("error", "Erreur inconnue")
                    callback(false, error)
                }
            },
            onError = {
                callback(false, "Erreur lors de l'appel à l'API")
            }
        )
    }

    fun sendLocation(
        context: Context,
        latitude: String,
        longitude: String,
        callback: (Boolean) -> Unit
    ) {

        val json = JSONObject().apply {
            put("latitude", latitude)
            put("longitude", longitude)
        }

        ApiHelper.apiPost(
            context = context,
            endpoint = "sendlocation",
            json = json,
            onSuccess = { responseJson ->
                val success = responseJson.optBoolean("success", false)
                if (success) {
                    callback(true)
                } else {
                    callback(false)
                }
            },
            onError = {
                callback(false)
            }
        )
    }

    fun saveNewPassword(context: Context,password: String, newPassword: String, callback: (Boolean) -> Unit) {
        val json = JSONObject().apply {
            put("password", password)
            put("newPassword", newPassword)
        }

        ApiHelper.apiPost(context, "updatePassword", json, { jsonObj ->
            callback(jsonObj.optBoolean("status", false))
        }, {
            callback(false)
        })

    }

    fun logout(
        context: Context,
        onLogoutSuccess: () -> Unit,
        onLogoutFailure: (String) -> Unit = {}
    ) {
        onLogoutSuccess()
    }

}