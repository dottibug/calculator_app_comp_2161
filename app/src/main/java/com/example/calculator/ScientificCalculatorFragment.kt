package com.example.calculator

import android.os.Bundle
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
    private val memoryUtils = MemoryUtilities()
    var equation : String = ""
    var result : String = ""
    var memory : String = ""
    private var isFinalResult : Boolean = false

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
        if (isFinalResult) equation = ""

        isFinalResult = false
        var cursorOffset = 1
        var (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

        equation = "$leftOfCursor$number$rightOfCursor"

        // Handles cursor position and offset when a number is clicked while isFinalResult was true
        if (equation.length == 1) {
            cursorPosition = 1
            cursorOffset = 0
        }

        // Render equation and result
        displayFragment.renderEquation(equation, cursorPosition, cursorOffset)
        calculateBedmasResult(equation)
    }

    // Handle operator clicks
    private fun onOperatorClick(operator: String) {
        if (isFinalResult && result != "error") {
            equation = "$result$operator"
            isFinalResult = false
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateBedmasResult(equation)
        } else {
            val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

            // Prevent user from entering an operator as the first char in the equation
            if (leftOfCursor.isEmpty()) { return }

            // Prevent user from entering two operators in a row
            if (calcUtils.hasDoubleOperators(leftOfCursor, rightOfCursor)) { return }

            // Prevent user from entering an operator to the right of an open bracket
            if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '(') { return }

            // Add trailing 0 if user enters operator beside a decimal
            if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '.' && rightOfCursor.isEmpty()) {
                val zero = "0"
                equation = "$leftOfCursor$zero$operator"
                displayFragment.renderEquation(equation, cursorPosition, 2)
                return
            }

            // Render equation and result
            equation = "$leftOfCursor$operator$rightOfCursor"
            displayFragment.renderEquation(equation, cursorPosition, 1)
        }
        calculateBedmasResult(equation)
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
            // Render equation and result
            equation = "$leftOfCursor$rightOfCursor"
            displayFragment.renderEquation(equation, cursorPosition, cursorOffset)
            calculateBedmasResult(equation)
            return
        } else {
            // Add leading 0 if needed
            val (decimalEquation, leadingZeroAdded) = calcUtils.getEquationWithDecimal(leftOfCursor, rightOfCursor)

            // Render equation and result
            cursorOffset = if (leadingZeroAdded) 2 else 1
            equation = decimalEquation
            displayFragment.renderEquation(equation, cursorPosition, cursorOffset)
            calculateBedmasResult(equation)
        }
    }

    // Handle sign clicks
    private fun onSignClick() {
        if (isFinalResult && result != "error") {
            if (result.startsWith("-")) equation = result.removePrefix("-")
            else equation = "(-$result"
            isFinalResult = false
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateBedmasResult(equation)
            return
        }

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
        if (isFinalResult && result != "error") {
            equation = "$result×("
            isFinalResult = false
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateBedmasResult(equation)
            return
        }

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
        if (isFinalResult && result != "error") {
            equation = result
            isFinalResult = false
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateBedmasResult(equation)
            return
        }

        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)

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
            equation = "$leftOfCursor$zero)"
            displayFragment.renderEquation(equation, cursorPosition, 2)
            return
        }

        // Render equation and result
        val bracket = ")"
        equation = "$leftOfCursor$bracket$rightOfCursor"
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

        isFinalResult = true
        calculateBedmasResult(equation)
    }

    // Get result and show error toast if applicable
    private fun calculateBedmasResult(calcEquation: String) {
        if (calcEquation.length == 0) result = "" else result = calcUtils.calculateBEDMAS(calcEquation)
        if (result == "error") { fragUtils.showToast("Invalid equation", requireContext()) }
        if (isFinalResult) displayFragment.renderFinalResult(result) else displayFragment.renderResult(result)
    }

    // Clear equation, result, and display
    private fun onClearClick() {
        equation = ""
        result = ""
        displayFragment.renderEquation(equation, 0, 0)
        displayFragment.renderResult(result)
    }

    // Handle backspace click
    fun onBackspace() {
        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, equation)
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

        equation = "$updatedLeftOfCursor$updatedRightOfCursor"
        displayFragment.renderEquation(equation, cursorPosition - 1, 0)
        calculateBedmasResult(equation)
    }

    // MEMORY FUNCTIONS //
    fun onMemStore() {
        var number = equation

        if (equation.isEmpty() && result.isEmpty()) { return }

        // If the result is final, store the result
        if (isFinalResult) number = result

        // Set isFinalResult to true so the result is rendered in the darker color
        isFinalResult = true

        val isValidNumber = memoryUtils.isNumber(number)
        if (isValidNumber) {
            memory = memoryUtils.getScientificNumber(number)
            fragUtils.showToast("Memory updated", requireContext())
            calculateBedmasResult(number)
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
            calculateBedmasResult(equation)
            return
        }

        equation = memory
        fragUtils.showToast("Memory recalled", requireContext())
        displayFragment.renderEquation(equation, equation.length, 0)
        calculateBedmasResult(equation)
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
            calculateBedmasResult(equation)
            return
        }

        // If a final result is displayed, add memory num to the result
        if (isFinalResult) {
            equation = "$result+$memory"
            isFinalResult = true
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateBedmasResult(equation)
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
            calculateBedmasResult(equation)
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
            calculateBedmasResult(equation)
            return
        }

        // If a final result is displayed, subtract memory num from the result
        if (isFinalResult) {
            equation = "$result~$memory"
            isFinalResult = true
            displayFragment.renderEquation(equation, equation.length, 0)
            calculateBedmasResult(equation)
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
            calculateBedmasResult(equation)
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