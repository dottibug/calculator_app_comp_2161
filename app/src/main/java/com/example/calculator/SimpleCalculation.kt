package com.example.calculator

import android.content.Context

// This class contains the logic for the simple calculator
// It calculates the result of the equation sequentially, from left to right, ignoring BEDMAS order of operations
class SimpleCalculation {
    private val calcUtils = CalcUtils()

    fun calculateLeftToRight(expression: String, context: Context): String {
        if (expression.isEmpty()) return ""

        val operators = setOf('+', '~', '×', '÷')

        var number = ""
        var operator = '+'
        var result = 0.0
        var hasDecimal = false

        // Iterate through the equation character by character to calculate the result
        for (char in expression) {
            when {
                // Build number string
                char.isDigit() || char == '-' -> number += char

                // Handle decimals: Add leading 0 before decimal if necessary and check if
                // the number already has a decimal
                char == '.' && number.isEmpty() -> number += "0."
                char == '.' && !hasDecimal -> {
                    number += char
                    hasDecimal = true
                }

                // Handle operators
                char in operators -> {
                    result = performOperation(result, number.toDouble(), operator)
                    operator = char
                    number = ""
                    hasDecimal = false // Reset for next number
                }
            }
        }

        // Calculate the final result (if the last char is NOT an operator)
        if (expression.last() !in operators) {
            result = performOperation(result, number.toDouble(), operator)
        }

        // Check number of digits in result
        if (calcUtils.hasTooManyDigits(result.toString(), 10)) {
            throw Exception("max digits")
        }

        return calcUtils.formatResult(result.toString(), 10)
    }

    private fun performOperation(result: Double, number: Double, operator: Char): Double {
        return when (operator) {
            '+' -> result + number
            '~' -> result - number
            '×' -> result * number
            '÷' -> if (number != 0.0) { result / number }
                else { throw Exception("divide by zero") }
            else -> result
        }
    }
}