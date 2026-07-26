package com.example.data.auth

import android.content.Context
import com.example.data.model.UserRole
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class RegisteredAccount(
    val email: String,
    val mobile: String,
    val name: String,
    val role: UserRole,
    val gymName: String,
    val passwordHash: String = ""
)

object AuthAccountManager {

    private const val PREFS_NAME = "fitops_auth_accounts"
    private const val KEY_ACCOUNTS_JSON = "registered_accounts_json"
    private const val KEY_LAST_LOGGED_IN_ACCOUNT = "last_logged_in_account"

    val defaultAccounts = listOf(
        RegisteredAccount(
            email = "alex.vance@fitops.ai",
            mobile = "+91 98765 43210",
            name = "Alex Vance",
            role = UserRole.GYM_OWNER,
            gymName = "Metro Fitness Club"
        ),
        RegisteredAccount(
            email = "marcus@fitops.ai",
            mobile = "+91 98765 22222",
            name = "Coach Marcus",
            role = UserRole.TRAINER,
            gymName = "Metro Fitness Club"
        ),
        RegisteredAccount(
            email = "tina@fitops.ai",
            mobile = "+91 98765 33333",
            name = "Tina Lopez",
            role = UserRole.RECEPTIONIST,
            gymName = "Metro Fitness Club"
        )
    )

    fun getRegisteredAccounts(context: Context): List<RegisteredAccount> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_ACCOUNTS_JSON, null)
        if (jsonStr.isNull_or_empty()) {
            saveList(context, defaultAccounts)
            return defaultAccounts
        }

        return try {
            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<RegisteredAccount>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val roleName = obj.optString("role", UserRole.GYM_OWNER.name)
                val role = try { UserRole.valueOf(roleName) } catch (e: Exception) { UserRole.GYM_OWNER }
                list.add(
                    RegisteredAccount(
                        email = obj.optString("email"),
                        mobile = obj.optString("mobile"),
                        name = obj.optString("name"),
                        role = role,
                        gymName = obj.optString("gymName", "Metro Fitness Club"),
                        passwordHash = obj.optString("passwordHash", "")
                    )
                )
            }
            if (list.isEmpty()) defaultAccounts else list
        } catch (e: Exception) {
            defaultAccounts
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.isEmpty()

    fun registerAccount(context: Context, account: RegisteredAccount) {
        val currentList = getRegisteredAccounts(context).toMutableList()
        currentList.removeAll { 
            (account.email.isNotBlank() && it.email.equals(account.email, ignoreCase = true)) ||
            (account.mobile.isNotBlank() && it.mobile.replace(" ", "") == account.mobile.replace(" ", ""))
        }
        currentList.add(0, account)

        saveList(context, currentList)
        setLastLoggedInAccount(context, account)

        // Sync account to online server (Firebase Firestore)
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            com.example.data.remote.FirestoreCloudBackendImpl().syncAccount(account)
        }
    }

    private fun saveList(context: Context, list: List<RegisteredAccount>) {
        val jsonArray = JSONArray()
        for (acc in list) {
            val obj = JSONObject().apply {
                put("email", acc.email)
                put("mobile", acc.mobile)
                put("name", acc.name)
                put("role", acc.role.name)
                put("gymName", acc.gymName)
                put("passwordHash", acc.passwordHash)
            }
            jsonArray.put(obj)
        }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACCOUNTS_JSON, jsonArray.toString())
            .apply()
    }

    fun findAccountForInput(context: Context, input: String): RegisteredAccount {
        val cleanInput = input.trim().lowercase()
        val accounts = getRegisteredAccounts(context)

        if (cleanInput.isBlank()) {
            return getLastLoggedInAccount(context) ?: accounts.firstOrNull() ?: defaultAccounts[0]
        }

        // 1. Phone digit match
        val inputDigits = cleanInput.filter { it.isDigit() }
        if (inputDigits.length >= 4) {
            val phoneMatch = accounts.firstOrNull { acc ->
                val accDigits = acc.mobile.filter { it.isDigit() }
                accDigits.endsWith(inputDigits) || accDigits.contains(inputDigits)
            }
            if (phoneMatch != null) return phoneMatch
        }

        // 2. Email exact or prefix match
        val emailMatch = accounts.firstOrNull { acc ->
            acc.email.isNotBlank() && (
                acc.email.lowercase() == cleanInput ||
                cleanInput.contains(acc.email.lowercase()) ||
                acc.email.lowercase().startsWith(cleanInput)
            )
        }
        if (emailMatch != null) return emailMatch

        // 3. Name match
        val nameMatch = accounts.firstOrNull { acc ->
            acc.name.isNotBlank() && (
                acc.name.lowercase().contains(cleanInput) ||
                cleanInput.contains(acc.name.lowercase())
            )
        }
        if (nameMatch != null) return nameMatch

        // 4. Role keyword fallback
        if (cleanInput.contains("trainer") || cleanInput.contains("marcus") || cleanInput.contains("pt")) {
            return accounts.firstOrNull { it.role == UserRole.TRAINER } ?: defaultAccounts[1]
        }
        if (cleanInput.contains("reception") || cleanInput.contains("tina") || cleanInput.contains("desk")) {
            return accounts.firstOrNull { it.role == UserRole.RECEPTIONIST } ?: defaultAccounts[2]
        }
        if (cleanInput.contains("owner") || cleanInput.contains("alex") || cleanInput.contains("admin")) {
            return accounts.firstOrNull { it.role == UserRole.GYM_OWNER } ?: defaultAccounts[0]
        }

        // 5. Default fallback to last logged in or first registered account
        return getLastLoggedInAccount(context) ?: accounts.firstOrNull() ?: defaultAccounts[0]
    }

    fun getLastLoggedInAccount(context: Context): RegisteredAccount? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = prefs.getString(KEY_LAST_LOGGED_IN_ACCOUNT, null) ?: return null
        return getRegisteredAccounts(context).firstOrNull { 
            it.email.equals(key, ignoreCase = true) || it.mobile.replace(" ", "") == key.replace(" ", "")
        }
    }

    fun setLastLoggedInAccount(context: Context, account: RegisteredAccount) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_LOGGED_IN_ACCOUNT, if (account.email.isNotBlank()) account.email else account.mobile)
            .apply()
    }
}
