package com.example.calculator

import kotlin.reflect.KFunction2

// Common utilities shared between the simple and scientific calculator fragments
class CalculatorUtilities {
    private val fragUtils = FragmentUtilities()

    // ----------------------------------------------------------------------------------------------
    // CLICK LISTENERS
    // ----------------------------------------------------------------------------------------------

    // Number click listeners
    fun setupNumberClickListeners(numberButtons: List<ButtonData>, onClick: KFunction2<String, String, Unit>) {
        numberButtons.forEach { buttonData ->
            buttonData.button.setOnClickListener { onClick(buttonData.value, buttonData.mode) }
        }
    }

    // Operator click listeners
    fun setupOperatorClickListeners(operatorButtons: List<ButtonData>, onClick: KFunction2<String, String, Unit>) {
        operatorButtons.forEach { buttonData ->
            buttonData.button.setOnClickListener { onClick(buttonData.value, buttonData.mode) }
        }
    }

    // ----------------------------------------------------------------------------------------------
    // CALCULATE FUNCTIONS
    // ----------------------------------------------------------------------------------------------

    // Check if the user has entered two operators in a row
    fun hasDoubleOperators(leftOfCursor: String, rightOfCursor: String) : Boolean {
        val charLeftOfCursor = leftOfCursor.lastOrNull()
        val charRightOfCursor = rightOfCursor.firstOrNull()
        if (charLeftOfCursor in setOf('+', '~', '×', '÷') || charRightOfCursor in setOf('+', '~', '×', '÷')) {
            return true
        }
        return false
    }

    // Add leading 0 to decimal if needed
    fun getEquationWithDecimal(leftOfCursor: String, rightOfCursor: String) : Pair<String,
            Boolean> {
        var leadingZeroAdded = false
        var decimalEquation = ""

        if (leftOfCursor.isEmpty() || leftOfCursor.last() in setOf('+', '~', '×', '÷', '(')) {
            leadingZeroAdded = true
            val leadingZero = "0."
            decimalEquation = "$leftOfCursor$leadingZero$rightOfCursor"
        } else {
            decimalEquation = "$leftOfCursor.$rightOfCursor"
        }
        return Pair(decimalEquation, leadingZeroAdded)
    }

    // Check if the user has entered a number that contains more than one decimal
    fun hasInvalidDecimal(equation: String) : Boolean {
        val numbers = equation.split(Regex("[$+~×÷]"))

        numbers.forEach {
            val decimalCount = it.count{char -> char == '.'}
            if (decimalCount > 1) { return true }
        }
        return false
    }

    // Check if a string is a number (contains no operators)
    private fun isNumber(equation: String) : Boolean {
        if (equation.contains(Regex("[$+~×÷]"))) { return false } else { return true }
    }

    // Check if a number is negative
    private fun isNegativeNumber(equation: String) : Boolean {
        if (equation.startsWith("(-") || equation.startsWith("-")) {
            return true
        } else {
            return false
        }
    }

    // ----------------------------------------------------------------------------------------------
    // SIMPLE CALCULATOR FUNCTIONS
    // ----------------------------------------------------------------------------------------------
    // Calculates the result of an equation sequentially, from left to right, ignoring BEDMAS order of operations
    fun calculateLeftToRight(equation: String) : String {
        // Guard clause for an empty equation
        if (equation.isEmpty()) return ""

        var number = ""
        var operator = '+'
        var result = 0.0

        // Iterate through the equation character by character to calculate the result
        for (i in equation.indices) {
            // Handle numbers and decimal points
            if (equation[i].isDigit() || equation[i] == '.' || equation[i] == '-') {
                number += equation[i]
            }

            // Handle operators and operation
            if (equation[i] in setOf('+', '~', '×', '÷')) {
                val num = number.toDoubleOrNull() ?: 0.0
                when (operator) {
                    '+' -> result += num
                    '~' -> result -= num
                    '×' -> result *= num
                    '÷' -> if (num != 0.0) result /= num else return Double.NaN.toString()
                }
                operator = equation[i]
                number = ""
            }
        }

        // Return result if the last character is an operator
        if (equation.last() in setOf('+', '~', '×', '÷')) {
            return result.toString()
//            val formattedResult = String.format(Locale.CANADA,"%.${decimalPlaces}f", result).trimEnd('0').trimEnd('.')
//            return formattedResult
        }

        // If the last character is not an operator, calculate the final result
        val num = number.toDoubleOrNull() ?: 0.0
        when (operator) {
            '+' -> result += num
            '~' -> result -= num
            '×' -> result *= num
            '÷' -> if (num != 0.0) result /= num else return Double.NaN.toString()
        }

//        val formattedResult = String.format(Locale.CANADA,"%.${decimalPlaces}f", result).trimEnd('0').trimEnd('.')
//        return formattedResult
        return result.toString()
    }

    // Toggle negative sign in simple equations (uses brackets)
    fun getSimpleEquationWithSign(equation: String, leftOfCursor: String, rightOfCursor:
    String, cursorPosition: Int) : Pair<String, Int> {

        // Prevent user from entering a negative sign at the end of an equation if the preceding
        // character is a digit
        if (rightOfCursor.isEmpty() && leftOfCursor.isNotEmpty() && leftOfCursor.last().isDigit()) {
            return Pair(equation, 0)
        }

        // Start equation with a negative sign if the equation is empty
        if (equation.isEmpty()) { return Pair("-", 1) }

        // Remove negative sign if the equation is only a negative sign
        if (equation == "-") { return Pair("", -1) }

        // Toggle negative sign if the equation is only a number (ie. it does not have operators)
        if (isNumber(equation)) {
            if (isNegativeNumber(equation)) {
                // Toggle to positive number
                return Pair(equation.removePrefix("-"), -1)
            } else {
                // Toggle to negative number
                return Pair("-$equation", 1)
            }
        }

        // Add negative sign when the preceding character is an operator
        if (leftOfCursor.isNotEmpty() && (leftOfCursor.last() in setOf('+', '~', '×', '÷')
                    || leftOfCursor.endsWith("0."))) {

            if (leftOfCursor.endsWith("0.")) {
                val negativeSign = "-0."
                val equationWithSign = "${leftOfCursor.removeSuffix("0.")}$negativeSign$rightOfCursor"
                return Pair(equationWithSign, 1)
            } else {
                val negativeSign = "-"
                val equationWithSign = "$leftOfCursor$negativeSign$rightOfCursor"
                return Pair(equationWithSign, 1)
            }
        }

        // Remove negative sign when the preceding character is an operator
        if (leftOfCursor.isNotEmpty() && (leftOfCursor.endsWith("+-") ||
                    leftOfCursor.endsWith("~-") || leftOfCursor.endsWith("×-") ||
                    leftOfCursor.endsWith("÷-"))) {
            val equationWithSign = "${leftOfCursor.removeSuffix("-")}$rightOfCursor"
            return Pair(equationWithSign, -1)
        }

        // Toggle sign of number if the preceding character is a number
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last().isDigit()) {
            val lastOperatorIndex = leftOfCursor.lastIndexOfAny(charArrayOf('+', '~', '×', '÷'))
            val numberSubstring = leftOfCursor.substring(lastOperatorIndex + 1, cursorPosition)

            if (numberSubstring.startsWith('-')) {
                val updatedNumberSubstring = numberSubstring.removePrefix("-")
                val updatedLeftOfCursorString = leftOfCursor.replaceRange(lastOperatorIndex + 1,
                    cursorPosition, updatedNumberSubstring)
                val equationWithSign = "$updatedLeftOfCursorString$rightOfCursor"
                return Pair(equationWithSign, -1)
            } else {
                val updatedNumberSubstring = "-$numberSubstring"
                val updatedLeftOfCursorString = leftOfCursor.replaceRange(lastOperatorIndex + 1,
                    cursorPosition, updatedNumberSubstring)
                val equationWithSign = "$updatedLeftOfCursorString$rightOfCursor"
                return Pair(equationWithSign, 1)
            }
        }
        return Pair("", 0)
    }

    // ----------------------------------------------------------------------------------------------
    // SCIENTIFIC CALCULATOR FUNCTIONS
    // ----------------------------------------------------------------------------------------------

    // Calculate the result of the equation using BEDMAS order of operations
    // Use the shunting-yard algorithm to calculate the result of the equation
    // NOTE Reference for algorithm: https://brilliant.org/wiki/shunting-yard-algorithm/
    fun calculateBEDMAS(equation: String) : String {

        var equationToCalculate = equation

        if (equation.last() in setOf('+', '~', '×', '÷')) {
            equationToCalculate = equation.removeSuffix(equation.last().toString())
        }

        val pairedBracketEquation = getEquationWithPairedBrackets(equationToCalculate)

        if (pairedBracketEquation == "()" || pairedBracketEquation == "(-)" || pairedBracketEquation.endsWith("×(")) {
            return ""}

        val postfixExpression = getPostfixExpression(pairedBracketEquation)

        // Check for an empty postfix expression (return error if empty)
        if (postfixExpression.isEmpty()) { return "error" }

        else {
            val result = try { evaluatePostfix(postfixExpression) }
            catch (e: Exception) { return "error" }
            return result
        }
    }

    private fun getEquationWithPairedBrackets(equation: String) : String {
        // If the equation contains one less closing bracket than an open bracket, add a closing
        // bracket to the equation to prevent a mismatched brackets error
        val openBracketCount = equation.count { it == '(' }
        val closeBracketCount = equation.count { it == ')' }
        if (openBracketCount > closeBracketCount) { return "$equation)" }
        if (closeBracketCount > openBracketCount) { return "error" }
        return equation
    }

    // Get the postfix expression of the equation
    // NOTE: × is not the letter x, it is the multiplication symbol
    private fun getPostfixExpression(equation: String) : MutableList<String> {
        val output : MutableList<String> = mutableListOf()
        val operators : MutableList<String> = mutableListOf()

        // Parses numbers, operators, and brackets, matching optional negative sign and/or decimal
        val regex = Regex("(-?[0-9]+\\.?[0-9]*)|([+~×÷()])")

        // Iterate through the matched pattern to create a postfix expression in the correct order
        regex.findAll(equation).forEach { item ->
            val token = item.value

            when {
                // --- Number: Push token to the output stack
                token.matches(Regex("-?[0-9.]+")) -> { output.add(token) }

                // --- Open bracket: Push token to the operator stack
                token == "(" -> { operators.add(token) }

                // --- Close bracket: Pop operators from the operator stack to the output stack
                // until an open bracket is found. Remove the open bracket from the operator stack
                token == ")" -> {
                    while (operators.last() != "(") {
                        output.add(operators.removeLast())
                        // There are mismatched brackets if the operator stack is empty
                        if (operators.isEmpty()) { return mutableListOf() }
                    }
                    operators.removeLast()
                }

                // --- Operator: If operator stack is empty or the top operator is an open
                // bracket, push operator to the operator stack. Otherwise, pop operators
                // from  the  operator  stack  to  the  output  stack  until  an  operator  with
                // an  equal  or  lower  precedence is  found
                token in setOf("+", "~", "×", "÷") -> {
                    while (operators.isNotEmpty() && operators.last() != "(" &&
                        getPrecedence(operators.last()) >= getPrecedence(token)) {
                        output.add(operators.removeLast())
                    }
                    operators.add(token)
                }
            }
        }
        // Add any remaining entries in the operator stack to the output stack
        while (operators.isNotEmpty()) { output.add(operators.removeLast()) }
        return output
    }

    // Calculate result of a postfix expression
    // NOTE: × is not the letter x, it is the multiplication symbol
    private fun evaluatePostfix(postfix: MutableList<String>) : String {
        val stack = mutableListOf<String>()

        postfix.forEach { token ->
            if (token in setOf("+", "~", "×", "÷")) {
                // Guard clause to check for at least two numbers in the stack
                if (stack.size == 1) { return stack[0] }

                // Pop top two numbers from the stack, perform operation, and push result back
                // onto the stack (the top two numbers are popped in reverse order and removed from
                // the stack after the operation is performed)
                val num2 = stack.removeLast()
                val num1 = stack.removeLast()

                // Matches numbers with an optional negative sign and/or decimal point
                val numberPattern = "-?[0-9]+\\.?[0-9]*"

                if (num1.matches(Regex(numberPattern)) && num2.matches(Regex(numberPattern))) {
                    val result = when (token) {
                        "+" -> num1.toDouble() + num2.toDouble()
                        "~" -> num1.toDouble() - num2.toDouble()
                        "×" -> num1.toDouble() * num2.toDouble()
                        "÷" -> if (num2.toDouble() != 0.0) num1.toDouble() / num2.toDouble() else return Double.NaN.toString()
                        else -> { return "error" }
                    }
                    stack.add(result.toString())
                } else { return "error" }
            } else { stack.add(token) }
        }

        // Return formatted result to the specified number of decimal places
        if (stack.size == 1) {
            val result = stack.last().toDouble()
//            val formattedResult = String.format(Locale.CANADA,"%.${decimalPlaces}f", result).trimEnd('0')
//                .trimEnd('.')
//            return formattedResult
            return result.toString()
        } else { return "error" }
    }

    // Evaluate the precedence of an operator according to BEDMAS rules
    // NOTE: × is not the letter x, it is the multiplication symbol
    private fun getPrecedence(operator: String) : Int {
        return when (operator) {
            "÷", "×" -> 2
            "+", "~" -> 1
            else -> 0
        }
    }

    // Toggle negative sign in scientific equations (uses brackets)
    fun getScientificEquationWithSign(equation: String, leftOfCursor: String, rightOfCursor:
    String, cursorPosition: Int) : Pair<String, Int> {

        // Prevent user from entering a negative sign at the end of an equation if the preceding
        // character is a digit or a closed bracket
        if (rightOfCursor.isEmpty() && leftOfCursor.isNotEmpty() && (leftOfCursor.last().isDigit
                () || leftOfCursor.last() == ')')) {
            return Pair(equation, 0)
        }

        // Start equation with a negative sign if the equation is empty
        if (equation.isEmpty()) { return Pair("(-", 2) }

        // Remove negative sign if the equation is only a negative sign
        if (equation == "(-") { return Pair("", -2) }

        // Toggle negative sign if first character in equation is an open bracket
        if (equation == "(") {
            val negativeSign = "-"
            val equationWithSign = "$equation$negativeSign"
            return Pair(equationWithSign, 1)
        }

        // Toggle negative sign if the equation is only a number (ie. it does not have operators)
        if (isNumber(equation)) {
            if (isNegativeNumber(equation)) {
                // Toggle to positive number
                return Pair(equation.removePrefix("(-"), -2)
            } else {
                // Toggle to negative number
                return Pair("(-$equation", 2)
            }
        }

        // Add negative sign when the preceding character is an operator
        if (leftOfCursor.isNotEmpty() && (leftOfCursor.last() in setOf('+', '~', '×', '÷')
                    || leftOfCursor.endsWith("0."))) {

            if (leftOfCursor.endsWith("0.")) {
                val negativeSign = "(-0."
                val equationWithSign = "${leftOfCursor.removeSuffix("0.")}$negativeSign$rightOfCursor"
                return Pair(equationWithSign, 2)
            } else {
                val negativeSign = "(-"
                val equationWithSign = "$leftOfCursor$negativeSign$rightOfCursor"
                return Pair(equationWithSign, 2)
            }
        }

        // Remove negative sign when the preceding character is an operator
        if (leftOfCursor.isNotEmpty() && (leftOfCursor.endsWith("+(-") ||
                    leftOfCursor.endsWith("~(-") || leftOfCursor.endsWith("×(-") ||
                    leftOfCursor.endsWith("÷(-"))) {
            val equationWithSign = "${leftOfCursor.removeSuffix("(-")}$rightOfCursor"
            return Pair(equationWithSign, -2)
        }

        // Toggle negative sign if the preceding character is an open bracket
        if (leftOfCursor.isNotEmpty() && (leftOfCursor.endsWith("(") || leftOfCursor.endsWith("(-"))) {
            if (leftOfCursor.last() == '-') {
                // remove the negative sign
                val equationWithSign = "${leftOfCursor.removeSuffix("-")}$rightOfCursor"
                return Pair(equationWithSign, -1)
            } else {
                // add the negative sign
                val negativeSign = "-"
                val equationWithSign = "$leftOfCursor{$negativeSign}$rightOfCursor"
                return Pair(equationWithSign, 1)
            }
        }

        // Toggle sign of number if the preceding character is a number
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last().isDigit()) {
            val lastOperatorIndex = leftOfCursor.lastIndexOfAny(charArrayOf('+', '~', '×', '÷'))
            val numberSubstring = leftOfCursor.substring(lastOperatorIndex + 1, cursorPosition)

            if (numberSubstring.startsWith('-')) {
                val updatedLeftOfCursorString = leftOfCursor.replaceRange(lastOperatorIndex + 1,
                    cursorPosition, numberSubstring.removePrefix("-"))
                val equationWithSign = "$updatedLeftOfCursorString$rightOfCursor"
                return Pair(equationWithSign, -1)
            } else if (numberSubstring.startsWith("(-")) {
                val updatedLeftOfCursorString = leftOfCursor.replaceRange(lastOperatorIndex + 1, cursorPosition,
                    numberSubstring.removePrefix("(-"))
                val equationWithSign = "$updatedLeftOfCursorString$rightOfCursor"
                return Pair(equationWithSign, -2)
            } else {
                val updatedNumberSubstring = "(-$numberSubstring"
                val updatedLeftOfCursorString = leftOfCursor.replaceRange(lastOperatorIndex + 1,
                    cursorPosition, updatedNumberSubstring)
                val equationWithSign = "$updatedLeftOfCursorString$rightOfCursor"
                return Pair(equationWithSign, 2)
            }
        }
        return Pair("", 0)
    }
}