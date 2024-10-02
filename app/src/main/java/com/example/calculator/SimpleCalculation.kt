package com.example.calculator

// This class contains the logic for the simple calculator
// It calculates the result of the equation sequentially, from left to right, ignoring BEDMAS order of operations
class SimpleCalculation {

    fun calculateLeftToRight(expression: String): String {
        if (expression.isEmpty()) return ""

        var number = ""
        var operator = '+'
        var result = 0.0

        val operators = setOf('+', '~', '×', '÷')

        // Iterate through the equation character by character to calculate the result
        for (char in expression) {
            when {
                // Build number string
                char.isDigit() || char == '.' || char == '-' -> number += char

                // Handle operators
                char in operators -> {
                    result = performOperation(result, number.toDouble(), operator)
                    operator = char
                    number = ""
                }
            }
        }

        // Calculate the final result (if the last char is NOT an operator)
        if (expression.last() !in operators) {
            result = performOperation(result, number.toDouble(), operator)
        }

        return result.toString()
    }

    private fun performOperation(result: Double, number: Double, operator: Char): Double {
        return when (operator) {
            '+' -> result + number
            '~' -> result - number
            '×' -> result * number
            '÷' -> if (number != 0.0) result / number else Double.NaN
            else -> result
        }
    }
}