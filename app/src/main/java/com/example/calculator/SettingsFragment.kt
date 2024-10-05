package com.example.calculator

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

class SettingsFragment : PreferenceFragmentCompat() {
    private val appUtils = AppUtils()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        setupNightModePreference()
        setupDecimalPlacesPreference()
        setupDefaultCalcModePreference()
    }

    private fun setupNightModePreference() {
        val darkModePreference = findPreference<SwitchPreferenceCompat>("dark_mode")

        darkModePreference?.setOnPreferenceChangeListener { preference, newValue ->
            val darkModeEnabled = newValue as Boolean
            updateTheme(darkModeEnabled)
            true
        }
    }

    private fun setupDecimalPlacesPreference() {
        val decimalPlacesPreference = findPreference<EditTextPreference>("decimal_places")

        // Casts input type to number to allow only numbers (inputType is not an attribute in XML
        // for EditTextPreference, so we have to set it programmatically instead)
        decimalPlacesPreference?.setOnBindEditTextListener { editText ->
            editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        decimalPlacesPreference?.setOnPreferenceChangeListener { _, newValue ->
            val decimalPlaces = newValue.toString().toIntOrNull()
            if (decimalPlaces != null && decimalPlaces in 0..10) {
                val activity = requireActivity() as MainActivity
                activity.updateCalculatorDecimalPlaces()
                true
            } else {
                appUtils.showToast(requireContext(), "Please enter a number between 0 and 10")
                false
            }
        }
    }

    private fun updateTheme(darkModeEnabled: Boolean) {
        if (darkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun setupDefaultCalcModePreference() {
        val defaultCalcModePreference = findPreference<SwitchPreferenceCompat>("default_scientific_mode")

        defaultCalcModePreference?.setOnPreferenceChangeListener { _, newValue ->
            val isScientificDefault = newValue as Boolean
            true
        }
    }
}