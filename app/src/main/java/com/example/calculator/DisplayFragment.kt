package com.example.calculator

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.calculator.databinding.FragmentDisplayBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// Fragment to display the running equation and result of the calculation
class DisplayFragment : Fragment() {

    private lateinit var binding: FragmentDisplayBinding
    private val viewModel: CalculatorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentDisplayBinding.inflate(inflater, container, false)

        // Update the equation string when state changes
        viewModel.equation.onEach { equation ->
            Log.i("testcat", "Number 5 clicked: Display Fragment")
//            binding.equation.text = equation
            binding.viewModel = viewModel
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        return binding.root
    }
}