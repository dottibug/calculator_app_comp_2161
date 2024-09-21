package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.calculator.databinding.FragmentDisplayBinding
import kotlinx.coroutines.launch

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
        val view = binding.root

        // Update the equation string when state changes
        binding.viewModel = viewModel

        // Collect the equation state flow and update the equation string
        lifecycleScope.launch {
            viewModel.equation.collect { equation ->
                binding.textViewEquation.text = equation
            }
        }


//        viewModel.equation.onEach { equation ->
//            Log.i("testcat", "equation: $equation")
//            binding.textViewEquation.text = equation
//        }.launchIn(viewLifecycleOwner.lifecycleScope)

        return view
    }
}