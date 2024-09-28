package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentSimpleCalculatorBinding

// NOTE: The simple calculator calculates the result of an equation sequentially, from left to
//  right, ignoring BEDMAS order of operations
class SimpleCalculatorFragment : Fragment() {
    private lateinit var binding : FragmentSimpleCalculatorBinding
    private lateinit var displayFragment : DisplayFragment
    private val calcUtils = CalculatorUtilities()
    private val fragUtils = FragmentUtilities()
    var equation : String = ""
    var result : String = ""
    private var isFinalResult : Boolean = false

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
            ButtonData(binding.button0, "0"),
            ButtonData(binding.button1, "1"),
            ButtonData(binding.button2, "2"),
            ButtonData(binding.button3, "3"),
            ButtonData(binding.button4, "4"),
            ButtonData(binding.button5, "5"),
            ButtonData(binding.button6, "6"),
            ButtonData(binding.button7, "7"),
            ButtonData(binding.button8, "8"),
            ButtonData(binding.button9, "9")
        )

        // Note: × is the multiplication symbol, not the letter x
        val operatorButtons = listOf(
            ButtonData(binding.buttonAdd, "+"),
            ButtonData(binding.buttonSubtract, "~"),
            ButtonData(binding.buttonMultiply, "×"),
            ButtonData(binding.buttonDivide, "÷")
        )

        calcUtils.setupNumberClickListeners(numberButtons, ::onNumberClick)
        calcUtils.setupOperatorClickListeners(operatorButtons, ::onOperatorClick)
        binding.buttonClear.setOnClickListener { onClearClick() }
        binding.buttonEqual.setOnClickListener { onSimpleEqualClick() }
        binding.buttonDecimal.setOnClickListener { onDecimalClick() }
        binding.buttonSign.setOnClickListener { onSignClick() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get the display fragment
        displayFragment = parentFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment
    }

    // Handle number clicks
    private fun onNumberClick(number: String) {
        isFinalResult = false
        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

        // Render equation and result
        equation = "$leftOfCursor$number$rightOfCursor"
        displayFragment.renderEquation(equation, cursorPosition, 1)
        calculateLeftToRightResult(equation)
    }

    // Handle operator clicks
    private fun onOperatorClick(operator: String) {
        if (isFinalResult && result != "error") {
            equation = "$result$operator"
            isFinalResult = false
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateLeftToRightResult(equation)
            return
        }

        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(
                displayFragment, equation)

        // Prevent user from entering an operator as the first char in the equation
        if (leftOfCursor.isEmpty()) { return }

        // Prevent user from entering two operators in a row
        if (calcUtils.hasDoubleOperators(leftOfCursor, rightOfCursor)) { return }

        // Update equation and render equation
        equation = "$leftOfCursor$operator$rightOfCursor"
        displayFragment.renderEquation(equation, cursorPosition, 1)
        calculateLeftToRightResult(equation)
    }

    // Handle decimal clicks
    private fun onDecimalClick() {
        isFinalResult = false

        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)
        val decimal = "."

        // Prevent user from entering numbers with more than one decimal
        val testEquation = "$leftOfCursor$decimal$rightOfCursor"

        if (calcUtils.hasInvalidDecimal(testEquation)) {
            equation = "$leftOfCursor$rightOfCursor"
            displayFragment.renderEquation(equation, cursorPosition, 0)
            calculateLeftToRightResult(equation)
            return
        } else {
            // Add leading 0 if needed
            val (decimalEquation, leadingZeroAdded) = calcUtils.getEquationWithDecimal(leftOfCursor, rightOfCursor)

            // Render equation and result
            val cursorOffset = if (leadingZeroAdded) 2 else 1
            equation = decimalEquation
            displayFragment.renderEquation(equation, cursorPosition, cursorOffset)
            calculateLeftToRightResult(equation)
        }
    }

    // Handle sign clicks
    private fun onSignClick() {
        if (isFinalResult && result != "error") {
            if (result.startsWith("-")) equation = result.removePrefix("-")
            else equation = "-$result"
            isFinalResult = false
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateLeftToRightResult(equation)
            return
        }

        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

        val (equationWithSign, cursorOffset) = calcUtils.getSimpleEquationWithSign(equation,
            leftOfCursor, rightOfCursor, cursorPosition)

        // Render equation and result
        equation = equationWithSign
        displayFragment.renderEquation(equation, cursorPosition, cursorOffset)
        calculateLeftToRightResult(equation)
    }

    // Handle equals click
    private fun onSimpleEqualClick() {
        // Show toast message if equation is empty
        if (equation.isEmpty()) {
            fragUtils.showToast("Please enter an equation", requireContext())
            return
        }

        // If final character of the equation is an operator, show toast message
        if (equation.last() in setOf('+', '~', '×', '÷')) {
            fragUtils.showToast("Invalid equation", requireContext())
            return
        }

        isFinalResult = true

        // Calculate result and render result
        calculateLeftToRightResult(equation)
        equation = ""
        displayFragment.renderEquation(equation, 0, 0)
    }

    // Get result and show error toast if applicable
    private fun calculateLeftToRightResult(calcEquation: String) {
        if (calcEquation == "-") result = "" else result = calcUtils.calculateLeftToRight(calcEquation)

        if (result == "error") { fragUtils.showToast("Invalid equation", requireContext()) }
        if (isFinalResult) displayFragment.renderFinalResult(result) else displayFragment
            .renderResult(result)
    }

    // Clear equation, result, and display
    private fun onClearClick() {
        equation = ""
        result = ""
        isFinalResult = false
        displayFragment.renderEquation(equation, 0, 0)
        displayFragment.renderResult(result)
    }

    // Handle backspace click
    fun onBackspace() {
        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

        // Guard clause if cursor is at beginning of equation (nothing to delete)
        if (cursorPosition == 0) { return }

        // Delete the character at the cursor position
        val updatedLeftOfCursor = leftOfCursor.dropLast(1)

        equation = "$updatedLeftOfCursor$rightOfCursor"
        displayFragment.renderEquation(equation, cursorPosition - 1, 0)
        calculateLeftToRightResult(equation)
    }
}