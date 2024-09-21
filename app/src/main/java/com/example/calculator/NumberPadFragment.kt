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
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentNumberPadBinding.inflate(inflater, container, false)

        createNumberClickListeners()

        return binding.root
    }

    private fun createNumberClickListeners() {
        binding.button5.setOnClickListener {
            viewModel.onNumberClicked("5")
        }
    }
}