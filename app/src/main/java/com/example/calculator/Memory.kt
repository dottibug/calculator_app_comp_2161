package com.example.calculator

// This class contains methods related to the memory functionality of the calculator.
class Memory {
    private val calcUtils = CalcUtils()
    private var memoryValue: String = ""

    data class MemoryResponse(
        val response: String,
        val memoryValue: String
    )

    data class MemoryOpResponse(
        val response: String,
        val newExpression: String
    )

    fun store(expression: String, result: String, isFinalResult: Boolean): MemoryResponse {
        val number = if (isFinalResult) result else expression

        return if (calcUtils.isNumber(number)) {
            memoryValue = calcUtils.getScientificNumber(number)
            MemoryResponse("Memory updated", memoryValue)
        } else {
            MemoryResponse("Memory only stores numbers", "")
        }
    }

    fun recall(): MemoryResponse {
        return if (memoryValue.isEmpty()) {
            MemoryResponse("Memory is empty", "")
        } else {
            MemoryResponse("Memory recalled", memoryValue)
        }
    }

    fun clear(): MemoryResponse {
        memoryValue = ""
        return MemoryResponse("Memory cleared", "")
    }

    fun setMemory(value: String) {
        memoryValue = value
    }

    fun operation(expression: String, result: String, operator: String, isFinalResult: Boolean): MemoryOpResponse {
        var newExpression = ""

        if (memoryValue.isEmpty()) {
            return MemoryOpResponse("Memory is empty", newExpression)
        }

        if (expression.isEmpty() && result.isEmpty()) {
            newExpression = "$expression$memoryValue"
            return MemoryOpResponse("Memory recalled", newExpression)
        }

        if (isFinalResult) {
            newExpression = "$result$operator$memoryValue"
            return MemoryOpResponse("Memory updated", newExpression)
        }

        val isValidNumber = calcUtils.isNumber(expression)
        return if(isValidNumber) {
            val simpleNumber = calcUtils.getSimpleNumber(expression)
            newExpression = "$simpleNumber$operator$memoryValue"
            MemoryOpResponse("Memory updated", newExpression)
        } else {
            MemoryOpResponse("Memory only stores numbers", newExpression)
        }
    }
}