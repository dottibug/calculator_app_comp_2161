package com.example.calculator

import android.content.Context
import android.util.Log
import java.util.Locale

class Calculator
{

    // Use the shunting-yard algorithm to calculate the result of the equation
    // Reference: https://brilliant.org/wiki/shunting-yard-algorithm/
    fun calculate(context: Context, equation: String, decimalPlaces: String = "10") : String {
        // todo handle invalid equations (old way pasted below for reference when refactoring)
//        if (operators.size != (numbers.size - 1)) {
//            Toast.makeText(context, "Invalid equation", Toast.LENGTH_SHORT).show()
//            return "error"
//        }

        val postfix = getPostfixExpression(equation)

        Log.i("testcat", "postfix: $postfix")

        if (postfix.isEmpty()) {
            return if (equation.isEmpty()) "" else "error"
        } else {
            val result = try {
                evaluatePostfix(postfix, decimalPlaces)
            } catch (e: Exception) {
                "error" // Or handle the error appropriately
            }
        return result
        }
    }

    private fun getPostfixExpression(equation: String) : MutableList<String> {
        val output : MutableList<String> = mutableListOf()
        val operatorList : MutableList<String> = mutableListOf()

        // Regex to parse the equation for numbers, operators, and brackets
        // Match numbers with an optional negative sign and/or optional decimal point
        val regex = Regex("(-?[0-9]+\\.?[0-9]*)|([+~×÷()])")

        regex.findAll(equation).forEach {
            val currentToken = it.value

            when {
                // If the token is a number, add it to the output list
                currentToken.matches(Regex("-?[0-9.]+")) -> { output.add(currentToken) }

                // If the token is an open bracket, add it to the operator list
                currentToken == "(" -> { operatorList.add(currentToken) }

                // If the token is a close bracket, add all operators to the output list until an
                // open bracket is found
                currentToken == ")" -> {
                    while (operatorList.last() != "(") {
                        output.add(operatorList.removeLast())

                        // If operatorList is empty, then there are mismatched brackets
                        if (operatorList.isEmpty()) {
                            // TODO handle mismatched brackets
                            Log.i("testcat", "mismatched brackets")
                            return mutableListOf()
                        }
                    }
                    // Remove the open bracket from the operator list
                    operatorList.removeLast()
                }

                // If the token is an operator, add it to the operator list if it has a higher
                // precedence than the top of the operator list, or if the operator list is empty.
                // Otherwise, add all operators to the output list until an operator with a lower
                // precedence is found or the operator list is empty.
                // NOTE: × is not the letter x, it is the multiplication symbol
                currentToken in setOf("+", "~", "×", "÷") -> {
                    while (operatorList.isNotEmpty() &&
                        operatorList.last() != "(" &&
                        getPrecedence(operatorList.last()) >= getPrecedence(currentToken)) {
                        output.add(operatorList.removeLast())
                    }
                    operatorList.add(currentToken)
                }
            }
        }
        // Add any remaining operators from operatorList to output
        while (operatorList.isNotEmpty()) {
            output.add(operatorList.removeLast())
        }

        return output
    }

    private fun evaluatePostfix(postfix: MutableList<String>, decimalPlaces: String) : String {
        val stack = mutableListOf<String>()

        postfix.forEach { token ->
            // NOTE: × is not the letter x, it is the multiplication symbol
            if (token in setOf("+", "~", "×", "÷")) {

                // Check if there are at least two numbers in the stack
                if (stack.size < 2) { return "" }

                // Pop the top two numbers from the stack, perform the operation, and push the
                // result back onto the stack
                val num2 =  stack.removeLast()
                val num1 = stack.removeLast()

                // Regex to match numbers with an optional negative sign and/or optional decimal point
                val numberPattern = "-?[0-9]+\\.?[0-9]*"
                // Check if num1 and num2 are numbers (not brackets)
                if (num1.matches(Regex(numberPattern)) && num2.matches(Regex(numberPattern))) {
                    val result = when (token) {
                        "+" -> num1.toDouble() + num2.toDouble()
                        "~" -> num1.toDouble() - num2.toDouble()
                        "×" -> num1.toDouble() * num2.toDouble()
                        "÷" -> num1.toDouble() / num2.toDouble()
                        // TODO: handle error
                        else -> {
                            Log.i("testcat", "Invalid operator")
                            return "error"
                        }
                    }
                    stack.add(result.toString())
                } else {
                    Log.i("testcat", "Invalid expression")
                    return "error"
                }
            } else {
                // Push the number onto the stack
                stack.add(token)
            }
        }
        // Check for single result or error
        if (stack.size == 1) {
            val result = stack.last().toDouble()
            // Format the result to 10 decimal places and remove trailing zeros and decimal points
            val formattedResult = String.format(Locale.CANADA,"%.${decimalPlaces}f", result).trimEnd('0')
                .trimEnd('.')
            return formattedResult
        } else {
            return "error"
        }
    }

    // Get the precedence of an operator
    private fun getPrecedence(operator: String) : Int {
        // NOTE: × is not the letter x, it is the multiplication symbol
        return when (operator) {
            "÷", "×" -> 2
            "+", "~" -> 1
            else -> 0
        }
    }
}