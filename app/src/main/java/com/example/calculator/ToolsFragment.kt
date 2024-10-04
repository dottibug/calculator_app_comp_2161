package com.example.calculator

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.example.calculator.databinding.FragmentToolsBinding

class ToolsFragment : Fragment() {
    private lateinit var binding : FragmentToolsBinding
    private lateinit var activity : MainActivity
    private lateinit var sharedPreferences: SharedPreferences
    private var isDarkMode : Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentToolsBinding.inflate(inflater, container, false)

        // Get the parent activity
        activity = requireActivity() as MainActivity
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        // NavController for navigating between simple and scientific modes
        val navController = findNavController()

        // Create click listeners
        binding.buttonSettings.setOnClickListener { activity.onSettingsClick(navController) }
        binding.buttonCalculatorMode.setOnClickListener { activity.onCalculatorModeClick(navController) }
        binding.buttonBackspace.setOnClickListener { onBackspaceClick() }

        setupDarkModeToggle()

        return binding.root
    }

    private fun setupDarkModeToggle() {
        isDarkMode = sharedPreferences.getBoolean("dark_mode", false)

        // Set the initial state of the switch
        updateDarkModeButton(isDarkMode)

        binding.buttonDarkMode.setOnClickListener {
            isDarkMode = !isDarkMode
            sharedPreferences.edit().putBoolean("dark_mode", isDarkMode).apply()
            activity.applyNightMode()
            updateDarkModeButton(isDarkMode)
        }
    }

    private fun updateDarkModeButton(isDarkMode: Boolean) {
        binding.buttonDarkMode.backgroundTintList = ColorStateList.valueOf(
            if (isDarkMode) {
                getColor(requireContext(), R.color.violet)
            } else {
                getColor(requireContext(), R.color.platinum)
            })
    }


    // Call the correct backspace method depending on calculator mode
    private fun onBackspaceClick() {
        // Get isScientificMode from MainActivity
        val isScientificMode = activity.isScientificMode

        if (isScientificMode) {
            // Get the scientific calculator fragment via the parent fragment manager
            val scientificCalculatorFragment = parentFragmentManager.findFragmentById(R.id.scientificCalculatorFragment) as ScientificCalculatorFragment
            scientificCalculatorFragment.onBackspace()
        } else {
            // Get the simple calculator fragment via the parent fragment manager
            val simpleCalculatorFragment = parentFragmentManager.findFragmentById(R.id.simpleCalculatorFragment) as SimpleCalculatorFragment
            simpleCalculatorFragment.onBackspace()
            }
        }
}