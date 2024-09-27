package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentScientificModeBinding

class ScientificModeFragment : Fragment() {
    private lateinit var binding: FragmentScientificModeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using data binding
        binding = FragmentScientificModeBinding.inflate(inflater, container, false)
        return binding.root
    }
}