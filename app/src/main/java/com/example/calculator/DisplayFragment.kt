package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentDisplayBinding

// Fragment to display the running equation and result of the calculation
class DisplayFragment : Fragment() {

    private lateinit var binding: FragmentDisplayBinding
    lateinit var equation: TextView
    lateinit var result: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize the equation
        equation = binding.textViewEquation
        result = binding.result
    }

    fun getEquation() : String {
        return equation.text.toString()
    }

    // TODO need to show the intermediate results of the calculation
    fun updateEquation(input: String) {
        // Append the input to the equation
        val currentEquation = binding.textViewEquation.text.toString()
        binding.textViewEquation.text = currentEquation + input
    }

    fun clearResult() {
        binding.result.text = ""
    }

    fun updateResult(result: String) {
        binding.result.text = result
    }

}