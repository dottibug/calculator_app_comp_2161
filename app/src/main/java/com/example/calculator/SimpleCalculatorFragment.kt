package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.calculator.databinding.FragmentSimpleCalculatorBinding

// NOTE: The simple calculator calculates the result of an equation sequentially, from left to
//  right, ignoring BEDMAS order of operations
class SimpleCalculatorFragment : CalculatorFragment() {
    private lateinit var binding : FragmentSimpleCalculatorBinding
    private val mode = "simple"

    // TODO GET DECIMAL PLACES FROM USER SETTINGS WHEN IMPLEMENTED (pass to calculateLeftToRight
    //  functions)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSimpleCalculatorBinding.inflate(inflater, container, false)

        // TODO This can be refactored into a loop
        val numberButtons = listOf(
            ButtonData(binding.button0, "0", mode),
            ButtonData(binding.button1, "1", mode),
            ButtonData(binding.button2, "2", mode),
            ButtonData(binding.button3, "3", mode),
            ButtonData(binding.button4, "4", mode),
            ButtonData(binding.button5, "5", mode),
            ButtonData(binding.button6, "6", mode),
            ButtonData(binding.button7, "7", mode),
            ButtonData(binding.button8, "8", mode),
            ButtonData(binding.button9, "9", mode)
        )

        // Note: × is the multiplication symbol, not the letter x
        val operatorButtons = listOf(
            ButtonData(binding.buttonAdd, "+", mode),
            ButtonData(binding.buttonSubtract, "~", mode),
            ButtonData(binding.buttonMultiply, "×", mode),
            ButtonData(binding.buttonDivide, "÷", mode)
        )

        calcUtils.setupNumberClickListeners(numberButtons, ::onNumberClick)
        calcUtils.setupOperatorClickListeners(operatorButtons, ::onOperatorClick)
        binding.buttonClear.setOnClickListener { onClearClick() }
        binding.buttonEqual.setOnClickListener { onEqualClick(mode) }
        binding.buttonDecimal.setOnClickListener { onDecimalClick(mode) }
        binding.buttonSign.setOnClickListener { onSignClick(mode) }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayFragment = parentFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment
        memoryFragment = parentFragmentManager.findFragmentById(R.id.memoryFragment) as MemoryFragment
    }

    // Handle backspace click
    fun onBackspace() {
        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getExpressionParts(displayFragment, expression)

        // Guard clause if cursor is at beginning of equation (nothing to delete)
        if (cursorPosition == 0) { return }

        // Delete the character at the cursor position
        val updatedLeftOfCursor = leftOfCursor.dropLast(1)

        expression = "$updatedLeftOfCursor$rightOfCursor"
        renderExpressionAndResult(expression, cursorPosition - 1, 0, mode)
    }
}