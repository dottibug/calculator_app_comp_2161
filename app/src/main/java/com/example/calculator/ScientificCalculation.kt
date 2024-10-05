package com.example.calculator

import android.content.Context

// This class contains the logic for the scientific calculator
// It calculates the result of the equation using BEDMAS order of operations
// NOTE: the operator × is not the letter x, it is the multiplication symbol
class ScientificCalculation {
    private val piValue = "3.1415"
    private val eulerValue = "2.7182"
    private val calcUtils = CalcUtils()

    fun calculateBedmas(expression: String, context: Context, decimalPlaces: Int): String {

        val cleanExpression = cleanExpression(expression)
        val postfixList = getPostfixList(cleanExpression, context)
        val result = calculatePostfix(postfixList)

        // Check number of digits in result
        if (calcUtils.hasTooManyDigits(result, 12, decimalPlaces)) {
            throw Exception("max digits")
        }

        return calcUtils.formatResult(result, decimalPlaces)

    }

    private fun performPostfixOperation(num1: Double, num2: Double, operator: String): String {
        val result = when (operator) {
            "+" -> num1 + num2
            "~" -> num1 - num2
            "×" -> num1 * num2
            "÷" -> if (num2 != 0.0) { num1 / num2 }
                else { throw Exception("divide by zero") }
            else -> "error"
        }
        return result.toString()
    }

    private fun calculatePostfix(postfix: MutableList<String>): String {
        val resultStack = mutableListOf<Double>()

        val numRegex = Regex("-?[0-9]+\\.?[0-9]*")

        for (token in postfix) {
            when {
                token.matches(numRegex) -> resultStack.add(token.toDouble())

                token in setOf("+", "~", "×", "÷") -> {
                    // Return the result if there is only one number in the stack
                    if (resultStack.size == 1) { return resultStack[0].toString() }

                    // If the result has more than one number, pop top two numbers from the stack
                    // and perform operation; push result to the stack. The top two numbers are
                    // taken in reverse order to perform division correctly
                    val num2 = resultStack.removeLast()
                    val num1 = resultStack.removeLast()
                    val result = performPostfixOperation(num1, num2, token)
                    resultStack.add(result.toDouble())
                }

                else -> return "error"
            }
        }

        if (resultStack.size != 1) return "error"
        else return resultStack[0].toString()
    }

    private fun getPostfixList(expression: String, context: Context): MutableList<String> {
        val output = mutableListOf<String>()
        val operators = mutableListOf<String>()

        // Parses numbers, operators, brackets, matching optional negative sign and/or decimals
        val pattern = Regex("(-?[0-9]+\\.?[0-9]*)|([+~×÷()])")

        pattern.findAll(expression).forEach { item ->
            val token = item.value

            when (token) {
                in setOf("+", "~", "×", "÷") -> handleOperator(token, output, operators)
                "(" -> operators.add(token)
                ")" -> handleClosedBracket(operators, output)
                else -> output.add(token) // Token is a number
            }
        }
        // As per shunting-yard algorithm, add any remaining operators to the output stack in
        // reverse order
        output.addAll(operators.reversed())
        return output
    }

    // Handle closing brackets as per shunting-yard algorithm
    // Pop operators from the operator stack to the output stack until an open bracket is found;
    // remove the open bracket from the operator stack
    private fun handleClosedBracket(operators: MutableList<String>, output: MutableList<String>) {
        while (operators.last() != "(") {
            output.add(operators.removeLast())
            if (operators.isEmpty()) { return }
        }
        operators.removeLast()
    }

    // Handle operators as per shunting-yard algorithm
    // If the operator stack is empty or the top operator is an open bracket, push the operator
    // to the operator stack. Otherwise, pop operators from the operator stack to the output
    // stack until an operator with an equal or lower precedence is found
    private fun handleOperator(token: String, output: MutableList<String>, operators:
    MutableList<String>) {
        while (operators.isNotEmpty() && operators.last() != "(" &&
            getPrecedence(operators.last()) >= getPrecedence(token)) {
            output.add(operators.removeLast())
        }
        operators.add(token)
    }

    private fun getPrecedence(operator: String): Int {
        return when (operator) {
            "÷", "×" -> 2
            "+", "~" -> 1
            else -> 0
        }
    }

    private fun cleanExpression(expression: String): String {
        // Remove any trailing operators (as they would cause a calculation error)
        var cleanExpression = expression.trimEnd('+', '~', '×', '÷')

        cleanExpression = pairBrackets(cleanExpression)
        if (cleanExpression == "error") { return "error" }

        // Replace constants (pi and euler) with their values
        cleanExpression = replaceConstants(cleanExpression)

        // Calculate factorials
        cleanExpression = calcFactorial(cleanExpression)

        return cleanExpression
    }

    private fun factorial(number: Int): Int {
        if (number == 0) { return 1 }
        return number * factorial(number - 1)
    }

    private fun calcFactorial(expression: String): String {
        if ("!" !in expression) { return expression }

        var newExpression = expression
        var result = 0

        val factorialRegex = Regex("""(-?\d+)!""")
        val numberRegex = Regex("""-?\d+""")

        val factorials = factorialRegex.findAll(expression)
        for (factorial in factorials) {
            val factorialString = factorial.value
            val number = numberRegex.find(factorialString)?.value?.toInt()

            if (number == null) { return "error" }

            if (number > 0) {
                result = factorial(number)
            } else {
                // If the number is negative, calculate factorial of the positive number, then
                // append the negative sign to the result
                val nonNegNum = number * -1
                result = factorial(nonNegNum)
                result *= -1
            }
            newExpression = newExpression.replace(factorialString, result.toString())
        }
        return newExpression
    }

    // Replaces constants (pi and euler) with their values
    private fun replaceConstants(expression: String): String {
        if ("π" !in expression && "e" !in expression) { return expression }
        return expression.replace("π", piValue).replace("e", eulerValue)
    }

    // Add any missing closed brackets to the expression to prevent a mismatched brackets error
    private fun pairBrackets(expression: String): String {
        val openCount = expression.count { it == '(' }
        val closedCount = expression.count { it == ')' }
        if (closedCount > openCount) { return "error" }

        if (openCount > closedCount) {
            val missingCount = openCount - closedCount
            val newExpression = expression + ")".repeat(missingCount)
            if (newExpression in listOf("()", "(-)", "×()")) { return "error" }
            return expression + ")".repeat(missingCount)
        }
        return expression
    }
}