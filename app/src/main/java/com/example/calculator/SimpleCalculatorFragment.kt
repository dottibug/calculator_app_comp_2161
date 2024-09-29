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
    private lateinit var memoryFragment : MemoryFragment
    private val calcUtils = CalculatorUtilities()
    private val fragUtils = FragmentUtilities()
    private val memoryUtils = MemoryUtilities()
    var equation : String = ""
    var result : String = ""
    var memory : String = ""
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
        displayFragment = parentFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment
        memoryFragment = parentFragmentManager.findFragmentById(R.id.memoryFragment) as MemoryFragment
    }

    // Handle number clicks
    private fun onNumberClick(number: String) {
        if (isFinalResult) equation = ""

        isFinalResult = false
        var cursorOffset = 1
        var (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

        // Render equation and result
        equation = "$leftOfCursor$number$rightOfCursor"

        // Handles cursor position and offset when a number is clicked while isFinalResult was true
        if (equation.length == 1) {
            cursorPosition = 1
            cursorOffset = 0
        }

        displayFragment.renderEquation(equation, cursorPosition, cursorOffset)
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

        // Add trailing 0 if user enters operator beside a decimal
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '.' && rightOfCursor.isEmpty()) {
            val zero = "0"
            equation = "$leftOfCursor$zero$operator"
            displayFragment.renderEquation(equation, cursorPosition, 2)
            return
        }

        // Update equation and render equation
        equation = "$leftOfCursor$operator$rightOfCursor"
        displayFragment.renderEquation(equation, cursorPosition, 1)
        calculateLeftToRightResult(equation)
    }

    // Handle decimal clicks
    private fun onDecimalClick() {
        val decimal = "."
        if (isFinalResult) {
            if (result.contains('.')) return
            else equation = "$result$decimal"
        }

        isFinalResult = false
        var cursorOffset = 0

        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

        // Prevent user from entering numbers with more than one decimal
        val testEquation = "$leftOfCursor$decimal$rightOfCursor"

        if (calcUtils.hasInvalidDecimal(testEquation)) {
            equation = "$leftOfCursor$rightOfCursor"
            displayFragment.renderEquation(equation, cursorPosition, cursorOffset)
            calculateLeftToRightResult(equation)
            return
        } else {
            // Add leading 0 if needed
            val (decimalEquation, leadingZeroAdded) = calcUtils.getEquationWithDecimal(leftOfCursor, rightOfCursor)

            // Render equation and result
            cursorOffset = if (leadingZeroAdded) 2 else 1
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
        calculateLeftToRightResult(equation)
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

    // MEMORY FUNCTIONS //
    fun onMemStore() {
        var number = equation

        if (equation.isEmpty() && result.isEmpty()) { return }

        // Set isFinalResult to true so the result is rendered in the darker color
        if (isFinalResult) number = result

        // Set isFinalResult to true to ...
        isFinalResult = true

        val isValidNumber = memoryUtils.isNumber(number)
        if (isValidNumber) {
            memory = memoryUtils.getSimpleNumber(number)
            fragUtils.showToast("Memory updated", requireContext())
            calculateLeftToRightResult(number)
        }
        else fragUtils.showToast("Memory can only store numbers", requireContext())
    }

    fun onMemRecall() {
        isFinalResult = false

        if (memory.isEmpty()) {
            fragUtils.showToast("Memory is empty", requireContext())
            return
        }

        if (equation.isNotEmpty()) {
            if (equation.last() in setOf('+', '~', '×', '÷', '.')) {
                equation = "${equation.dropLast(1)}+$memory"
            } else {
                equation = "$equation+$memory"
            }

            displayFragment.renderEquation(equation, equation.length, 0)
            calculateLeftToRightResult(equation)
            return
        }

        equation = memory
        fragUtils.showToast("Memory recalled", requireContext())
        displayFragment.renderEquation(equation, equation.length, 0)
        calculateLeftToRightResult(equation)
    }

    fun onMemAdd() {
        if (memory.isEmpty()) {
            fragUtils.showToast("Memory is empty", requireContext())
            return
        }

        // If equation and result are empty, but memory is not, start an equation with memory num
        if (equation.isEmpty() && result.isEmpty() && memory.isNotEmpty()) {
            isFinalResult = false
            equation = memory
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateLeftToRightResult(equation)
            return
        }

        // If a final result is displayed, add memory num to the result
        if (isFinalResult) {
            equation = "$result+$memory"
            isFinalResult = true
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateLeftToRightResult(equation)
            memory = result
            fragUtils.showToast("Memory updated", requireContext())
            return
        }

        // If equation is not empty, add memory num to the end of the equation
        isFinalResult = false
        var number = equation
        val isValidNumber = memoryUtils.isNumber(number)

        if (isValidNumber) {
            val simpleNumber = memoryUtils.getSimpleNumber(number)
            equation = "$simpleNumber+$memory"
            displayFragment.renderEquation(equation, equation.length, 0)

            // Update memory
            isFinalResult = true
            calculateLeftToRightResult(equation)
            memory = result
            fragUtils.showToast("Memory updated", requireContext())
        }
        else fragUtils.showToast("Memory can only store numbers", requireContext())
    }

    fun onMemSubtract() {
        if (memory.isEmpty()) {
            fragUtils.showToast("Memory is empty", requireContext())
            return
        }

        // If equation and result are empty, but memory is not, start an equation with memory num
        if (equation.isEmpty() && result.isEmpty() && memory.isNotEmpty()) {
            isFinalResult = false
            equation = memory
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateLeftToRightResult(equation)
            return
        }

        // If a final result is displayed, subtract memory num from the result
        if (isFinalResult) {
            equation = "$result~$memory"
            isFinalResult = true
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateLeftToRightResult(equation)
            memory = result
            fragUtils.showToast("Memory updated", requireContext())
            return
        }

        // If equation is not empty, subtract memory num from the end of the equation
        isFinalResult = false
        var number = equation
        val isValidNumber = memoryUtils.isNumber(number)

        if (isValidNumber) {
            val simpleNumber = memoryUtils.getSimpleNumber(number)
            equation = "$simpleNumber~$memory"
            displayFragment.renderEquation(equation, equation.length, 0)

            // Update memory
            isFinalResult = true
            calculateLeftToRightResult(equation)
            memory = result
            fragUtils.showToast("Memory updated", requireContext())
        }
        else fragUtils.showToast("Memory can only store numbers", requireContext())
    }

    fun onMemClear() {
        isFinalResult = false
        memory = ""
        fragUtils.showToast("Memory cleared", requireContext())
    }


}