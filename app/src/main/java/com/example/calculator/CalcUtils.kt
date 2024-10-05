package com.example.calculator

import java.util.Locale

class CalcUtils {
    fun isNumber(number: String): Boolean {
        if (number.isEmpty()) return false
        if (number == "-" || number == "(" || number == "(-") return false
        return !number.contains(Regex("[$+~×÷]"))
    }

    fun formatResult(value: String, decimalPlaces: Int): String {
        if (value.isEmpty()) return ""
        return String.format(Locale.CANADA, "%.${decimalPlaces}f", value.toDouble())
            .trimEnd('0')
            .trimEnd('.')
    }

    fun hasTooManyDigits(number: String, maxDigits: Int, decimalPlaces: Int): Boolean {
       // Format result to the number of decimal places then count the digits
        val formattedNumber = formatResult(number, decimalPlaces)
        val digitCount = formattedNumber.count { it.isDigit() }
        return digitCount > maxDigits
    }

    private fun removeBrackets(input: String): String {
        val regex = """[()]+""".toRegex()
        return input.replace(regex, "")
    }

    fun getSimpleNumber(number: String): String {
        var simpleNumber = number
        if (simpleNumber.endsWith(".")) {
            simpleNumber = simpleNumber.dropLast(1)
        }
        return simpleNumber
    }

    fun getScientificNumber(number: String): String {
        var scientificNumber = number

        // Remove brackets from the number
        if (scientificNumber.contains('(') || scientificNumber.contains(')')) {
            scientificNumber = removeBrackets(scientificNumber)
        }

        if (scientificNumber.startsWith("-")) {
            scientificNumber = "(${scientificNumber})"
        }
        if (scientificNumber.endsWith(".")) {
            scientificNumber = scientificNumber.dropLast(1)
        }
        if (scientificNumber.endsWith(".)")) {
            scientificNumber = "${scientificNumber.dropLast(2)})"
        }

        return scientificNumber
    }

    data class Parts(
        val cursorPos: Int,
        val left: String,
        val right: String
    )

    // Get cursor position and the substrings to the left and right of the cursor
    fun getParts(displayFragment: DisplayFragment, expression: String): Parts {
        val cursorPos = displayFragment.getCursorPosition()
        val left = getLeftOfCursor(cursorPos, expression)
        val right = getRightOfCursor(cursorPos, expression)
        return Parts(cursorPos, left, right)
    }

    private fun getLeftOfCursor(cursorPos: Int, expression: String): String {
        if (expression.isEmpty()) return ""
        return expression.substring(0, cursorPos)
    }

    // Get substring to the right of the cursor
    private fun getRightOfCursor(cursorPos: Int, expression: String): String {
        if (expression.isEmpty()) return ""
        return expression.substring(cursorPos)
    }

    fun addTrailingZero(left: String, operator: String): String {
        val zero = "0"
        return "$left$zero$operator"
    }

    // Check if the user has entered an invalid operator. Cannot enter operator:
    // 1. As the first char in an expression
    // 2. Immediately after an operator
    // 3. Immediately after a negative sign
    // 4. Immediately after an open bracket
    // 5. Immediately before an operator
    // 6. Immediately before a factorial symbol
    fun isOperatorAllowed(left: String, right: String, mode: String): Boolean {
        val operators = setOf('+', '~', '×', '÷')

        return when {
            left.isEmpty() -> false
            left.isNotEmpty() && left.last() in operators -> false
            left.isNotEmpty() && left.last() == '-' -> false
            left.isNotEmpty() && left.last() == '(' -> false
            right.isNotEmpty() && right.first() in operators -> false
            right.isNotEmpty() && right.first() == '!' -> false
            else -> true
        }
    }

    // Check if the user has entered a number that contains more than one decimal
    fun hasInvalidDecimal(equation: String): Boolean {
        val numbers = equation.split(Regex("[$+~×÷]"))
        // Returns true if any number (it) contains a count of more than 1 decimal
        return numbers.any { it.count { char -> char == '.' } > 1 }
    }

    data class DecimalExpression(
        val newExpression: String,
        val leadingZeroAdded: Boolean
    )

    // Formats a decimal number in the expression with a leading zero if necessary
    fun addLeadingZero(left: String, right: String): DecimalExpression {
        var leadingZeroAdded = false
        var decimalExpression = ""

        if (left.isEmpty() || left.last() in setOf('+', '~', '×', '÷', '(')) {
            leadingZeroAdded = true
            val leadingZero = "0."
            decimalExpression = "$left$leadingZero$right"
        } else {
            decimalExpression = "$left.$right"
        }
        return DecimalExpression(decimalExpression, leadingZeroAdded)
    }

    // Get the leftmost number in an expression before the cursor
    fun extractLeftmostNumber(input: String): String {
        val regex = Regex("^-?\\d*\\.?\\d+")
        val matchResult = regex.find(input)
        return matchResult?.value ?: ""
    }
}