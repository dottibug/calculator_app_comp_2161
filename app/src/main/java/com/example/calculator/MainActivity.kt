package com.example.calculator

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.preference.PreferenceManager
import com.example.calculator.databinding.ActivityMainBinding

//
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    var isScientificMode : Boolean = false
    private var initialScientificMode: Boolean = false

    var expression: String = ""
    var result: String = ""
    var memory: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Base_Theme_Calculator)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        applyNightMode()
        enableEdgeToEdge()

        // Inflates the layout using the data binding object and sets the root view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            // Set the initial mode based on user preference
            initialScientificMode = sharedPreferences.getBoolean("default_scientific_mode", false)
            // Set the current mode to the initial mode
            isScientificMode = initialScientificMode
        }

        // Set up the NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerNavigation) as NavHostFragment
        val navController = navHostFragment.navController

        // Set the start destination based on the default mode
        val navGraph = navController.navInflater.inflate(R.navigation.navigation)
        navGraph.setStartDestination(
            if (initialScientificMode) R.id.scientificModeFragment else R.id.simpleModeFragment
        )
        navController.graph = navGraph

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
        outState.putBoolean("initialScientificMode", initialScientificMode)
    }

    // Restore state on screen rotation
    private fun restoreState(savedInstanceState: Bundle) {
        isScientificMode = savedInstanceState.getBoolean("isScientificMode")
        initialScientificMode = savedInstanceState.getBoolean("initialScientificMode")
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

