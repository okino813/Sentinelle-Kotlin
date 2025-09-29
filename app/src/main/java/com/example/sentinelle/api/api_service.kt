package com.example.sentinelle.api

import android.content.Context
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.toMutableStateList
import com.example.sentinelle.ApiHelper
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

            val saferidersJsonArray = jsonObject.getJSONArray("saferiders")
            val saferidersList = mutableStateListOf<Saferider>()

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

            for (i in 0 until saferidersJsonArray.length()) {
                val saferiderJson = saferidersJsonArray.getJSONObject(i)
                val saferider = Saferider(
                    id = saferiderJson.getInt("id"),
                    path = saferiderJson.getString("path"),
                    start_date = saferiderJson.getString("start_date"),
                    theorotical_end_date = saferiderJson.getString("theorotical_end_date"),
                    real_end_date = saferiderJson.getString("real_end_date"),
                    locked = saferiderJson.getBoolean("locked"),
                    status = saferiderJson.getInt("status")
                )
                saferidersList.add(saferider)
            }


            AppValues.contacts = contactsList.toMutableStateList()
            AppValues.saferiders = saferidersList.toMutableStateList()

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

    fun AddContact(context: Context,name: String, phone: String, callback: (Boolean, Int) -> Unit) {
        val json = JSONObject().apply {
            put("name", name)
            put("phone", phone)
        }

        ApiHelper.apiPost(context, "addcontact", json, { jsonObj ->
            callback(jsonObj.optBoolean("status", false), jsonObj.optString("id_contact").toInt())
        }, {
            callback(false, 0)
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
        timestamp: String,
        callback: (Boolean) -> Unit
    ) {

        val json = JSONObject().apply {
            put("latitude", latitude)
            put("longitude", longitude)
            put("timestamp", timestamp)
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

    fun sendAudioFile(
        context: Context,
        audioFile: File,
        callback: (Boolean) -> Unit
    ) {
        ApiHelper.apiPostFile(
            context = context,
            endpoint = "sendaudio",
            file = audioFile,
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


    fun GetSafeRiderDetail(context: Context, id_saferider: Int?, callback: (JSONObject) -> Unit) {
        val json = JSONObject().apply {
            put("id_saferider", id_saferider)
        }

        ApiHelper.apiPost(context, "getsaferiderdetail", json, { jsonObject ->
            callback(jsonObject)
        }, {
            callback(
                JSONObject().put("satus", false)
            )
        })

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun DownloadSaferider(context: Context, saferider: List<Saferider>, coords: List<Triple<Double, Double, Double>>, audios : List<AudioRecord>): Boolean {
        Log.d("DownloadSaferider", "Téléchargement du trajet ${coords}")

        // Créez le contenu du fichier GPX
        val gpxContent = generateGpx(saferider, coords)

        Log.d("GPX", gpxContent)

        // On crée le dossier du saferider
        // On vérifie si le dossier racine "Saferiders" existe, sinon on le crée


        // On crée le dossier spécifique au saferider
        var trajetName = "${Saferider.getDate(saferider[0].start_date)}-${Saferider.getDate(saferider[0].start_date)}"
        // En cas de doublon on ajoute (bis) à la fin
        val currentDateTime: Date = Date()
        val currentTimestamp: Long = currentDateTime.time
        // Dossier par défaut
        var saferiderDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "Sentinelle/$trajetName"
        )

// Si le dossier existe déjà, on ajoute le suffixe (bis<timestamp>)
        if (saferiderDir.exists()) {
            trajetName += "(bis${currentTimestamp})"
            saferiderDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                "Sentinelle/${trajetName}"
            )
        }

        // On crée le dossier final
        if (!saferiderDir.exists()) {
            saferiderDir.mkdirs()
        }

        // On récupère les fichier audio depuis l'api
        for(audio in audios) {
            // On split le path pour récupérer le nom du fichier
            var name = audio.path.split("/").last()
            Log.d("DownloadSaferider", "Téléchargement de l'audio ${name}")

            ApiHelper.downloadFileToDocuments(context, trajetName = trajetName, url = "${AppValues.base_url}/api/listen/${audio.path}", fileName = name, onSuccess = { uri ->
                Log.d("DownloadSaferider", "Fichier audio téléchargé avec succès : $uri")
            }, onError = {
                Log.e("DownloadSaferider", "Erreur lors du téléchargement du fichier audio")
            })
        }

        // On crée le fichier GPX dans le dossier
        val gpxFile = File(saferiderDir, "saferider_$trajetName.gpx")
        gpxFile.writeText(gpxContent)

        return true

    }

    fun formatTimestampToIso(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.FRENCH)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp* 1000)) // ton timestamp est en secondes
    }

    fun generateGpx(saferider: List<Saferider>, locations: List<Triple<Double, Double, Double>>): String {
        val sb = StringBuilder()

        var dateJ = Saferider.getDate(saferider[0].start_date)

        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append("\n")
        sb.append("""<gpx version="1.1" creator="MyApp" xmlns="http://www.topografix.com/GPX/1/1">""").append("\n")
        sb.append("  <trk>\n")
        sb.append("    <name>Saferider du $dateJ</name>\n")
        sb.append("    <trkseg>\n")

        for (loc in locations) {
            Log.d("GPX", "Location: lat=${loc.first}, lon=${loc.second}, time=${loc.third.toLong()}")
            val isoTime = formatTimestampToIso(loc.third.toLong())
            sb.append("""      <trkpt lat="${loc.first}" lon="${loc.second}">""").append("\n")
            sb.append("        <time>$isoTime</time>\n")
            sb.append("      </trkpt>\n")
        }

        sb.append("    </trkseg>\n")
        sb.append("  </trk>\n")
        sb.append("</gpx>\n")
        return sb.toString()
    }


    fun logout(
        context: Context,
        onLogoutSuccess: () -> Unit,
        onLogoutFailure: (String) -> Unit = {}
    ) {
        onLogoutSuccess()
    }



}