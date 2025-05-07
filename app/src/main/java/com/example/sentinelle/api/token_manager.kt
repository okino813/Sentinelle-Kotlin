
import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TOKEN_KEY_ACCESS = "access_token"
        private const val TOKEN_KEY_REFRESH = "refresh_token"
    }

    // Enregistrer le token
    fun saveToken(access_token: String, refresh_token: String) {
        val editor = prefs.edit()
        editor.putString(TOKEN_KEY_ACCESS, access_token)
        editor.putString(TOKEN_KEY_REFRESH, refresh_token)
        editor.putBoolean("is_authentificated", true)
        editor.apply()
    }

    // Récupérer le token
    fun getToken(type: Int): String? {
        if (type == 0) {
            return prefs.getString(TOKEN_KEY_ACCESS, null)
        }
        else {
            return prefs.getString(TOKEN_KEY_REFRESH, null)
        }
    }

    // Supprimer les tokens
    fun removeToken() {
        val editor = prefs.edit()
        editor.remove(TOKEN_KEY_ACCESS)
        editor.remove(TOKEN_KEY_REFRESH)
        editor.apply()
    }
}
