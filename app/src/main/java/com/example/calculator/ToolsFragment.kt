package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

        // Create click listeners
        binding.buttonBackspace.setOnClickListener { activity.onBackspaceClick() }

        return binding.root
    }
}