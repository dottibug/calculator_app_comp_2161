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
        binding.buttonMemoryAdd.setOnClickListener { onMemoryAdd() }
        binding.buttonMemorySubtract.setOnClickListener { onMemorySubtract() }

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
            scientificCalculatorFragment.onMemStore()
        } else {
            val simpleCalculatorFragment = parentFragmentManager.findFragmentById(R.id.simpleCalculatorFragment) as SimpleCalculatorFragment
            simpleCalculatorFragment.onMemStore()
        }
    }

    // Display the number in memory
    fun onMemoryRecall() {
        if (isScientificMode) {
            val scientificCalculatorFragment = parentFragmentManager.findFragmentById(R.id.scientificCalculatorFragment) as ScientificCalculatorFragment
            scientificCalculatorFragment.onMemRecall()
        } else {
            val simpleCalculatorFragment = parentFragmentManager.findFragmentById(R.id.simpleCalculatorFragment) as SimpleCalculatorFragment
            simpleCalculatorFragment.onMemRecall()
        }
    }

    // Add a number to the number in memory and replace memory with the new value
    fun onMemoryAdd() {
        if (isScientificMode) {
            val scientificCalculatorFragment = parentFragmentManager.findFragmentById(R.id.scientificCalculatorFragment) as ScientificCalculatorFragment
            scientificCalculatorFragment.onMemAdd()
        } else {
            val simpleCalculatorFragment = parentFragmentManager.findFragmentById(R.id.simpleCalculatorFragment) as SimpleCalculatorFragment
            simpleCalculatorFragment.onMemAdd()
        }
    }

    // Subtract a number to the number in memory and replace memory with the new value
    fun onMemorySubtract() {
        if (isScientificMode) {
            val scientificCalculatorFragment = parentFragmentManager.findFragmentById(R.id.scientificCalculatorFragment) as ScientificCalculatorFragment
            scientificCalculatorFragment.onMemSubtract()
        } else {
            val simpleCalculatorFragment = parentFragmentManager.findFragmentById(R.id.simpleCalculatorFragment) as SimpleCalculatorFragment
            simpleCalculatorFragment.onMemSubtract()
        }
    }
}