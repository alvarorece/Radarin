package es.uniovi.asw.radarinen3b

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import es.uniovi.asw.radarinen3b.models.User
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


class UserPreferenceLiveData(private val sharedPreferences: SharedPreferences) :
    LiveData<User>() {

    private val mTokenSharedPreferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences?, key: String? ->
            if (sharedPreferences != null && key == PREFERENCE) {
                val str = sharedPreferences.getString(PREFERENCE, "")
                if (str == "")
                    value = null
                value = Json.decodeFromString<User>(str!!)
            }
        }


    override fun onActive() {
        super.onActive()
        val userData = sharedPreferences.getString(PREFERENCE, "")
        if (userData == "")
            value = null
        value = Json.decodeFromString<User>(userData!!)
        sharedPreferences.registerOnSharedPreferenceChangeListener(mTokenSharedPreferenceListener)
    }

    override fun onInactive() {
        super.onInactive()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(mTokenSharedPreferenceListener)
    }

    companion object {
        private const val PREFERENCE = "es.uniovi.asw.radarinen3b.userdata";
    }
}