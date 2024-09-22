package com.example.calculator

class Calculator
{
    fun calculate(equation: String) : String {
        // Split the equation string into mutable arrays of numbers and operators
        val numbers = getNumbersList(equation)
        val operators = getOperatorsList(equation)

        // BEDMAS Order of operations
        // TODO need to handle brackets
        // TODO need to handle exponents (scientific mode)

        val (updatedNumbers, updatedOperators) = handleMultiplicationAndDivision(numbers, operators)
        return performRemainingOperations(updatedNumbers, updatedOperators)
    }

    // Performs the remaining operations after multiplication and division have been handled
    private fun performRemainingOperations(numbers: MutableList<String>, operators:
    MutableList<String>) : String {
        var result = numbers[0].toInt()

        for (i in operators.indices) {
            // NOTE: × is not the letter x, it is the multiplication symbol
            when (operators[i]) {
                "+" -> result += numbers[i+1].toInt()
                "~" -> result -= numbers[i+1].toInt()
                "×" -> result *= numbers[i+1].toInt()
                "÷" -> result /= numbers[i+1].toInt()
            }
        }
        return result.toString()
    }

    // Handles multiplication and division to follow BEDMAS order of operations. Returns the
    // updated lists as a Pair
    private fun handleMultiplicationAndDivision(numbers: MutableList<String>, operators:
    MutableList<String>) : Pair<MutableList<String>, MutableList<String>> {
        var i = 0
        while (i < operators.size) {
            // NOTE: × is not the letter x, it is the multiplication symbol
            if (operators[i] == "×" || operators[i] == "÷") {
                // Multiply or divide the numbers (i and i+1)
                val currentResult = if (operators[i] == "×") numbers[i].toInt() * numbers[i+1].toInt()
                else numbers[i].toInt() / numbers[i+1].toInt()

                // Replace numbers[i] with the result then remove numbers[i+1] from the numbers list
                numbers[i] = currentResult.toString()
                numbers.removeAt(i+1)

                // Remove operators[i] from the operators list
                operators.removeAt(i)

                // Reset i to 0: This restarts the loop from the beginning. The size of the lists
                // have changed after removing elements, so the indices of the remaining elements
                // have also changed. Without resetting i, the loop would skip the remaining
                // elements to be calculated and the result would be incorrect
                i = 0
                continue
            }
            i++
        }
        return Pair(numbers, operators)
    }

    // Get a mutable list of numbers from the equation string
    private fun getNumbersList(equation: String) : MutableList<String> {
        // NOTE: × is not the letter x, it is the multiplication symbol
        // NOTE: ~ is used to represent subtraction and allow for easy parsing of minus vs negative
        return equation.split("+", "~", "×", "÷").toMutableList()
    }

    // Get a mutable list of operators from the equation string
    private fun getOperatorsList(equation: String) : MutableList<String> {
        return equation.split(Regex("-?\\d+")).filter { it.isNotEmpty() }.toMutableList()
    }
}