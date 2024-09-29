package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.calculator.databinding.FragmentToolsBinding

class ToolsFragment : Fragment() {

    private lateinit var binding : FragmentToolsBinding
    private lateinit var activity : MainActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentToolsBinding.inflate(inflater, container, false)

        // Get the parent activity
        activity = requireActivity() as MainActivity

        // NavController for navigating between simple and scientific modes
        val navController = findNavController()

        // Create click listeners
        binding.buttonCalculatorMode.setOnClickListener { activity.onCalculatorModeClick(navController) }
        binding.buttonBackspace.setOnClickListener { onBackspaceClick() }

        return binding.root
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