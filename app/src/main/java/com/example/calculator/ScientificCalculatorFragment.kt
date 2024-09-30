package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.calculator.databinding.FragmentScientificCalculatorBinding

// NOTE: The scientific calculator uses BEDMAS order of operations to calculate the result
class ScientificCalculatorFragment : CalculatorFragment() {
    private lateinit var binding: FragmentScientificCalculatorBinding
    private val mode = "scientific"

    // TODO GET DECIMAL PLACES FROM USER SETTINGS WHEN IMPLEMENTED (pass to calculateBEDMAS
    //  functions)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentScientificCalculatorBinding.inflate(inflater, container, false)

        // List of buttons to set up click listeners for
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
        binding.buttonBracketOpen.setOnClickListener { onOpenBracketClick() }
        binding.buttonBracketClose.setOnClickListener { onCloseBracketClick() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get the display fragment
        displayFragment = parentFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment
    }

    // Handle open bracket clicks
    private fun onOpenBracketClick() {
        if (isFinalResult && result != "error") {
            expression = "$result×("
            isFinalResult = false
            renderExpressionAndResult(expression, expression.length, 0, mode)
            return
        }

        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getExpressionParts(displayFragment, expression)

        // Prevent user from entering an opening bracket if there is an operator to the immediate right
        if (rightOfCursor.isNotEmpty() && rightOfCursor.first() in setOf('+', '~', '×', '÷')) {
            return }

        // Add a multiplication symbol if user enters an opening bracket to the immediate right
        // of a digit
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() in setOf('0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9')) {
            val multiplication = "×("
            expression = "$leftOfCursor$multiplication$rightOfCursor"
            renderExpressionAndResult(expression, cursorPosition, 2, mode)
            return
        }

        // Add a multiplication symbol if user enters an opening bracket right beside an closing
        // bracket
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == ')') {
            val multiplication = "×("
            expression = "$leftOfCursor$multiplication$rightOfCursor"
            renderExpressionAndResult(expression, cursorPosition, 2, mode)
            return
        }

        // Render equation and result
        val bracket = "("
        expression = "$leftOfCursor$bracket$rightOfCursor"
        renderExpressionAndResult(expression, cursorPosition, 1, mode)
    }

    // Handle close bracket clicks
    private fun onCloseBracketClick() {
        if (isFinalResult && result != "error") {
            expression = result
            isFinalResult = false
            renderExpressionAndResult(expression, expression.length, 0, mode)
            return
        }

        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getExpressionParts(displayFragment, expression)

        // Prevent user from entering closing bracket as the first character in an equation
        if (leftOfCursor.isEmpty()) { return }

        // Prevent user from entering a closing bracket right beside an opening bracket
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '(') { return }

        // Prevent user from entering a closing bracket if there is no opening bracket
        if (!leftOfCursor.contains('(')) { return }

        // Prevent user from entering a closing bracket to the right of an operator
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() in setOf('+', '~', '×', '÷')) { return }

        // Prevent user from entering (-)
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '-') { return}

        // Add trailing 0 if user enters closing bracket beside a decimal
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '.' && rightOfCursor.isEmpty()) {
            val zero = "0"
            expression = "$leftOfCursor$zero)"
            renderExpressionAndResult(expression, cursorPosition, 2, mode)
            return
        }

        // Render equation and result
        val bracket = ")"
        expression = "$leftOfCursor$bracket$rightOfCursor"
        renderExpressionAndResult(expression, cursorPosition, 1, mode)
    }

    // Handle backspace click
    fun onBackspace() {
        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getExpressionParts(displayFragment, expression)
        var updatedLeftOfCursor = leftOfCursor
        var updatedRightOfCursor = rightOfCursor

        // Guard clause if cursor is at beginning of equation (nothing to delete)
        if (cursorPosition == 0) { return }

        // Delete the character at the cursor position
        updatedLeftOfCursor = updatedLeftOfCursor.dropLast(1)

        // If the right of cursor is a decimal, add a leading zero
        if (updatedRightOfCursor.isNotEmpty() && updatedRightOfCursor.first() == '.' &&
            updatedLeftOfCursor.last() in setOf('+', '~', '×', '÷')) {
            updatedRightOfCursor = "0$updatedRightOfCursor"
        }

        expression = "$updatedLeftOfCursor$updatedRightOfCursor"
        renderExpressionAndResult(expression, cursorPosition -1, 0, mode)
    }
}