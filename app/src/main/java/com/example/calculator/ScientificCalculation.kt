package com.example.calculator

import android.content.Context
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

// This class contains the logic for the scientific calculator
// It calculates the result of the equation using BEDMAS order of operations
// NOTE: the operator × is not the letter x, it is the multiplication symbol
class ScientificCalculation {
    private val piValue = "3.1415"
    private val eulerValue = "2.7182"
    private val calcUtils = CalcUtils()

    fun calculateBedmas(expression: String, context: Context): String {

        val cleanExpression = cleanExpression(expression)
        val postfixList = getPostfixList(cleanExpression, context)
        val result = calculatePostfix(postfixList)

        // Check number of digits in result
        if (calcUtils.hasTooManyDigits(result, 10)) {
            throw Exception("max digits")
        }

        return calcUtils.formatResult(result, 10)

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

        // Calculate trig functions, square roots, exponents, absolutes, and factorials
        cleanExpression = calcSpecialFunctions(cleanExpression)

        return cleanExpression
    }

    // Calculate trig functions, square roots, exponents, and factorials
    private fun calcSpecialFunctions(expression: String): String {
        var newExpression = expression

        newExpression = calcExponent(newExpression)
        if (newExpression == "error") { return "error" }

        newExpression = calcAbsValue(newExpression)
        if (newExpression == "error") { return "error" }

        newExpression = calcSquareRoot(newExpression)
        if (newExpression == "error") { return "error" }

        newExpression = calcFactorial(newExpression)
        if (newExpression == "error") { return "error" }

        newExpression = calcTrig(newExpression)
        if (newExpression == "error") { return "error" }

        return newExpression
    }

    private fun calcTrig(expression: String): String {
        if ("sin" !in expression && "cos" !in expression && "tan" !in expression) {
            return expression
        }

        var newExpression = expression

        val trigRegex = Regex("""(sin|cos|tan)\(\d+(\.\d+)?\)""")
        val numberRegex = Regex("""\d+(\.\d+)?""")

        val trigFunctions = trigRegex.findAll(expression)
        for (trigFunction in trigFunctions) {
            val trig = trigFunction.value
            val number = numberRegex.find(trig)?.value?.toDouble()

            if (number == null) { return "error" }

            // Kotlin sin, cos, and tan functions use radians
            val radians = toRadians(number)
            val result = when {
                trig.startsWith("sin") -> sin(radians)
                trig.startsWith("cos") -> cos(radians)
                trig.startsWith("tan") -> tan(radians)
                else -> 0.0
            }

            val formattedResult = formatShortResult(result.toString())
            newExpression = newExpression.replace(trig, formattedResult)
        }
        return newExpression
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

    private fun calcSquareRoot(expression: String): String {
        if ("√" !in expression) { return expression }

        var newExpression = expression

        val squareRootRegex = Regex("""√\((\d+(\.\d+)?)\)""")
        val numberRegex = Regex("""\d+(\.\d+)?""")

        val squareRoots = squareRootRegex.findAll(expression)
        for (squareRoot in squareRoots) {
            val squareRootString = squareRoot.value
            val number = numberRegex.find(squareRootString)?.value?.toDouble()

            if (number == null || number < 0) { return "error" }

            val result = sqrt(number)
            val formattedResult = formatShortResult(result.toString())
            newExpression = newExpression.replace(squareRootString, formattedResult)
        }

        return newExpression
    }

    private fun calcAbsValue(expression: String): String {
        if ("|" !in expression) { return expression }

        var newExpression = expression
        val absRegex = Regex("""\|([^|]+)\|""")

        val absValues = absRegex.findAll(newExpression)
        for (absValue in absValues) {
            val abs = absValue.value
            val content = absValue.groupValues[1]

            try {
                // Recursively calculate the content inside the absolute value
                val innerResult = calculateBedmas(content, Context)
                val number = innerResult.toDouble()
                val result = abs(number)
                val formattedResult = formatShortResult(result.toString())
                newExpression = newExpression.replace(abs, formattedResult)
            } catch (e: Exception) {
                // If there's an error calculating the inner expression, return the original expression
                return expression
            }
        }

        // If there are any unclosed absolute value symbols, return the original expression
        if (newExpression.count { it == '|' } % 2 != 0) {
            return expression
        }

        return newExpression
    }

    private fun calcExponent(expression: String): String {
        if ("^" !in expression) { return expression }

        var newExpression = expression

        val exponentRegex = Regex("""-?\d+(?:\.\d+)?\^\(\d+\)""")
        val baseRegex = Regex(""".*(?=\^)""")

        val exponents = exponentRegex.findAll(newExpression)
        for (exponent in exponents) {
            val exponentString = exponent.value
            val base = baseRegex.find(exponentString)?.value?.toDouble()
            val power = if (exponentString.contains("^(2)")) 2 else 3
            val result = base?.pow(power).toString()
            val formattedResult = formatShortResult(result)
            newExpression = newExpression.replace(exponentString, formattedResult)
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

    private fun formatShortResult(result: String): String {
        val formattedResult = "%.4f".format(result)
        return formattedResult
    }
}