/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.datastore

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.mifospay.core.model.domain.client.Client
import com.mifospay.core.model.domain.user.User

/** Android SharedPreferences wrapper that stores the logged-in user's token, name, contact details and ids. */
class PreferencesHelper(private val context: Context?) {
    private val sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    /** Removes every stored preference. */
    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    /** Reads an int preference, returning [preferenceDefaultValue] when absent. */
    fun getInt(preferenceKey: String?, preferenceDefaultValue: Int): Int {
        return sharedPreferences.getInt(preferenceKey, preferenceDefaultValue)
    }

    /** Stores an int preference. */
    fun putInt(preferenceKey: String?, preferenceValue: Int) {
        sharedPreferences.edit().putInt(preferenceKey, preferenceValue).apply()
    }

    /** Reads a long preference, returning [preferenceDefaultValue] when absent. */
    fun getLong(preferenceKey: String?, preferenceDefaultValue: Long): Long {
        return sharedPreferences.getLong(preferenceKey, preferenceDefaultValue)
    }

    /** Stores a long preference. */
    fun putLong(preferenceKey: String?, preferenceValue: Long) {
        sharedPreferences.edit().putLong(preferenceKey, preferenceValue).apply()
    }

    /** Reads a string preference, returning [preferenceDefaultValue] when absent. */
    fun getString(preferenceKey: String?, preferenceDefaultValue: String?): String? {
        return sharedPreferences.getString(preferenceKey, preferenceDefaultValue)
    }

    /** Stores a string preference. */
    fun putString(preferenceKey: String?, preferenceValue: String?) {
        sharedPreferences.edit().putString(preferenceKey, preferenceValue).apply()
    }

    /** Persists the authentication [token]. */
    fun saveToken(token: String?) {
        putString(TOKEN, token)
    }

    /** Clears the stored authentication token. */
    fun clearToken() {
        putString(TOKEN, "")
    }

    /** Stored authentication token, empty when logged out. */
    val token: String?
        get() = getString(TOKEN, "")

    /** Persists the user's full [name]. */
    fun saveFullName(name: String?) {
        putString(NAME, name)
    }

    /** Stored full name of the user. */
    val fullName: String?
        get() = getString(NAME, " ")

    /** Persists the login username. */
    fun saveUsername(name: String?) {
        putString(USERNAME, name)
    }

    /** Stored login username, empty if none. */
    val username: String
        get() = getString(USERNAME, "") ?: ""

    /** Persists the user's [email]. */
    fun saveEmail(email: String?) {
        putString(EMAIL, email)
    }

    /** Stored email address of the user. */
    val email: String?
        get() = getString(EMAIL, "")

    /** Persists the user's [mobile] number. */
    fun saveMobile(mobile: String?) {
        putString(MOBILE_NO, mobile)
    }

    /** Stored mobile number of the user. */
    val mobile: String?
        get() = getString(MOBILE_NO, "")

    /** Stored user id, -1 when unset. */
    var userId: Long
        get() = getLong(USER_ID, -1)
        set(id) {
            putLong(USER_ID, id)
        }

    /** Stored Fineract client id, 1 when unset. */
    var clientId: Long
        get() = getLong(CLIENT_ID, 1)
        set(clientId) {
            putLong(CLIENT_ID, clientId)
        }

    /** Stored virtual payment address (VPA) of the client. */
    var clientVpa: String?
        get() = getString(CLIENT_VPA, "")
        set(vpa) {
            putString(CLIENT_VPA, vpa)
        }

    /** Stored default savings account id, 0 when unset. */
    var accountId: Long
        get() = getLong(ACCOUNT_ID, 0)
        set(accountId) {
            putLong(ACCOUNT_ID, accountId)
        }

    /** Stored Firebase push registration id. */
    var firebaseRegId: String?
        get() = getString(FIREBASE_REG_ID, "")
        set(firebaseRegId) {
            putString(FIREBASE_REG_ID, firebaseRegId)
        }

    /** The logged-in [User], serialized as JSON. */
    var user: User
        get() = Gson().fromJson(
            getString(PREF_USER, Gson().toJson(User::class.java)),
            User::class.java,
        )
        set(user) {
            putString(PREF_USER, Gson().toJson(user))
        }

    /** The logged-in [Client], serialized as JSON. */
    var client: Client
        get() = Gson().fromJson(
            getString(PREF_CLIENT, Gson().toJson(Client::class.java)),
            Client::class.java,
        )
        set(client) {
            putString(PREF_USER, Gson().toJson(client))
        }

    /** Preference key names. */
    companion object {
        private const val PREF_USER = "pref_user"
        private const val PREF_CLIENT = "pref_client"
        private const val TOKEN = "preferences_token"
        private const val NAME = "preferences_name"
        private const val USERNAME = "preferences_user_name"
        private const val EMAIL = "preferences_email"
        private const val CLIENT_ID = "preferences_client"
        private const val USER_ID = "preferences_user_id"
        private const val CLIENT_VPA = "preferences_client_vpa"
        private const val MOBILE_NO = "preferences_mobile_no"
        private const val FIREBASE_REG_ID = "preferences_firebase_reg_id"
        private const val ACCOUNT_ID = "preferences_account_id"
    }
}
