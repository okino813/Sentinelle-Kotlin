
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("sentinelle", Context.MODE_PRIVATE)

    fun fetchFirebaseToken(context: Context, onResult: (String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.getIdToken(true)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result?.token
                    if (token != null) {
                        TokenManager(context).saveToken(token)
                    }
                    onResult(token)
                } else {
                    onResult(null)
                }
            }
    }


    // Enregistrer le token
    fun saveToken(token: String) {
        val editor = prefs.edit()
        editor.putString("auth_token", token)
        editor.putBoolean("is_authentificated", true)
        editor.apply()
    }

    // Suprimer le token
    fun delToken() {
        val editor = prefs.edit()
        editor.remove("auth_token")              // Supprime la clé "auth_token"
        editor.putBoolean("is_authentificated", false)  // Met à jour l'authentification
        editor.apply()
    }

    // Récupérer le token
    fun getToken(): String? {
        return prefs.getString("auth_token", null)
    }

}

class AuthInterceptor(val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        val token = tokenProvider()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}

object ApiClient {
    fun getClient(context: Context): OkHttpClient {
        val tokenManager = TokenManager(context)

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenManager.getToken() })
            .build()
    }
}