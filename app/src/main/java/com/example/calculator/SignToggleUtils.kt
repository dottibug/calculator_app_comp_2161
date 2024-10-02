package com.example.calculator

class SignToggleUtils {

    fun toggleNegativeSign(mode: String, expression: String, left: String, right: String,
        curPos: Int): Pair<String, Int> {

        var newExpression = expression
        var cursorOffset = 0

        // Toggle the sign in the expression
        // Uses Elvis ?: operator to apply the sign in different scenarios
        // The chain of calls stops at the first non-null value to return the new expression and
        // cursor offset
        if (isSignAllowed(expression, left, right)) {
            val result = startExpressionWithNegSign(expression, mode)
                ?: removeNegSignFromStart(expression, mode)
                ?: toggleNegSignAfterBracket(expression, mode)
                ?: addNegSignAfterOperator(mode, left, right)
                ?: removeNegSignAfterOperator(mode, left, right)
                ?: toggleNegSignOfNumber(expression, left, right, curPos, mode)
                ?: toggleNegSignAfterAbs(left, right, curPos)
                ?: toggleNegSignAfterOpenBracket(left, right)

            if (result != null) {
                newExpression = result.first
                cursorOffset = result.second
            }
        }

        return Pair(newExpression, cursorOffset)
    }

    fun isSignAllowed(expression: String, left: String, right: String): Boolean {
        if (left.isNotEmpty() && right.isEmpty() && (left.last().isDigit() || left.last() == ')'))
        { return false }

        else { return true }
    }

    fun startExpressionWithNegSign(expression: String, mode: String): Pair<String, Int>? {
        if (expression.isEmpty()) {
            val exp = if (mode == "simple") "-" else "(-"
            val offset = if (mode == "simple") 1 else 2
            return Pair(exp, offset)
        }
        return null
    }

    fun removeNegSignFromStart(expression: String, mode: String): Pair<String, Int>? {
        if (expression == "-" || expression == "(-") {
            val exp = ""
            val offset = if (mode == "simple") -1 else -2
            return Pair(exp, offset)
        }
        return null
    }

    // Toggle negative sign after a bracket if the bracket is the only character in the expression
    fun toggleNegSignAfterBracket(expression: String, mode: String): Pair<String, Int>? {
        if (expression == "(") {
            val negSign = "-"
            val exp = "$expression$negSign"
            val offset = 1
            return Pair(exp, offset)
        }
        return null
    }

    fun isNumber(number: String): Boolean {
        if (number.isEmpty()) return false
        if (number == "-" || number == "(" || number == "(-") return false
        return !number.contains(Regex("[$+~×÷]"))
    }

    fun isNegNumber(number: String): Boolean {
        if (number.isEmpty()) return false
        if (number.startsWith("-") || number.startsWith("(-")) return true
        return false
    }

    private fun toggleNegSignOfSingleNumber(expression: String, mode: String): Pair<String, Int>? {
        if (isNumber(expression)) {
            if (isNegNumber(expression)) {
                val prefixToRemove = if (mode == "simple") "-" else "(-"
                val exp = expression.removePrefix(prefixToRemove)
                val offset = if (mode == "simple") -1 else -2
                return Pair(exp, offset)
            } else {
                val prefixToAdd = if (mode == "simple") "-" else "(-"
                val exp = "$prefixToAdd$expression"
                val offset = if (mode == "simple") 1 else 2
                return Pair(exp, offset)
            }
        }
        return null
    }

    private fun getStartIndexOfNumber(left: String): Int {
        val lastOperatorIndex = left.lastIndexOfAny(charArrayOf('+', '~', '×', '÷'))
        return lastOperatorIndex + 1
    }

    private fun getNumSubstring(left: String, startIndex: Int, endIndex: Int): String {
        return left.substring(startIndex, endIndex)
    }

    fun toggleNegSignOfNumber(expression: String, left: String, right: String, curPos: Int,
        mode: String): Pair<String, Int>? {
        if (left.isNotEmpty() && left.last().isDigit()) {

            // If there are no operators to the left, the expression may be just a number
            if (!left.contains(Regex("[$+~×÷]"))) {
                return toggleNegSignOfSingleNumber(expression, mode)
            }

            // Toggle sign of the number to the immediate left of the cursor in the expression
            val numStartIndex = getStartIndexOfNumber(left)
            val num = getNumSubstring(left, numStartIndex, curPos)

            var newNum = ""
            var newLeft = ""
            var newExpression = ""
            var offset = 0

            val prefix = if (mode == "simple") "-" else "(-"

            // Change to positive (remove the negative sign)
            if (num.startsWith("-") || num.startsWith("(-")) {
                newNum = num.removePrefix(prefix)
                newLeft = left.replaceRange(numStartIndex, curPos, newNum)
                newExpression = "$newLeft$right"
                offset = if (mode == "simple") -1 else -2
            }

            // Change to negative (add the negative sign)
            else {
                newNum = "$prefix$num"
                newLeft = left.replaceRange(numStartIndex, curPos, newNum)
                newExpression = "$newLeft$right"
                offset = if (mode == "simple") 1 else 2
            }

            return Pair(newExpression, offset)
        }
        return null
    }

    fun addNegSignAfterOperator(mode: String, left: String, right: String): Pair<String, Int>? {
        var exp = ""
        var offset = 0

        if (left.isNotEmpty() && (left.last() in setOf('+', '~', '×', '÷')
                || left.endsWith("0."))) {
            if (left.endsWith("0.")) {
                val negativeSign = if (mode == "simple") "-0." else "(-0."
                exp = "${left.removeSuffix("0.")}$negativeSign$right"
                offset = if (mode == "simple") 1 else 2
            } else {
                val negativeSign = if (mode == "simple") "-" else "(-"
                exp = "$left$negativeSign$right"
                offset = if (mode == "simple") 1 else 2
            }
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

    fun toggleNegSignAfterAbs(left: String, right: String, curPos: Int): Pair<String, Int>? {
        if (left.isNotEmpty() && left.contains("abs(")) {
            val absIndex = left.lastIndexOf("abs(")
            var newLeft = ""
            var newExpression = ""
            var offset = 0

            // Change to positive (remove the negative sign)
            if (left.endsWith("-") || left.endsWith("(-")) {
                val prefix = if (left.endsWith("-")) "-" else "(-"
                newLeft = left.replaceRange(absIndex, curPos, prefix)
                newExpression = "$newLeft$right"
                offset = if (left.endsWith("-")) 1 else 2
            }

            // Change to negative (add the negative sign)
            else {
                val prefix = "(-"
                newLeft = left.replaceRange(absIndex, curPos, prefix)
                newExpression = "$newLeft$right"
                offset = 2
            }
            return Pair(newExpression, offset)
        }
        return null
    }

    fun toggleNegSignAfterOpenBracket(left: String, right: String): Pair<String, Int>? {
        var exp = ""
        var offset = 0

        if (left.isNotEmpty() && (left.endsWith("(") || left.endsWith("(-"))) {
            // Change to positive (remove the negative sign)
            if (left.endsWith("(-")) {
                val suffixToRemove = "(-"
                exp = "${left.removeSuffix(suffixToRemove)}$right"
                offset = -2
            }

            // Change to negative (add the negative sign)
            else if (left.endsWith("(")) {
                val negSign = "(-"
                exp = "$left$negSign$right"
                offset = 2
            }
            return Pair(exp, offset)
        }
        return null
    }
}