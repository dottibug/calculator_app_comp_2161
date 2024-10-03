package com.example.calculator

import java.util.Locale

class CalcUtils {
    private val signUtils = SignToggleUtils()

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

    fun hasTooManyDigits(number: String, decimalPlaces: Int): Boolean {
        if (number.isEmpty()) return false

        var digitCount = 0
        val formatNumber = formatResult(number, decimalPlaces)

        // Count the digits in the unformatted number (so decimal places are not limited)
        if (formatNumber.contains(".")) digitCount = number.count { it.isDigit() }

        // Count the digits in the formatted number so trailing zeros are not counted
        else digitCount = formatResult(number, decimalPlaces).count { it.isDigit() }

        return digitCount > 12
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

    private fun hasDoubleOperators(left: String, right: String): Boolean {
        val operators = setOf('+', '~', '×', '÷')

        val charLeft = left.lastOrNull()
        val charRight = right.firstOrNull()

        if ((charLeft != null && charLeft in operators) || (charRight != null && charRight in
            operators)) {
            return true
        } else {
            return false
        }
    }

    fun isOperatorAllowed(left: String, right: String, mode: String): Boolean {
        // Prevent user from entering an operator as the first char in the equation
        if (left.isEmpty()) {
            return false
        }

        // Prevent user from entering two operators in a row
        if (hasDoubleOperators(left, right)) {
            return false
        }

        // Prevent user from entering an operator to the right of an open bracket
        if (mode == "scientific" && left.isNotEmpty() && left.last() == '(') {
            return false
        }

        return true
    }

    // Check if the user has entered a number that contains more than one decimal
    fun hasInvalidDecimal(equation: String): Boolean {
        val numbers = equation.split(Regex("[$+~×÷]"))
        // Returns true if any number (it) contains a count of more than 1 decimal
        return numbers.any { it.count { char -> char == '.' } > 1 }
    }

    data class DecimalExpression(
        val decimalExpression: String,
        val leadingZeroAdded: Boolean
    )

    // Formats a decimal expression with a leading zero if necessary
    fun getDecimalExpression(left: String, right: String): DecimalExpression {
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

    // Toggles negative sign in an expression
    fun toggleNegativeSign(mode: String, expression: String, left: String, right: String,
        curPos: Int): Pair<String, Int> {
        return signUtils.toggleNegativeSign(mode, expression, left, right, curPos)
    }

    // Get the leftmost number in an expression before the cursor
    fun extractLeftmostNumber(input: String): String {
        val regex = Regex("^-?\\d*\\.?\\d+")
        val matchResult = regex.find(input)
        return matchResult?.value ?: ""
    }
}