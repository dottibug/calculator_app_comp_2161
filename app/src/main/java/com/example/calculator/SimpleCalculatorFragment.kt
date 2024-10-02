package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.calculator.databinding.FragmentSimpleCalculatorBinding

// TODO GET DECIMAL PLACES FROM USER SETTINGS WHEN IMPLEMENTED (pass to calculateLeftToRight
//  functions)

// NOTE: The simple calculator calculates the result of an equation sequentially, from left to
//  right, ignoring BEDMAS order of operations
class SimpleCalculatorFragment : Calculator() {
    private lateinit var binding : FragmentSimpleCalculatorBinding
    private val mode = "simple"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSimpleCalculatorBinding.inflate(inflater, container, false)
        setupButtons()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        display = parentFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment
    }

    // Handle backspace click
    fun onBackspace() {
        val (curPos, left, right) = calcUtils.getParts(display, expression)

        // Guard clause if cursor is at beginning of equation (nothing to delete)
        if (expression.isEmpty() || curPos == 0) { return }

        // Delete the character at the cursor position
        val updatedLeft = left.dropLast(1)

        expression = "$updatedLeft$right"

        val newCurPos = curPos - 1
        calcResultAndRefreshDisplay(expression, newCurPos, 0, mode)
    }

    // -------------------------------------------------------
    // BUTTON SETUP
    // -------------------------------------------------------
    private fun setupButtons() {
        setupNumberButtons()
        setupOperatorButtons()
        setupSpecialButtons()
    }

    private fun setupNumberButtons() {
        binding.button0.setOnClickListener { onNumberClick("0", mode) }
        binding.button1.setOnClickListener { onNumberClick("1", mode) }
        binding.button2.setOnClickListener { onNumberClick("2", mode) }
        binding.button3.setOnClickListener { onNumberClick("3", mode) }
        binding.button4.setOnClickListener { onNumberClick("4", mode) }
        binding.button5.setOnClickListener { onNumberClick("5", mode) }
        binding.button6.setOnClickListener { onNumberClick("6", mode) }
        binding.button7.setOnClickListener { onNumberClick("7", mode) }
        binding.button8.setOnClickListener { onNumberClick("8", mode) }
        binding.button9.setOnClickListener { onNumberClick("9", mode) }
    }

    private fun setupOperatorButtons() {
        binding.buttonAdd.setOnClickListener { onOperatorClick("+", mode) }
        binding.buttonSubtract.setOnClickListener { onOperatorClick("~", mode) }
        binding.buttonMultiply.setOnClickListener { onOperatorClick("×", mode) }
        binding.buttonDivide.setOnClickListener { onOperatorClick("÷", mode) }
    }

    private fun setupSpecialButtons() {
        binding.buttonClear.setOnClickListener { onClearClick(mode) }
        binding.buttonEqual.setOnClickListener { onEqualClick(mode) }
        binding.buttonDecimal.setOnClickListener { onDecimalClick(mode) }
        binding.buttonSign.setOnClickListener { onSignClick(mode) }
    }
}