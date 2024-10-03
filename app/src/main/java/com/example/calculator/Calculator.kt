package com.example.calculator

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment

// This is a parent class to the SimpleCalculator and ScientificCalculator classes. It uses the
// MemoryCallback interface to handle communication
abstract class Calculator : Fragment() {
    protected lateinit var display: DisplayFragment
    protected var isFinalResult: Boolean = false
    protected var expression: String = ""
    protected var result: String = ""
    protected var memory: String = ""
    protected var decimalPlaces: Int = 10
    protected val memoryManager = Memory()
    protected val appUtils = AppUtils()
    protected val calcUtils = CalcUtils()
    protected val simpleCalc = SimpleCalculation()
    protected val scientificCalc = ScientificCalculation()

    // Calculate result of an expression based on calculator mode. Simple mode calculates from
    // left to right (ignoring BEDMAS order of operations), while scientific mode uses BEDMAS.
    fun calculate(exp: String, mode: String, context: Context) {
        if (exp.isEmpty()) {
            result = ""
            displayResult()
            return
        }

        result = try{
            when (mode) {
            "simple" -> if (exp == "-") { "" }
                        else { simpleCalc.calculateLeftToRight(exp, context) }

            "scientific" -> if (exp in setOf("(-", "abs((-", "(")) { "" }
                            else { scientificCalc.calculateBedmas(exp, context) }

                else -> throw Exception("invalid mode")
            }
        } catch (e: Exception) {
            handleErrors(e.message, context)
        }

        Log.i("testcat", "result: $result")

        if (result != "error" && result.isNotEmpty()) {
            result = calcUtils.formatResult(result, decimalPlaces)
        }

        displayResult()
    }

    private fun handleErrors(errorMsg: String?, context: Context): String {
        when (errorMsg) {
            "invalid expression" -> { appUtils.showToast(context, "Invalid expression") }
            "max digits" -> { appUtils.showToast(context, "Max 12 digits in result") }
            "divide by zero" -> { appUtils.showToast(context, "Cannot divide by zero") }
            else -> {
                Log.e("testcat", "Error: $errorMsg")
                appUtils.showToast(context, "Invalid operation")
            }
        }
        return "error"
    }

    private fun displayResult() {
        if (isFinalResult) display.renderFinalResult(result)
        else display.renderResult(result)
    }

    // Render the expression and calculated result in the display fragment
    protected fun calcResultAndRefreshDisplay(exp: String, curPos: Int, curOffset: Int, mode: String) {
        display.renderExpression(exp, curPos, curOffset)

        if ((exp.isEmpty() && result.isEmpty()) || exp in setOf("-", "(-", "(")) {
            displayResult()
        } else {
            calculate(exp, mode, requireContext())
        }
    }

    // Handle number clicks
    protected fun onNumberClick(number: String, mode: String) {
        if (isFinalResult) expression = ""

        var (cursorPos, left, right) = calcUtils.getParts(display, expression)
        expression = "$left$number$right"

        val cursorOffset = if (isFinalResult) 0 else 1
        cursorPos = if (isFinalResult) 1 else cursorPos

        isFinalResult = false
        calcResultAndRefreshDisplay(expression, cursorPos, cursorOffset, mode)
    }

    // Handle operator clicks
    protected fun onOperatorClick(operator: String, mode: String) {
        var cursorOffset = 0
        var (cursorPos, left, right) = calcUtils.getParts(display, expression)

        if (!calcUtils.isOperatorAllowed(left, right, mode)) {
            return
        }

        // Start new expression with result if user enters operator after a final result is shown
        if (isFinalResult && result != "error") {
            expression = "$result$operator"
            cursorPos = expression.length
        }

        // Add trailing 0 if user enters an operator beside a decimal
        if (left.isNotEmpty() && left.last() == '.' && right.isEmpty()) {
            expression = calcUtils.addTrailingZero(left, operator)
            cursorOffset = 2
        } else {
            expression = "$left$operator$right"
            cursorOffset = 1
        }

        calcResultAndRefreshDisplay(expression, cursorPos, cursorOffset, mode)
    }

    // Handle decimal clicks
    protected fun onDecimalClick(mode: String) {
        val decimal = "."

        if (isFinalResult && result.contains('.')) {
            expression = result
        } else if (isFinalResult && !result.contains('.')) {
            expression = "$result$decimal"
        }

        isFinalResult = false

        var cursorOffset = 0
        val (cursorPos, left, right) = calcUtils.getParts(display, expression)

        // Prevent user from entering numbers with more than one decimal
        if (calcUtils.hasInvalidDecimal("$left$decimal$right")) {
            expression = "$left$right"
        } else {
            val (decimalExpression, leadingZeroAdded) = calcUtils.getDecimalExpression(left, right)
            expression = decimalExpression
            cursorOffset = if (leadingZeroAdded) 2 else 1
        }

        calcResultAndRefreshDisplay(expression, cursorPos, cursorOffset, mode)
    }

    private fun startExpressionWithSignedResult(result: String, mode: String): String {
        var signedResult = ""

        when (mode) {
            "simple" -> {
                if (result.startsWith("-")) signedResult = result.removePrefix("-")
                else signedResult = "-$result"
            }

            "scientific" -> {
                if (result.startsWith("(-")) signedResult = result.removePrefix("(-")
                else signedResult = "(-$result"
            }

            else -> {
                signedResult = ""
            }
        }
        return signedResult
    }

    protected fun onSignClick(mode: String) {
        var (curPos, left, right) = calcUtils.getParts(display, expression)

        // If user clicks sign after final result is shown, start new expression with the result,
        // changing its sign
        if (isFinalResult && result != "error") {
            expression = startExpressionWithSignedResult(result, mode)
            curPos = expression.length
            isFinalResult = false
        }

        val (newExpression, newCursorOffset) = calcUtils.toggleNegativeSign(mode, expression, left,
            right, curPos)

        expression = newExpression
        calcResultAndRefreshDisplay(expression, curPos, newCursorOffset, mode)
    }

    protected fun onEqualClick(mode: String) {
        // Show toast message if equation is empty
        if (expression.isEmpty()) {
            appUtils.showToast(requireContext(), "Please enter an expression")
            return
        }

        // If final character of the equation is an operator, show toast message
        if (expression.last() in setOf('+', '~', '×', '÷')) {
            appUtils.showToast(requireContext(), "Invalid expression")
            return
        }

        isFinalResult = true
        calcResultAndRefreshDisplay(expression, expression.length, 0, mode)
    }

    // Clear equation, result, and display
    protected fun onClearClick(mode: String) {
        expression = ""
        result = ""
        isFinalResult = false
        calcResultAndRefreshDisplay(expression, 0, 0, mode)
    }

    // MEMORY FUNCTIONS //
    fun onMemStore(mode: String) {
        val (response, memoryValue) = memoryManager.store(expression, result, isFinalResult)
        appUtils.showToast(requireContext(), response)
        if (response == "Memory updated") {
            isFinalResult = true
            calculate(memoryValue, mode, requireContext())
        }
    }

    fun onMemRecall(mode: String) {
        val (response, memoryValue) = memoryManager.recall()
        appUtils.showToast(requireContext(), response)
        if (response == "Memory recalled") {
            expression += memoryValue
            calcResultAndRefreshDisplay(expression, expression.length, 0, mode)
        }
    }

    fun onMemOperation(mode: String, operator: String) {
        val (response, newExpression) = memoryManager.operation(expression, result, operator,
            isFinalResult)

        if (response == "Memory updated" || response == "Memory recalled") {
            isFinalResult = if (response == "Memory updated") { true } else { false }
            calcResultAndRefreshDisplay(newExpression, newExpression.length, 0, mode)
            memory = result
        } else {
            isFinalResult = false
        }

        memoryManager.setMemory(result)
        expression = newExpression
        appUtils.showToast(requireContext(), response)
    }

    fun onMemClear() {
        val (response, memoryValue) = memoryManager.clear()
        memory = memoryValue
        isFinalResult = false
        appUtils.showToast(requireContext(), response)
    }
}