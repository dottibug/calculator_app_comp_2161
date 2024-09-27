package com.example.calculator

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentScientificCalculatorBinding

// NOTE: The scientific calculator uses BEDMAS order of operations to calculate the result
class ScientificCalculatorFragment : Fragment() {
    private lateinit var binding: FragmentScientificCalculatorBinding
    private lateinit var displayFragment : DisplayFragment
    private val calcUtils = CalculatorUtilities()
    private val fragUtils = FragmentUtilities()
    private var equation : String = ""
    private var result : String = ""

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
        binding.buttonEqual.setOnClickListener { onScientificEqualClick() }
        binding.buttonDecimal.setOnClickListener { onDecimalClick() }
        binding.buttonSign.setOnClickListener { onSignClick() }
        binding.buttonBracketOpen.setOnClickListener { onOpenBracketClick() }
        binding.buttonBracketClose.setOnClickListener { onCloseBracketClick() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get the display fragment
        displayFragment = parentFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment
    }

    // Handle number clicks
    private fun onNumberClick(number: String) {
        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

        // Render equation and result
        equation = "$leftOfCursor$number$rightOfCursor"
        displayFragment.renderEquation(equation, cursorPosition, 1)
        calculateBedmasResult(equation)
    }

    // Handle operator clicks
    private fun onOperatorClick(operator: String) {
        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

        // Prevent user from entering an operator as the first char in the equation
        if (leftOfCursor.isEmpty()) { return }

        // Prevent user from entering two operators in a row
        if (calcUtils.hasDoubleOperators(leftOfCursor, rightOfCursor)) { return }

        // Prevent user from entering an operator to the right of an open bracket
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '(') { return }

        // Render equation and result
        equation = "$leftOfCursor$operator$rightOfCursor"
        displayFragment.renderEquation(equation, cursorPosition, 1)
        calculateBedmasResult(equation)
    }

    // Handle equals click
    private fun onScientificEqualClick() {
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

        // Clear equation
        equation = ""
        displayFragment.renderEquation(equation, 0, 0)
    }

    // Handle decimal clicks
    private fun onDecimalClick() {
        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)
        val decimal = "."

        // Prevent user from entering numbers with more than one decimal
        val testEquation = "$leftOfCursor$decimal$rightOfCursor"

        if (calcUtils.hasInvalidDecimal(testEquation)) {
            // Render equation and result
            equation = "$leftOfCursor$rightOfCursor"
            displayFragment.renderEquation(equation, cursorPosition, 0)
            calculateBedmasResult(equation)
            return
        } else {
            // Add leading 0 if needed
            val (decimalEquation, leadingZeroAdded) = calcUtils.getEquationWithDecimal(leftOfCursor, rightOfCursor)

            // Render equation and result
            val cursorOffset = if (leadingZeroAdded) 2 else 1
            equation = decimalEquation
            displayFragment.renderEquation(equation, cursorPosition, cursorOffset)
            calculateBedmasResult(equation)
        }
    }

    // Handle sign clicks
    private fun onSignClick() {
        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

        val (equationWithSign, cursorOffset) = calcUtils.getScientificEquationWithSign(equation,
            leftOfCursor, rightOfCursor, cursorPosition)

        // Render equation and result
        equation = equationWithSign
        displayFragment.renderEquation(equation, cursorPosition, cursorOffset)
        calculateBedmasResult(equation)
    }

    // Handle open bracket clicks
    private fun onOpenBracketClick() {
        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

        // Prevent user from entering an opening bracket if there is an operator to the immediate right
        if (rightOfCursor.isNotEmpty() && rightOfCursor.first() in setOf('+', '~', '×', '÷')) {
            return }

        // Add a multiplication symbol if user enters an opening bracket to the immediate right
        // of a digit
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() in setOf('0', '1', '2', '3', '4',
                '5', '6', '7', '8', '9')) {
            val multiplication = "×("
            equation = "$leftOfCursor$multiplication$rightOfCursor"
            displayFragment.renderEquation(equation, cursorPosition, 2)
            calculateBedmasResult(equation)
            return
        }

        // Add a multiplication symbol if user enters an opening bracket right beside an closing
        // bracket
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == ')') {
            val multiplication = "×("
            equation = "$leftOfCursor$multiplication$rightOfCursor"
            displayFragment.renderEquation(equation, cursorPosition, 2)
            calculateBedmasResult(equation)
            return
        }

        // Render equation and result
        val bracket = "("
        equation = "$leftOfCursor$bracket$rightOfCursor"
        displayFragment.renderEquation(equation, cursorPosition, 1)
        calculateBedmasResult(equation)
    }

    // Handle close bracket clicks
    private fun onCloseBracketClick() {
        Log.i("testcat", "equation in onCloseBracketClick: $equation")
        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

        // Prevent user from entering closing bracket as the first character in an equation
        if (leftOfCursor.isEmpty()) { return }

        // Prevent user from entering a closing bracket right beside an opening bracket
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '(') { return }

        // Prevent user from entering a closing bracket if there is no opening bracket
        if (!leftOfCursor.contains('(')) { return }

        // Prevent user from entering a closing bracket to the right of an operator or decimal
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() in setOf('+', '~', '×', '÷', '.')) { return }

        // Render equation and result
        val bracket = ")"
        equation = "$leftOfCursor$bracket$rightOfCursor"
        displayFragment.renderEquation(equation, cursorPosition, 1)
        calculateBedmasResult(equation)
    }

    // Get result and show error toast if applicable
    private fun calculateBedmasResult(calcEquation: String) {
        result = calcUtils.calculateBEDMAS(calcEquation)
        if (result == "error") {
            fragUtils.showToast("Invalid equation", requireContext())
        }
        displayFragment.renderResult(result)
    }

    // Clear equation, result, and display
    private fun onClearClick() {
        equation = ""
        result = ""
        displayFragment.renderEquation(equation, 0, 0)
        displayFragment.renderResult(result)
    }
}