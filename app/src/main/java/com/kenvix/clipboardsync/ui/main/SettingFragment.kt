//--------------------------------------------------
// Class SettingFragment
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.clipboardsync.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.kenvix.clipboardsync.R
import java.util.*

class SettingFragment internal constructor(): PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_main)

        findPreference<Preference>("app_name")?.setOnPreferenceClickListener { preference ->
            openURL(
                "market://details?id=" + Objects.requireNonNull(
                    context
                )?.packageName
            )
        }

        findPreference<Preference>("view_github")?.setOnPreferenceClickListener { openURL("https://github.com/kenvix/BluetoothClipboardSync") }
        findPreference<Preference>("author")?.setOnPreferenceClickListener { openURL("https://kenvix.com") }
    }

    private fun openURL(url: String): Boolean {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (ex: Exception) {
            Log.w("OpenURL", "Exception occurred: $ex")
        }

        return false
    }
}