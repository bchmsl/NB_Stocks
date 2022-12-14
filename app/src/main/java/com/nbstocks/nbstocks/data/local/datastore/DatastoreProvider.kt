package com.nbstocks.nbstocks.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

object DatastoreProvider {
    private const val DATASTORE_NAME = "userSettingsDataStore"
    private val SHOW_BALANCE_PREF_KEY = booleanPreferencesKey("showBalance")

    private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

    suspend fun Context.savePreference(showBalance: Boolean) {
        this.dataStore.edit {
            it[SHOW_BALANCE_PREF_KEY] = showBalance
        }
    }

    suspend fun Context.readPreference(defaultShowBalance: Boolean): Boolean {
        return dataStore.data.first()[SHOW_BALANCE_PREF_KEY] ?: defaultShowBalance
    }
}