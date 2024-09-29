package com.example.calculator

import androidx.fragment.app.Fragment

abstract class CalculatorFragment : Fragment() {
    protected lateinit var displayFragment: DisplayFragment
    protected lateinit var memoryFragment: MemoryFragment
    protected val calcUtils = CalculatorUtilities()
    protected val fragUtils = FragmentUtilities()
    protected val memoryUtils = MemoryUtilities()
    protected var isFinalResult: Boolean = false
    protected var expression: String = ""
    protected var result: String = ""
    protected var memory: String = ""

    // Handle number clicks
    fun onNumberClick(number: String) {
        if (isFinalResult) expression = ""

        isFinalResult = false
        var cursorOffset = 1
        var (cursorPosition, leftOfCursor, rightOfCursor) = fragUtils.getEquationParts(displayFragment, expression)

        // Render equation and result
        expression = "$leftOfCursor$number$rightOfCursor"

        // Handles cursor position and offset when a number is clicked while isFinalResult was true
        if (expression.length == 1) {
            cursorPosition = 1
            cursorOffset = 0
        }

        displayFragment.renderEquation(expression, cursorPosition, cursorOffset)
//        calculateLeftToRightResult(expression)
    }

}