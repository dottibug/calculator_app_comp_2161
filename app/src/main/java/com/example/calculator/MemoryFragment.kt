package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentMemoryBinding

class MemoryFragment : Fragment() {

    private lateinit var binding: FragmentMemoryBinding
    private lateinit var activity: MainActivity
    private var isScientificMode : Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentMemoryBinding.inflate(inflater, container, false)
        activity = requireActivity() as MainActivity
        isScientificMode = activity.isScientificMode
        setupButtons()
        return binding.root
    }

    private fun getActiveCalculatorFragment(): Fragment? {
        return if (isScientificMode) {
            parentFragmentManager.findFragmentById(R.id.scientificCalculatorFragment) as
                ScientificCalculatorFragment?
        } else {
            parentFragmentManager.findFragmentById(R.id.simpleCalculatorFragment) as
                SimpleCalculatorFragment?
        }
    }

    // Clear memory
    private fun onMemoryClear() {
        val fragment = getActiveCalculatorFragment()

        when (fragment) {
            is SimpleCalculatorFragment -> fragment.onMemClear()
            is ScientificCalculatorFragment -> fragment.onMemClear()
        }
    }

    // Store the number in equation to memory (if it's a valid number)
    private fun onMemoryStore() {
        val fragment = getActiveCalculatorFragment()

        when (fragment) {
            is SimpleCalculatorFragment -> fragment.onMemStore("simple")
            is ScientificCalculatorFragment -> fragment.onMemStore("scientific")
        }
    }

    // Display the number in memory
    private fun onMemoryRecall() {
        val fragment = getActiveCalculatorFragment()

        when (fragment) {
            is SimpleCalculatorFragment -> fragment.onMemRecall("simple")
            is ScientificCalculatorFragment -> fragment.onMemRecall("scientific")
        }
    }

    // Add or subtract a number to the number in memory and replace memory with the new value
    private fun onMemoryOperation(operator: String) {
        val fragment = getActiveCalculatorFragment()

        when (fragment) {
            is SimpleCalculatorFragment -> fragment.onMemOperation("simple", operator)
            is ScientificCalculatorFragment -> fragment.onMemOperation("scientific", operator)
        }
    }

    private fun setupButtons() {
        binding.buttonMemoryClear.setOnClickListener { onMemoryClear() }
        binding.buttonMemoryStore.setOnClickListener { onMemoryStore() }
        binding.buttonMemoryRecall.setOnClickListener { onMemoryRecall() }
        binding.buttonMemoryAdd.setOnClickListener { onMemoryOperation("+") }
        binding.buttonMemorySubtract.setOnClickListener { onMemoryOperation("~") }
    }
}