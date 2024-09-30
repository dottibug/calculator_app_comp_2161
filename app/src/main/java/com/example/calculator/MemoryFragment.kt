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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentMemoryBinding.inflate(inflater, container, false)

        activity = requireActivity() as MainActivity
        isScientificMode = activity.isScientificMode

        // Create click listeners
        binding.buttonMemoryClear.setOnClickListener { onMemoryClear() }
        binding.buttonMemoryStore.setOnClickListener { onMemoryStore() }
        binding.buttonMemoryRecall.setOnClickListener { onMemoryRecall() }
        binding.buttonMemoryAdd.setOnClickListener { onMemoryOperation("+") }
        binding.buttonMemorySubtract.setOnClickListener { onMemoryOperation("~") }

        return binding.root
    }

    // Clear memory
    fun onMemoryClear() {
        if (isScientificMode) {
            val scientificCalculatorFragment = parentFragmentManager.findFragmentById(R.id.scientificCalculatorFragment) as ScientificCalculatorFragment
            scientificCalculatorFragment.onMemClear()
        } else {
            val simpleCalculatorFragment = parentFragmentManager.findFragmentById(R.id.simpleCalculatorFragment) as SimpleCalculatorFragment
            simpleCalculatorFragment.onMemClear()
        }
    }

    // Store the number in equation to memory (if it's a valid number)
    private fun onMemoryStore() {
        if (isScientificMode) {
            val scientificCalculatorFragment = parentFragmentManager.findFragmentById(R.id.scientificCalculatorFragment) as ScientificCalculatorFragment
            scientificCalculatorFragment.onMemStore("scientific")
        } else {
            val simpleCalculatorFragment = parentFragmentManager.findFragmentById(R.id.simpleCalculatorFragment) as SimpleCalculatorFragment
            simpleCalculatorFragment.onMemStore("simple")
        }
    }

    // Display the number in memory
    fun onMemoryRecall() {
        if (isScientificMode) {
            val scientificCalculatorFragment = parentFragmentManager.findFragmentById(R.id.scientificCalculatorFragment) as ScientificCalculatorFragment
            scientificCalculatorFragment.onMemRecall("scientific")
        } else {
            val simpleCalculatorFragment = parentFragmentManager.findFragmentById(R.id.simpleCalculatorFragment) as SimpleCalculatorFragment
            simpleCalculatorFragment.onMemRecall("simple")
        }
    }

    // Add or subtract a number to the number in memory and replace memory with the new value
    fun onMemoryOperation(operator: String) {
        if (isScientificMode) {
            val scientificCalculatorFragment = parentFragmentManager.findFragmentById(R.id.scientificCalculatorFragment) as ScientificCalculatorFragment
            scientificCalculatorFragment.onMemOperation("scientific", operator)
        } else {
            val simpleCalculatorFragment = parentFragmentManager.findFragmentById(R.id.simpleCalculatorFragment) as SimpleCalculatorFragment
            simpleCalculatorFragment.onMemOperation("simple", operator)
        }
    }
}