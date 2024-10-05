package com.example.calculator

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.calculator.databinding.FragmentScientificCalculatorBinding

// NOTE: The scientific calculator uses BEDMAS order of operations to calculate the result
class ScientificCalculatorFragment : Calculator() {
    private lateinit var binding: FragmentScientificCalculatorBinding
    private val mode = "scientific"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentScientificCalculatorBinding.inflate(inflater, container, false)
        setupButtons()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        display = parentFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment

        if (savedInstanceState != null) {
            updateDisplay()
        }
    }

    override fun getMode(): String {
        return if (this is ScientificCalculatorFragment) "scientific" else "simple"
    }

    private fun appendToFinalResult(bracket: String): Boolean {
        if (isFinalResult && result != "error") {
            expression = "${result}${bracket}"
            isFinalResult = false
            calcResultAndRefreshDisplay(expression, expression.length, 0, mode)
            return true
        }
        return false
    }

    private fun appendToExpression(bracket: String, left: String, right: String, curPos: Int,
        offset: Int) {
        expression = "$left$bracket$right"
        calcResultAndRefreshDisplay(expression, curPos, offset, mode)
    }

    // Handle open bracket clicks
    private fun onOpenBracketClick() {
        if (appendToFinalResult("×(")) { return }

        val (curPos, left, right) = calcUtils.getParts(display, expression)

        // Prevent user from entering an opening bracket if there is an operator to the immediate right
        if (right.isNotEmpty() && right.first() in setOf('+', '~', '×', '÷')) { return }

        // Add a multiplication symbol if user enters an opening bracket to the immediate right
        // of a digit or to the immediate right of a closed bracket
        if (left.isNotEmpty() && (left.last().isDigit() || left.last() == ')')) {
            appendToExpression("×(", left, right, curPos, 2)
            return
        }
        appendToExpression("(", left, right, curPos, 1)
    }

    // Prevents user from entering a closed bracket:
    // 1. As the first char in an expression
    // 2. Immediately after an open bracket - ie. ()
    // 3. Immediately after an operator - ie. +)
    // 4. Immediately after a negative sign - ie. -)
    // 5. If the expression does not contain a matching open bracket
    private fun isInvalidClosedBracket(left: String): Boolean {
        val symbolSet = setOf('+', '~', '×', '÷', '(', '-')

        if (left.isEmpty() || (left.isNotEmpty() && left.last() in symbolSet)) { return true }

        // Check for matching open brackets. Add 1 to closed count as the just-clicked closed
        // bracket is not yet part of the expression
        val openCount = left.count { it == '(' }
        val closedCount = left.count { it == ')' } + 1

        return openCount < closedCount
    }

    // Handle close bracket clicks
    private fun onClosedBracketClick() {
        val (curPos, left, right) = calcUtils.getParts(display, expression)

        if (isInvalidClosedBracket(left)) { return }

        // Add trailing 0 if user enters closing bracket beside a decimal
        if (left.isNotEmpty() && left.last() == '.' && right.isEmpty()) {
            appendToExpression("0)", left, right, curPos, 2)
            return
        }

        appendToExpression(")", left, right, curPos, 1)
    }

    // Handle backspace click
    fun onBackspace() {
        val (curPos, left, right) = calcUtils.getParts(display, expression)

        // Guard clause if cursor is at beginning of equation (nothing to delete)
        if (expression.isEmpty() || curPos == 0) { return }

        // Delete the character at the cursor position
        val updatedLeft = left.dropLast(1)

        expression = "$updatedLeft$right"
        calcResultAndRefreshDisplay(expression, curPos - 1, 0, mode)
    }

    // -------------------------------------------------------
    // SCIENTIFIC BUTTONS
    // -------------------------------------------------------

    // Handle special number button clicks
    private fun onConstantClick(number: String) {
        val constantSymbol = if (number == "pi") "π" else "e"
        appendSymbolAndCalculate(constantSymbol)
    }

    // Handle factorial click
    private fun onFactorialClick() {
        val (curPos, left, right) = calcUtils.getParts(display, expression)

        // Check if the last character is already a factorial
        if (left.isNotEmpty() && left.last() == '!') {
            appUtils.showToast(requireContext(), "Invalid operation")
            return
        }

        // Extract the leftmost number from the left part
        val lastNumber = calcUtils.extractLeftmostNumber(left.reversed()).reversed()

        if (lastNumber.isEmpty() || lastNumber.contains(".")) {
            appUtils.showToast(requireContext(), "Factorial must be a positive integer")
            return
        }

        // Check if the right part starts with an operator or is empty
        if (right.isNotEmpty() && right.first() !in setOf('+', '~', '×', '÷')) {
            appUtils.showToast(requireContext(), "Invalid position for factorial")
            return
        }

        val number = lastNumber.toIntOrNull()
        if (number == null || number > 12) {  // 13! exceeds the calculator's max of 12 digits
            appUtils.showToast(requireContext(), "Number too large for factorial")
            return
        }

        appendSymbolAndCalculate("!")
    }

    private fun appendSymbolAndCalculate(symbol: String) {
        val (curPos, left, right) = calcUtils.getParts(display, expression)
        var newExpression = expression
        var newCurPos = curPos

        val multiply = "×"

        when (symbol) {
            // Factorial
            "!" -> {
                newExpression = "$left$symbol$right"
                newCurPos += 1
            }

            // Constant
            in listOf("π", "e") -> {
                // Add multiplication symbol if needed
                if (left.isNotEmpty() && right.isNotEmpty()) {
                    // If left and right chars are digits or specified symbols, add a
                    // multiplication symbol on each side of the constant
                    if ((left.last().isDigit() || left.last() in "πe)!.")
                        && (right.first().isDigit() || right.first() in "πe(.")) {
                        newExpression = "$left$multiply$symbol$multiply$right"
                        newCurPos += 3
                    }
                }
                // If only the left char is a digit or specified symbol, add a multiplication
                // symbol before the constant
                else if (left.isNotEmpty() && (left.last().isDigit() || left.last() in "πe)!.")) {
                    newExpression = "$left$multiply$symbol$right"
                    newCurPos += 2
                }

                // If only the right char is a digit or specified symbol, add a multiplication
                // symbol after the constant
                else if (right.isNotEmpty() && (right.first().isDigit() ||
                        right.first() in "πe(.")) {
                    newExpression = "$left$symbol$multiply$right"
                    newCurPos += 2
                }

                // If neither char is a digit or specified symbol, simply add the constant
                else {
                    newExpression = "$left$symbol$right"
                    newCurPos += 1
                }
            }
        }

        expression = newExpression
        calcResultAndRefreshDisplay(expression, newCurPos, 0, mode)
    }


    // -------------------------------------------------------
    // ERROR HANDLING
    // -------------------------------------------------------
    private fun handleErrors(errorMsg: String?, newResult: String) {
        when (errorMsg) {
            "invalid expression" -> appUtils.showToast(requireContext(), "Invalid expression")
            "max digits" -> appUtils.showToast(requireContext(), "Max 12 digits in result")
            "divide by zero" -> appUtils.showToast(requireContext(), "Cannot divide by zero")
            else -> {
                Log.e("testcat", "Error: $errorMsg")
                appUtils.showToast(requireContext(), "An error occurred: $errorMsg")
            }
        }
        result = newResult
        displayResult()
    }

    private fun displayResult() {
        if (isFinalResult) display.renderFinalResult(result)
        else display.renderResult(result)
    }

    // -------------------------------------------------------
    // BUTTON SETUP
    // -------------------------------------------------------
    private fun setupButtons() {
        setupNumberButtons()
        setupOperatorButtons()
        setupSpecialButtons()
        setupScientificButtons()
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
        binding.buttonBracketOpen.setOnClickListener { onOpenBracketClick() }
        binding.buttonBracketClose.setOnClickListener { onClosedBracketClick() }
    }

    private fun setupScientificButtons() {
        binding.buttonPi.setOnClickListener { onConstantClick("pi") }
        binding.buttonEuler.setOnClickListener { onConstantClick("euler") }
        binding.buttonFactorial.setOnClickListener { onFactorialClick() }
    }
}