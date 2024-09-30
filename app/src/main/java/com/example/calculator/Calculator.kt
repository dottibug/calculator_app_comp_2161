package com.example.calculator

import android.util.Log
import androidx.fragment.app.Fragment
import java.util.Locale

abstract class CalculatorFragment : Fragment() {
    protected lateinit var displayFragment: DisplayFragment
    protected lateinit var memoryFragment: MemoryFragment
    protected val calcUtils = CalculatorUtilities()
    protected val fragUtils = FragmentUtilities()
    protected val memoryUtils = MemoryUtilities()
    protected var isFinalResult: Boolean = false
    protected var expression: String = ""
    protected var result: String = ""
    protected var memory: String = ""
    protected var decimalPlaces: Int = 10

    // Calculate and render the result in the display fragment. Simple mode uses sequential
    // calculation from left to right, ignoring BEDMAS order of operations. Scientific mode takes
    // BEDMAS order of operations into account.
    protected fun calculate(exp : String, mode : String) {
        if (mode == "simple") {
            result = if (exp == "-") "" else calcUtils.calculateLeftToRight(exp)
        }

        if (mode == "scientific") {
            result = if (exp.isEmpty()) "" else calcUtils.calculateBEDMAS(exp)
        }

        if (result == "error") {
            fragUtils.showToast(requireContext(), "Invalid expression")
        }

        // Check if the result has more than 12 digits
        val isTooLong = hasTooManyDigits(result)
        if (isTooLong) {
            fragUtils.showToast(requireContext(), "Max 12 digits in result")
            return
        }

        // Format result to default (10) or user-specified number of decimal places
        val formattedResult = String.format(Locale.CANADA,"%.${decimalPlaces}f", result.toDouble())
            .trimEnd('0')
            .trimEnd('.')
        result = formattedResult

        if (isFinalResult) { displayFragment.renderFinalResult(result) }
        else { displayFragment.renderResult(result) }
    }

    // Count the number of digits in a number
    private fun hasTooManyDigits(number: String): Boolean {
        if (number.isEmpty()) return false

        val formattedNumber = String.format(Locale.CANADA,"%.${decimalPlaces}f", number.toDouble())
            .trimEnd('0')
            .trimEnd('.')

        var digitCount = 0
        if (formattedNumber.contains(".")) {
            // Count digits of the unformatted number (we need to ignore the specified decimal
            // places to accurately count the number of digits)
            Log.i("testcat", "number is $number")
            for (char in number) {
                if (char.isDigit()) digitCount++
            }
        } else {
            // Count digits of the formatted number
            for (char in formattedNumber) {
                if (char.isDigit()) digitCount++
            }
        }

        return digitCount > 12
    }

    // Render the expression and calculated result in the display fragment
    protected fun renderExpressionAndResult(exp: String, cursorPosition: Int, cursorOffset: Int,
                                            mode: String) {
        displayFragment.renderExpression(exp, cursorPosition, cursorOffset)
        calculate(exp, mode)
    }

    // Handle number clicks
    protected fun onNumberClick(number: String, mode: String) {
        if (isFinalResult) expression = ""

        isFinalResult = false
        var cursorOffset = 1
        var (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getExpressionParts(displayFragment, expression)

        // Render equation and result
        expression = "$leftOfCursor$number$rightOfCursor"

        // Handles cursor position and offset when a number is clicked while isFinalResult was true
        if (expression.length == 1) {
            cursorPosition = 1
            cursorOffset = 0
        }

        renderExpressionAndResult(expression, cursorPosition, cursorOffset, mode)
    }

    // Handle operator clicks
    protected fun onOperatorClick(operator: String, mode: String) {
        if (isFinalResult && result != "error") {
            expression = "$result$operator"
            isFinalResult = false
            renderExpressionAndResult(expression, expression.length, 0, mode)
            return
        }

        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getExpressionParts(
            displayFragment, expression)

        // Prevent user from entering an operator as the first char in the equation
        if (leftOfCursor.isEmpty()) { return }

        // Prevent user from entering two operators in a row
        if (calcUtils.hasDoubleOperators(leftOfCursor, rightOfCursor)) { return }

        if (mode == "scientific") {
            // Prevent user from entering an operator to the right of an open bracket
            if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '(') { return }
        }

        // Add trailing 0 if user enters operator beside a decimal
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '.' && rightOfCursor.isEmpty()) {
            val zero = "0"
            expression = "$leftOfCursor$zero$operator"
            renderExpressionAndResult(expression, cursorPosition, 2, mode)
            return
        }

        // Update equation and render equation
        expression = "$leftOfCursor$operator$rightOfCursor"
        renderExpressionAndResult(expression, cursorPosition, 1, mode)
    }

    // Handle decimal clicks
    protected fun onDecimalClick(mode: String) {
        val decimal = "."
        if (isFinalResult) {
            if (result.contains('.')) return
            else expression = "$result$decimal"
        }

        isFinalResult = false
        var cursorOffset = 0

        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getExpressionParts(displayFragment, expression)

        // Prevent user from entering numbers with more than one decimal
        val testEquation = "$leftOfCursor$decimal$rightOfCursor"

        if (calcUtils.hasInvalidDecimal(testEquation)) {
            expression = "$leftOfCursor$rightOfCursor"
            renderExpressionAndResult(expression, cursorPosition, cursorOffset, mode)
            return
        } else {
            // Add leading 0 if needed
            val (decimalEquation, leadingZeroAdded) = calcUtils.getEquationWithDecimal(leftOfCursor, rightOfCursor)

            // Render equation and result
            cursorOffset = if (leadingZeroAdded) 2 else 1
            expression = decimalEquation
            renderExpressionAndResult(expression, cursorPosition, cursorOffset, mode)
        }
    }

    protected fun onSignClick(mode: String) {
        if (isFinalResult && result != "error") {
            if (result.startsWith("-")) expression = result.removePrefix("-")
            else expression = if (mode == "simple") "-$result" else "(-$result"
            isFinalResult = false
            renderExpressionAndResult(expression, expression.length, 0, mode)
            return
        }

        val (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getExpressionParts(displayFragment, expression)

        if (mode == "simple") {
            val (equationWithSign, cursorOffset) = calcUtils.getSimpleEquationWithSign(expression,
                leftOfCursor, rightOfCursor, cursorPosition)

            // Render equation and result
            expression = equationWithSign
            renderExpressionAndResult(expression, cursorPosition, cursorOffset, mode)
        }

        if (mode == "scientific") {
            val (equationWithSign, cursorOffset) = calcUtils.getScientificEquationWithSign(expression, leftOfCursor, rightOfCursor, cursorPosition)

            // Render equation and result
            expression = equationWithSign
            renderExpressionAndResult(expression, cursorPosition, cursorOffset, mode)
        }
    }

    protected fun onEqualClick(mode: String) {
        // Show toast message if equation is empty
        if (expression.isEmpty()) {
            fragUtils.showToast(requireContext(), "Please enter an expression")
            return
        }

        // If final character of the equation is an operator, show toast message
        if (expression.last() in setOf('+', '~', '×', '÷')) {
            fragUtils.showToast(requireContext(), "Invalid expression")
            return
        }

        isFinalResult = true
        calculate(expression, mode)
    }

    // Clear equation, result, and display
    protected fun onClearClick() {
        expression = ""
        result = ""
        isFinalResult = false
        displayFragment.renderExpression(expression, 0, 0)
        displayFragment.renderResult(result)
    }

    // MEMORY FUNCTIONS //
    fun onMemStore(mode: String) {
        var number = expression

        if (expression.isEmpty() && result.isEmpty()) { return }

        // If the result is final, store the result
        if (isFinalResult) number = result

        // Set isFinalResult to true so the result is rendered in the darker color
        isFinalResult = true

        val isValidNumber = memoryUtils.isNumber(number)
        if (isValidNumber) {
            memory = memoryUtils.getScientificNumber(number)
            fragUtils.showToast(requireContext(), "Memory updated")
            calculate(number, mode)
        }
        else fragUtils.showToast(requireContext(), "Memory can only store numbers")
    }

    fun onMemRecall(mode: String) {
        isFinalResult = false

        if (memory.isEmpty()) {
            fragUtils.showToast(requireContext(), "Memory is empty")
            return
        }

        if (expression.isNotEmpty()) {
            if (expression.last() in setOf('+', '~', '×', '÷', '.')) {
                expression = "${expression.dropLast(1)}+$memory"
            } else {
                expression = "$expression+$memory"
            }

            renderExpressionAndResult(expression, expression.length, 0, mode)
            return
        }

        expression = memory
        fragUtils.showToast(requireContext(), "Memory recalled")
        renderExpressionAndResult(expression, expression.length, 0, mode)
    }


    fun onMemOperation(mode: String, operator: String) {
        if (memory.isEmpty()) {
            fragUtils.showToast(requireContext(), "Memory is empty")
            return
        }

        // If equation and result are empty, but memory is not, start an equation with memory num
        if (expression.isEmpty() && result.isEmpty() && memory.isNotEmpty()) {
            isFinalResult = false
            expression = memory
            renderExpressionAndResult(expression, expression.length, 0, mode)
            return
        }

        // If a final result is displayed, add/subtract memory num to the result
        if (isFinalResult) {
            expression = "$result$operator$memory"
            isFinalResult = true
            renderExpressionAndResult(expression, expression.length, 0, mode)
            memory = result
            fragUtils.showToast(requireContext(), "Memory updated")
            return
        }

        // If equation is not empty, add/subtract memory num to the end of the equation
        isFinalResult = false
        var number = expression
        val isValidNumber = memoryUtils.isNumber(number)

        if (isValidNumber) {
            val simpleNumber = memoryUtils.getSimpleNumber(number)
            expression = "$simpleNumber$operator$memory"
            isFinalResult = true
            renderExpressionAndResult(expression, expression.length, 0, mode)
            memory = result
            fragUtils.showToast(requireContext(), "Memory updated")
        }
        else fragUtils.showToast(requireContext(), "Memory can only store numbers")
    }

    fun onMemClear() {
        isFinalResult = false
        memory = ""
        fragUtils.showToast(requireContext(), "Memory cleared")
    }
}