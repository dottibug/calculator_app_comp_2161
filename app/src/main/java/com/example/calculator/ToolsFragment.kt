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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentToolsBinding.inflate(inflater, container, false)

        // Get the parent activity
        val activity = requireActivity() as MainActivity

//        // NavController for navigating between simple and scientific modes
        val navController = findNavController()

        // Create click listeners
        binding.buttonCalculatorMode.setOnClickListener { activity.onCalculatorModeClick(navController) }
        binding.buttonBackspace.setOnClickListener { activity.onBackspaceClick() }

        return binding.root
    }
}