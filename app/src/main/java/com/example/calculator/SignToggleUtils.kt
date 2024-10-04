package com.example.calculator

// Methods for toggling the sign of a number in an expression
class SignToggleUtils {

    fun toggleSign(mode: String, expression: String, left: String, right: String,
        curPos: Int): Pair<String, Int> {

        var newExpression = expression
        var offset = 0

        // Toggle the sign in the expression
        // Uses Elvis ?: operator to apply the sign in different scenarios
        // The chain of calls stops at the first non-null value to return the new expression and
        // cursor offset
        val result = startExpressionWithNegSign(expression, mode)
            ?: removeNegSignFromStart(expression, mode)
            ?: toggleNegSignAfterOpenBracket(expression, mode)
            ?: toggleSignOfNumber(expression, left, right, curPos, mode)
            ?: addNegSignAfterOperator(mode, left, right)
            ?: removeNegSignAfterOperator(mode, left, right)

        if (result != null) {
            newExpression = result.first
            offset = result.second
        }

        return Pair(newExpression, offset)
    }

    private fun startExpressionWithNegSign(expression: String, mode: String): Pair<String, Int>? {
        if (expression.isEmpty()) {
            val exp = if (mode == "simple") "-" else "(-"
            val offset = if (mode == "simple") 1 else 2
            return Pair(exp, offset)
        }
        return null
    }

    private fun removeNegSignFromStart(expression: String, mode: String): Pair<String, Int>? {
        if (expression == "-" || expression == "(-") {
            val exp = ""
            val offset = if (mode == "simple") -1 else -2
            return Pair(exp, offset)
        }
        return null
    }

    // Add negative sign after a bracket if the bracket is the only character in the expression
    private fun toggleNegSignAfterOpenBracket(expression: String, mode: String): Pair<String,
        Int>? {
        if (expression == "(") {
            val negSign = "-"
            val exp = "$expression$negSign"
            val offset = 1
            return Pair(exp, offset)
        }
        return null
    }

    private fun toggleSignOfNumber(expression: String, left: String, right: String, curPos: Int,
        mode: String):
        Pair<String, Int>? {

        var newExpression = expression
        var newLeft = left
        var newNum = ""
        var offset = 0

        if (left.isNotEmpty() && left.last().isDigit()) {
            val numStartIndex = getStartIndexOfNumber(left)
            val (isNeg, number, negSign) = getNumberSubstring(left, numStartIndex, curPos)

            // If number is negative, remove the prefix from the number; otherwise, add it
            if (isNeg) {
                newNum = number.removePrefix(negSign)
                offset = negSign.length*-1
            }
            else {
                newNum = if (mode == "simple") "-$number" else "(-$number"
                offset = if (mode == "simple") 1 else 2
            }

            // Replace the number with the new number and sign
            newLeft = left.replaceRange(numStartIndex, curPos, newNum)
            newExpression = "$newLeft$right"
            return Pair(newExpression, offset)
        }
        return null
    }

    data class NumSign (
        val isNeg: Boolean,
        val num: String,
        val negSign: String = ""
    )

    private fun getNumberSubstring(left: String, startIndex: Int, curPos: Int) : NumSign {
        var isNeg = false
        var prefix = ""
        val number = left.substring(startIndex, curPos)

        // Check if number starts with negative
        if (number.startsWith("-") || number.startsWith("(-")) {
            isNeg = true
            prefix = if (number.startsWith("-")) "-" else "(-"
        }

        // The number is not preceded by a negative sign
        else {
            isNeg = false
            prefix = ""
        }
        return NumSign(isNeg, number, prefix)
    }

    private fun getStartIndexOfNumber(left: String): Int {
        val operators = charArrayOf('+', '~', '×', '÷')
        val numbers = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        val hasOperators = left.lastIndexOfAny(operators)
        var startIndex = 0

        // Left does not have any operators
        if (hasOperators == -1) {
            // If left contains a negative, get the start index of the negative number
            if (left.contains("-")) {
                val negIndex = left.indexOf("-")
                    startIndex = if (negIndex > 0 && left[negIndex - 1] == '(') {negIndex - 1}
                                else { negIndex }
            } else {
                // Get the start index of the positive number
                startIndex = left.indexOfAny(numbers)
            }
        }

        // Left has operators, so get index of the first digit after the last operator
        else {
            val lastOperatorIndex = left.lastIndexOfAny(operators)
            startIndex = lastOperatorIndex + 1
        }

        return startIndex
    }

    private fun addNegSignAfterOperator(mode: String, left: String, right: String): Pair<String, Int>? {
        var exp = ""
        var offset = 0

        if (left.isNotEmpty() && left.last() in setOf('+', '~', '×', '÷')) {
            val negativeSign = if (mode == "simple") "-" else "(-"
            exp = "$left$negativeSign$right"
            offset = if (mode == "simple") 1 else 2
            return Pair(exp, offset)
        }
        return null
    }

    fun removeNegSignAfterOperator(mode: String, left: String, right: String): Pair<String, Int>? {
        val operators = setOf('+', '~', '×', '÷')

        if (left.isNotEmpty() && left.last() in operators) {
            val suffixToRemove = if (mode == "simple") "-" else "(-"
            val exp = "${left.removeSuffix(suffixToRemove)}$right"
            val offset = if (mode == "simple") -1 else -2
            return Pair(exp, offset)
        }
        return null
    }
}