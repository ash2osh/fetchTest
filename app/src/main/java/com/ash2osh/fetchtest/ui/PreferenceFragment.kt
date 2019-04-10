package com.ash2osh.fetchtest.ui


import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat
import com.ash2osh.fetchtest.R
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.NetworkType
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class PreferenceFragment : PreferenceFragmentCompat(), KodeinAware {
    override val kodein by closestKodein()
    private val fetch: Fetch by instance()
    private val listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "wifi_only" -> {
                    val wifi = sharedPreferences.getBoolean(key, false)
                    Log.d("pref changed" ,wifi.toString())
                    fetch.setGlobalNetworkType(
                        if (wifi)
                            NetworkType.WIFI_ONLY
                        else
                            NetworkType.ALL
                    )
                }
            }
        }


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }


    override fun onDestroy() {
        super.onDestroy()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)

    }
}
