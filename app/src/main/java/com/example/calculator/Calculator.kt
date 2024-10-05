package com.example.calculator

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager

// This is a parent class to the SimpleCalculator and ScientificCalculator classes. It uses the
// MemoryCallback interface to handle communication
abstract class Calculator : Fragment() {
    protected lateinit var display: DisplayFragment
    var isFinalResult: Boolean = false
    var expression: String = ""
    var result: String = ""
    protected var memory: String = ""
    protected var decimalPlaces: Int = 10
    protected val memoryManager = Memory()
    protected val appUtils = AppUtils()
    protected val calcUtils = CalcUtils()
    protected val signUtils = SignToggleUtils()
    protected val simpleCalc = SimpleCalculation()
    protected val scientificCalc = ScientificCalculation()
    protected lateinit var sharedPreferences: SharedPreferences

    // Calculate result of an expression based on calculator mode. Simple mode calculates from
    // left to right (ignoring BEDMAS order of operations), while scientific mode uses BEDMAS.
    fun calculate(exp: String, mode: String, context: Context) {
        // Update decimal places if user changes it in settings
        updateDecimalPlaces()

        if (exp.isEmpty()) {
            result = ""
            displayResult()
            return
        }

        try {
            when (mode) {
            "simple" -> result = if (exp == "-") { "" }
                        else { simpleCalc.calculateLeftToRight(exp, context) }

            "scientific" -> result = if (exp in setOf("(-", "abs((-", "(")) { "" }
                            else { scientificCalc.calculateBedmas(exp, context) }

                else -> throw Exception("invalid mode")
            }
        } catch (e: Exception) { handleErrors(e.message, context) }

        Log.i("testcat", "RESULT: $result")

        if (result != "error" && result.isNotEmpty()) {
            result = calcUtils.formatResult(result, decimalPlaces)
        }
        displayResult()
    }

    private fun handleErrors(errorMsg: String?, context: Context) {
        when (errorMsg) {
            "invalid expression" -> { appUtils.showToast(context, "Invalid expression") }
            "max digits" -> { appUtils.showToast(context, "Maximum digits in result") }
            "divide by zero" -> { appUtils.showToast(context, "Cannot divide by zero") }
            else -> {
                Log.e("testcat", "Error: $errorMsg")
                appUtils.showToast(context, "Invalid operation")
            }
        }
        result = "error"
    }

    private fun displayResult() {
        if (isFinalResult) display.renderFinalResult(result)
        else display.renderResult(result)
    }

    fun updateDisplay() {
        display.renderExpression(expression, expression.length, 0)
        display.renderResult(result)
    }

    // Render the expression and calculated result in the display fragment
    protected fun calcResultAndRefreshDisplay(exp: String, curPos: Int, curOffset: Int, mode: String) {
        updateDecimalPlaces()
        display.renderExpression(exp, curPos, curOffset)

        if ((exp.isEmpty() && result.isEmpty()) || exp in setOf("-", "(-", "(")) {
            displayResult()
        } else {
            calculate(exp, mode, requireContext())
        }
    }

    // Handle number clicks
    protected fun onNumberClick(number: String, mode: String) {
        var newCurPos = 0

        if (isFinalResult) {
            expression = number
            newCurPos = expression.length
            isFinalResult = false
        } else {
            val (cursorPos, left, right) = calcUtils.getParts(display, expression)
            newCurPos = cursorPos

            if (left.isNotEmpty() && left.last() in "πe!") {
                val times = "×"
                expression = "$left$times$number$right"
                newCurPos += 2
            } else {
                expression = "$left$number$right"
                newCurPos += number.length
            }
        }

        calcResultAndRefreshDisplay(expression, newCurPos, 0, mode)
    }

    // Handle operator clicks
    protected fun onOperatorClick(operator: String, mode: String) {
        val (cursorPos, left, right) = calcUtils.getParts(display, expression)
        var newCurPos = cursorPos

        if (!calcUtils.isOperatorAllowed(left, right, mode)) { return }

        // Start new expression with result if user enters operator after a final result is shown
        if (isFinalResult && result != "error") {
            expression = "$result$operator"
            newCurPos = expression.length
            isFinalResult = false
        } else if (left.isNotEmpty() && left.last() == '.' && right.isEmpty()) {
            // Add trailing 0 if user enters an operator beside a decimal
            expression = calcUtils.addTrailingZero(left, operator)
            newCurPos += 2
        } else {
            expression = "$left$operator$right"
            newCurPos += 1
        }
        calcResultAndRefreshDisplay(expression, newCurPos, 0, mode)
    }

    // Handle decimal clicks
    protected fun onDecimalClick(mode: String) {
        val decimal = "."
        var newCurPos = 0

        // If user clicks decimal after final result is shown, start new expression with the
        // final result (checks that user does not enter more than one decimal point)
        if (isFinalResult) {
            when {
                result.contains('.') -> expression = result
                else -> expression = "$result$decimal"
            }
            newCurPos = expression.length
            isFinalResult = false
        } else {
            val (cursorPos, left, right) = calcUtils.getParts(display, expression)
            newCurPos = cursorPos

            // Prevent user from entering numbers with more than one decimal
            if (calcUtils.hasInvalidDecimal("$left$decimal$right")) {
                expression = "$left$right"
            } else {
                val (newExpression, leadingZeroAdded) = calcUtils.addLeadingZero(left, right)
                expression = newExpression
                newCurPos = if (leadingZeroAdded) { newCurPos + 2 } else { newCurPos + 1 }
            }
        }
        calcResultAndRefreshDisplay(expression, newCurPos, 0, mode)
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
        var newCurPos = 0

        // If user clicks sign after final result is shown, start new expression with the result,
        // changing its sign
        if (isFinalResult && result != "error") {
            expression = startExpressionWithSignedResult(result, mode)
            newCurPos = expression.length
            isFinalResult = false
        } else {
            val (curPos, left, right) = calcUtils.getParts(display, expression)
            newCurPos = curPos

            val (newExpression, offset) = signUtils.toggleSign(mode, expression, left, right, curPos)
            newCurPos = newCurPos + offset
            expression = newExpression
        }
        calcResultAndRefreshDisplay(expression, newCurPos, 0, mode)
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

    protected fun showErrorToastAndReturn() {
        appUtils.showToast(requireContext(), "Please clear calculator error")
        return
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


    // ---------------------------------------------------------------------
    // OVERRIDE METHODS - Handle preferences and state
    // ---------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        updateDecimalPlaces()
    }

    fun updateDecimalPlaces() {
        decimalPlaces = sharedPreferences.getString("decimal_places", "10")?.toIntOrNull() ?: 10
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("expression", expression)
        outState.putString("result", result)
        outState.putString("memory", memory)
        outState.putBoolean("isFinalResult", isFinalResult)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        if (savedInstanceState != null) {
            expression = savedInstanceState.getString("expression", "")
            result = savedInstanceState.getString("result", "")
            memory = savedInstanceState.getString("memory", "")
            isFinalResult = savedInstanceState.getBoolean("isFinalResult", false)
            updateDisplay()
        }
    }

    // Method to be defined in child classes
    abstract fun getMode(): String
}
