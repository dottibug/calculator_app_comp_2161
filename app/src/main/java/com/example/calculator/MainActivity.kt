package com.example.calculator

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.preference.PreferenceManager
import com.example.calculator.databinding.ActivityMainBinding

//
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    var isScientificMode : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Base_Theme_Calculator)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        applyNightMode()
        enableEdgeToEdge()

        // Inflates the layout using the data binding object and sets the root view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setupNavigation() // To Settings Fragment (but also to Simple or Scientific Mode?)

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            isScientificMode = sharedPreferences.getBoolean("always_scientific", false)
        }

        // Sets up the window insets so app content is not covered by system UI
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Save state for screen rotation
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isScientificMode", isScientificMode)
    }

    // Restore state on screen rotation
    private fun restoreState(savedInstanceState: Bundle) {
        isScientificMode = savedInstanceState.getBoolean("isScientificMode")
    }

    // Apply night mode based on user preference
    fun applyNightMode() {
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    // Settings navigation
    fun onSettingsClick(navController: NavController) {
        if (isScientificMode) {
            navController.navigate(ScientificModeFragmentDirections.actionScientificModeFragmentToSettingsFragment())
        } else {
            navController.navigate(SimpleModeFragmentDirections.actionSimpleModeFragmentToSettingsFragment())
        }
    }

    // Navigate to the correct fragment based on the calculator mode
    fun onCalculatorModeClick(navController: NavController) {
        isScientificMode = !isScientificMode

        if (isScientificMode) {
            // Navigate to scientific mode
            navController.navigate(SimpleModeFragmentDirections.actionSimpleModeFragmentToScientificModeFragment())
        } else {
            // Navigate to simple mode
            navController.navigate(ScientificModeFragmentDirections.actionScientificModeFragmentToSimpleModeFragment())
        }
    }

    fun updateCalculatorDecimalPlaces() {
        supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is Calculator) { fragment.updateDecimalPlaces() }
        }
    }
}

