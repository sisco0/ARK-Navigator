package space.taran.arknavigator.mvp.model

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import space.taran.arknavigator.BuildConfig
import space.taran.arknavigator.utils.Sorting
import javax.inject.Inject

class UserPreferences @Inject constructor(val context: Context) {
    private val SHARED_PREFERENCES_KEY = "user_preferences"

    private val Context.preferencesDatastore by preferencesDataStore((SHARED_PREFERENCES_KEY))

    private val dataStore = context.preferencesDatastore

    suspend fun clearPreferences() {
        setSorting(Sorting.DEFAULT)
        setSortingAscending(true)
        setCrashReportPref(CrashReport.NONE)
        setImgCacheReplication(ImgCacheReplication.NONE)
        setIndexReplication(IndexReplication.NONE)
    }

    suspend fun setSorting(sorting: Sorting) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORTING_PREFERENCE] = sorting.ordinal
        }
    }

    suspend fun getSorting(): Sorting {
        val intValue = dataStore.data.first()[PreferencesKeys.SORTING_PREFERENCE]
        return convertIntToSorting(intValue)
    }

    suspend fun setSortingAscending(isAscending: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SORTING_ORDER] = isAscending }
    }

    suspend fun isSortingAscending(): Boolean =
        dataStore.data.first()[PreferencesKeys.SORTING_ORDER] ?: true

    private fun convertIntToSorting(intValue: Int?): Sorting {
        return if (intValue == null) Sorting.DEFAULT
        else Sorting.values()[intValue]
    }

    suspend fun setCrashReportPref(pref: CrashReport) {

        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CRASH_REPORT_PREFERENCE] = pref.ordinal
        }
    }

    suspend fun getCrashReportPref(): CrashReport {
        val crashReportInt = dataStore.data.first()[PreferencesKeys.CRASH_REPORT_PREFERENCE] ?: 0
        return crashReportFromInt(crashReportInt)
    }

    suspend fun setImgCacheReplication(pref: ImgCacheReplication) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IMG_CACHE_REPLICATION_PREF] = pref.ordinal
        }
    }

    suspend fun getImgCacheReplication(): ImgCacheReplication {
        val imgCacheReplicationInt = dataStore.data.first()[PreferencesKeys.IMG_CACHE_REPLICATION_PREF] ?: 0
        return imgCacheReplicationFromInt(imgCacheReplicationInt)
    }

    suspend fun setIndexReplication(pref: IndexReplication) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.INDEX_REPLICATION_PREF] = pref.ordinal
        }
    }

    suspend fun getIndexReplication(): IndexReplication {
        val indexReplicationInt = dataStore.data.first()[PreferencesKeys.INDEX_REPLICATION_PREF] ?: 0
        return indexReplicationFromInt(indexReplicationInt)
    }

    private object PreferencesKeys {
        val SORTING_PREFERENCE = intPreferencesKey("sorting_preference")
        val SORTING_ORDER = booleanPreferencesKey("sorting_preference_is_ascending")
        val CRASH_REPORT_PREFERENCE = intPreferencesKey("crash_report_preference")
        val IMG_CACHE_REPLICATION_PREF = intPreferencesKey("img_cache_replication_preference")
        val INDEX_REPLICATION_PREF = intPreferencesKey("index_replication_preference")
    }

    enum class CrashReport {
        NONE, SEND_AUTOMATICALLY, DONT_SEND
    }

    enum class ImgCacheReplication {
        NONE, ENABLED, DISABLED
    }

    enum class IndexReplication {
        NONE, ENABLED, DISABLED
    }

    private fun crashReportFromInt(int: Int): CrashReport {
        val prefOrdinal = CrashReport.values()[int]
        return if (prefOrdinal == CrashReport.NONE) {
            if (BuildConfig.DEBUG) CrashReport.SEND_AUTOMATICALLY
            else CrashReport.DONT_SEND
        } else prefOrdinal
    }

    private fun imgCacheReplicationFromInt(int: Int): ImgCacheReplication {
        val prefOrdinal = ImgCacheReplication.values()[int]
        return if (prefOrdinal == ImgCacheReplication.NONE) ImgCacheReplication.ENABLED
        else prefOrdinal
    }

    private fun indexReplicationFromInt(int: Int): IndexReplication {
        val prefOrdinal = IndexReplication.values()[int]
        return if (prefOrdinal == IndexReplication.NONE) IndexReplication.ENABLED
        else prefOrdinal
    }
}