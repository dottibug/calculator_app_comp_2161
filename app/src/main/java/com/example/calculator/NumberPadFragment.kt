package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.calculator.databinding.FragmentNumberPadBinding

class NumberPadFragment : Fragment() {

    private lateinit var binding: FragmentNumberPadBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dataBinding layout for this fragment
        val viewModel: CalculatorViewModel by viewModels()

        binding = FragmentNumberPadBinding.inflate(inflater, container, false)
        return binding.root
    }
}