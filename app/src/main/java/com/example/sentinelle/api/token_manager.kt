import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY = "access_token"
    }

    // Enregistrer le token
    fun saveToken(token: String) {
        val editor = prefs.edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    // Récupérer le token
    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    // Supprimer le token
    fun removeToken() {
        val editor = prefs.edit()
        editor.remove(TOKEN_KEY)
        editor.apply()
    }
}
