package com.okino813.sentinelle

import android.content.Context
import androidx.core.content.ContextCompat
import com.google.common.base.Verify.verify
import com.okino813.sentinelle.api.AppValues
import com.okino813.sentinelle.api.Saferider
import com.okino813.sentinelle.api.api_service
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.json.JSONObject
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    // Test unitaire
    @Test
    fun TU_008_vérification_generation_gpx() {
        // On instancie api_service avec un context mocké
        val mockContext = mockk<Context>(relaxed = true)
        val service = api_service(mockContext)

        val fakeSaferider = listOf(
            Saferider(
                id = 1,
                path = "",
                start_date = "2024-01-15T10:00:00",
                theorotical_end_date = "2024-01-15T11:00:00",
                real_end_date = "",
                locked = false,
                status = 1
            )
        )

        val fakeCoords = listOf(
            Triple(48.8566, 2.3522, 1700000000.0), // Paris
            Triple(43.2965, 5.3698, 1700000100.0)  // Marseille
        )

        val gpx = service.generateGpx(fakeSaferider, fakeCoords)

        assertTrue(gpx.contains("<trkpt lat=\"48.8566\" lon=\"2.3522\">"))
        assertTrue(gpx.contains("<trkpt lat=\"43.2965\" lon=\"5.3698\">"))
        assertTrue(gpx.contains("<trkseg>"))
        assertTrue(gpx.startsWith("<?xml"))
    }

    @Test
    fun TU_009_verification_timestamp() {
        val mockContext = mockk<Context>(relaxed = true)
        val service = api_service(mockContext)

        // Timestamp Unix connu : 2024-01-15 10:00:00 UTC
        val timestamp = 1705312800L

        val result = service.formatTimestampToIso(timestamp)

        assertEquals("2024-01-15T10:00:00Z", result)
    }

    // Test fonctionnel
    @Test
    fun TF_008_modification_message() {
        val mockContext = mockk<Context>(relaxed = true)
        val service = api_service(mockContext)

        var capturedJson: JSONObject? = null

        mockkObject(ApiHelper)
        every {
            ApiHelper.apiPost(any(), eq("updatemessage"), any(), captureLambda(), any())
        } answers {
            capturedJson = thirdArg()
            val fakeResponse = JSONObject().apply { put("status", true) }
            lambda<(JSONObject) -> Unit>().captured.invoke(fakeResponse)
        }

        var result = false
        service.SaveMessage(mockContext, "Je suis en sécurité") { success ->
            result = success
        }

        assertTrue(result)
        assertEquals("Je suis en sécurité", capturedJson?.getString("message"))
    }

    @Test
    fun TF_009_supression_contact_avec_erreur() {
        val mockContext = mockk<Context>(relaxed = true)
        val service = api_service(mockContext)

        mockkObject(ApiHelper)
        every {
            ApiHelper.apiPost(any(), eq("deletecontact"), any(), any(), captureLambda())
        } answers {
            // On simule une erreur réseau en appelant le callback onError
            lambda<() -> Unit>().captured.invoke()
        }

        var result = true // On initialise à true pour vérifier qu'il passe bien à false

        service.deleteContact(mockContext, 42) { success ->
            result = success
        }

        assertFalse(result)
    }

}