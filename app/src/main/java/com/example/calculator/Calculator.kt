package com.example.calculator

import android.content.Context
import android.util.Log

// next commit -m : "Refactor calculator class to implement shunting-yard algorithm and handle brackets"

class Calculator
{

    // Use the shunting-yard algorithm to calculate the result of the equation
    // Reference: https://brilliant.org/wiki/shunting-yard-algorithm/
    fun calculate(context: Context, equation: String) : String {
        // todo handle invalid equations (old way pasted below for reference when refactoring)
//        if (operators.size != (numbers.size - 1)) {
//            Toast.makeText(context, "Invalid equation", Toast.LENGTH_SHORT).show()
//            return "error"
//        }

        val postfix = getPostfixExpression(equation)
        Log.i("testcat", "postfix: $postfix")

        // Evaluate the postfix expression
        val result = evaluatePostfix(postfix)
        Log.i("testcat", "result: $result")


        return result
    }

    fun getPostfixExpression(equation: String) : MutableList<String> {
        val output : MutableList<String> = mutableListOf()
        val operatorList : MutableList<String> = mutableListOf()

        // Regex to parse the equation for numbers, operators, and brackets
        val regex = Regex("(-?[0-9.]+)|([+~×÷()])")

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

    private fun evaluatePostfix(postfix: MutableList<String>) : String {
        val stack = mutableListOf<String>()

        postfix.forEach { token ->
            // NOTE: × is not the letter x, it is the multiplication symbol
            if (token in setOf("+", "~", "×", "÷")) {
                // Pop the top two numbers from the stack, perform the operation, and push the
                // result back onto the stack
                val num2 =  stack.removeLast()
                val num1 = stack.removeLast()

                // Check if num1 and num2 are numbers (not brackets)
                if (num1.matches(Regex("-?[0-9.]+")) && num2.matches(Regex("-?[0-9.]+"))) {
                    val result = when (token) {
                        "+" -> num1.toInt() + num2.toInt()
                        "~" -> num1.toInt() - num2.toInt()
                        "×" -> num1.toInt() * num2.toInt()
                        "÷" -> num1.toInt() / num2.toInt()
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
        return if (stack.size == 1) stack.last() else "error"
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