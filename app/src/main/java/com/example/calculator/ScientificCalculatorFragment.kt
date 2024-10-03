package com.example.calculator

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.calculator.databinding.FragmentScientificCalculatorBinding

// TODO GET DECIMAL PLACES FROM USER SETTINGS WHEN IMPLEMENTED (pass to calculateBEDMAS
//  functions)

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

        // If the right of cursor is a decimal, add a leading zero
//        if (updatedRight.isNotEmpty() && updatedRight.first() == '.' && updatedLeft.last()
//            in setOf('+', '~', '×', '÷')) {
//            updatedRight = "0$updatedRight"
//        }

        expression = "$updatedLeft$right"
        calcResultAndRefreshDisplay(expression, curPos - 1, 0, mode)
    }

    // -------------------------------------------------------
    // SCIENTIFIC BUTTONS
    // -------------------------------------------------------

    private val addMultSymbolToLeftPart = setOf(")", "π", "e", "!", "^(2)", "^(3)")
    private val addMultSymbolToRightPart = setOf("(", "√(", "π", "e", "abs(", "sin(", "cos(", "tan(")

    private fun appendSymbol2(symbol: String) {
        var newSymbol = symbol
        val (curPos, left, right) = calcUtils.getParts(display, expression)

        if (left.isNotEmpty() && (left.last().isDigit() || addMultSymbolToLeftPart.any { left.endsWith(it) })) {
            if (newSymbol in setOf("π", "e", "sin(", "cos(", "tan(", "abs(", "√(")) {
                newSymbol = "×$newSymbol"
            }
        }

        expression = "$left$newSymbol$right"
        val newCurPos = curPos + newSymbol.length
        display.renderExpression(expression, newCurPos, 0)
    }


    // Returns true if the input ends with any of the math symbols
    private fun appendMultiplySymbol(symbol: String, left: String, right: String): String {

        var newSymbol = symbol
        val exponents = setOf("^(2)", "^(3)")

        if (left.isNotEmpty() && (left.last().isDigit() || addMultSymbolToLeftPart.any { left.endsWith(it) })) {
            if (newSymbol in setOf("π", "e", "sin(", "cos(", "tan(", "abs(", "√(")) {
                newSymbol = "×$newSymbol"
            }
        }

        if (right.isNotEmpty() && (right.first().isDigit() || addMultSymbolToRightPart.any
            { right.startsWith(it) })) {
            newSymbol = "$newSymbol×"
        }
        return newSymbol
    }

//    private fun appendSymbolAndCalculate(symbol: String) {
//        val (curPos, left, right) = calcUtils.getParts(display, expression)
////        val newSymbol = appendMultiplySymbol(symbol, left, right)
//        expression = "$left$symbol$right"
//        val newCurPos = curPos + symbol.length
//        calcResultAndRefreshDisplay(expression, newCurPos, 0, mode)
//    }

    // Handle special number button clicks
    private fun onConstantClick(number: String) {
        val constantSymbol = if (number == "pi") "π" else "e"
        appendSymbolAndCalculate(constantSymbol)
    }

    // Handle trig function click
    private fun onTrigClick(trigFunction: String) {
        var trigSymbol = ""
        when (trigFunction) {
            "sin" -> trigSymbol = "sin("
            "cos" -> trigSymbol = "cos("
            "tan" -> trigSymbol = "tan("
        }
//        appendSymbol(trigSymbol)
    }

    // Handle square root click
    private fun onSquareRootClick() {
        val squareRoot = "√("
//        appendSymbol(squareRoot)
    }

    // Handle exponent click
    private fun onExponentClick(exponent: String) {
        val exponentSymbol = if (exponent == "square") { "^(2)" } else { "^(3)" }
        appendSymbolAndCalculate(exponentSymbol)
    }

    private fun onAbsClick() {
        val (curPos, left, right) = calcUtils.getParts(display, expression)

        if (right.isEmpty() || !right.contains('|')) {
            expression = "$left|$right"
            val newCurPos = curPos + 1
            display.renderExpression(expression, newCurPos, 0)
        } else {
            appendSymbolAndCalculate("|")
        }
    }

    // Handle absolute value click
    private fun onAbsClick2() {
        val (curPos, left, right) = calcUtils.getParts(display, expression)

        // Allows user to close the absolute value function
        if (left.count { it == '|' } % 2 != 0) {
            appendSymbolAndCalculate("|")
            return
        }

        // Check for invalid position to put absolute value symbol
        if (left.isNotEmpty()) {
            val last = left.last()
            val invalidChars = setOf(')', '!', 'e', 'π')
            val invalidEndings = listOf("sin(", "cos(", "tan(", "^(2)", "^(3)", "√(")

            // Add multiplication symbol next to invalid character or endings
            if (last.isDigit() || last in invalidChars || invalidEndings.any { left.endsWith(it) }) {
                appendSymbolAndCalculate("×|")
                return
            }
        }

        appendSymbolAndCalculate("|")
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

        // Handle different types of symbols
        when {
            // For postfix operators like factorial
            symbol == "!" -> {
                newExpression = "$left$symbol$right"
                newCurPos += 1
            }
            // For prefix functions like sin, cos, tan, abs, sqrt
            symbol in listOf("sin(", "cos(", "tan(", "abs(", "√(") -> {
                // Add multiplication symbol if needed
                if (left.isNotEmpty() && (left.last().isDigit() || left.last() in "πe)")) {
                    newExpression = "$left×$symbol$right"
                    newCurPos += symbol.length + 1
                } else {
                    newExpression = "$left$symbol$right"
                    newCurPos += symbol.length
                }
            }
            // For constants like π and e
            symbol in listOf("π", "e") -> {
                // Add multiplication symbol if needed
                if (left.isNotEmpty() && (left.last().isDigit() || left.last() in "πe)")) {
                    newExpression = "$left×$symbol$right"
                    newCurPos += 2
                } else {
                    newExpression = "$left$symbol$right"
                    newCurPos += 1
                }
            }
            // For infix operators like ^
            symbol.startsWith("^") -> {
                newExpression = "$left$symbol$right"
                newCurPos += symbol.length
            }
            // Default case for other symbols
            else -> {
                newExpression = "$left$symbol$right"
                newCurPos += symbol.length
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
        binding.buttonSin.setOnClickListener { onTrigClick("sin") }
        binding.buttonCos.setOnClickListener { onTrigClick("cos") }
        binding.buttonTan.setOnClickListener { onTrigClick("tan") }
        binding.buttonSquareRoot.setOnClickListener { onSquareRootClick() }
        binding.buttonSquare.setOnClickListener { onExponentClick("square") }
        binding.buttonCube.setOnClickListener { onExponentClick("cube") }
        binding.buttonAbs.setOnClickListener { onAbsClick() }
        binding.buttonFactorial.setOnClickListener { onFactorialClick() }
    }
}