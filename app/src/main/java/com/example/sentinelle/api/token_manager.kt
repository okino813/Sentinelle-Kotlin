
import android.content.Context
import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)


    // Enregistrer le token
    fun saveToken(token: String) {
        val editor = prefs.edit()
        editor.putString("auth_token", token)
        editor.putBoolean("is_authentificated", true)
        editor.apply()
    }

    // Enregistrer le token
    fun delToken() {
        val editor = prefs.edit()
        editor.remove("auth_token")              // Supprime la clé "auth_token"
        editor.putBoolean("is_authentificated", false)  // Met à jour l'authentification
        editor.apply()
    }

    // Récupérer le token
    fun getToken(type: Int): String? {
        return prefs.getString("auth_token", null)
    }

}

class AuthInterceptor(val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        val token = tokenProvider()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Token $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}

object ApiClient {
    fun getClient(context: Context): OkHttpClient {
        val tokenManager = TokenManager(context)

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenManager.getToken(0) })
            .build()
    }
}