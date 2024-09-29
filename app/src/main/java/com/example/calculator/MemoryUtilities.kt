package com.example.calculator

class MemoryUtilities {

    fun isNumber(equation: String) : Boolean {
        if (equation == "-" || equation == "(" || equation == "(-") return false
        return !equation.contains(Regex("[$+~×÷]"))
    }

    private fun removeBrackets(input: String) : String {
        val regex = """[()]+""".toRegex()
        return input.replace(regex, "")
    }

    fun getScientificNumber(number : String) : String {
        var scientificNumber = number

        // Remove brackets from the number
        if (scientificNumber.contains('(') || scientificNumber.contains(')')) {
            scientificNumber = removeBrackets(scientificNumber)
        }

        if (scientificNumber.startsWith("-")) { scientificNumber = "(${scientificNumber})" }
        if (scientificNumber.endsWith(".")) { scientificNumber = scientificNumber.dropLast(1) }
        if (scientificNumber.endsWith(".)")) { scientificNumber = "${scientificNumber.dropLast(2)})" }

        return scientificNumber
    }

    fun getSimpleNumber(number : String) : String {
        var simpleNumber = number
        if (simpleNumber.endsWith(".")) { simpleNumber = simpleNumber.dropLast(1) }
        return simpleNumber
    }
}