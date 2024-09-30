package com.example.calculator

import android.content.Context
import android.widget.Toast

// Common utilities across fragments
class FragmentUtilities {
    var toast: Toast? = null

    // ----------------------------------------------------------------------------------------------
    // SUBSTRING FUNCTIONS
    // ----------------------------------------------------------------------------------------------
    // Get cursor position and the substrings to the left and right of the cursor
    fun getExpressionParts(displayFragment: DisplayFragment, expression: String) : Triple<Int,
            String, String> {
        val cursorPosition = displayFragment.getCursorPosition()
        val leftOfCursor = getLeftOfCursor(cursorPosition, expression)
        val rightOfCursor = getRightOfCursor(cursorPosition, expression)
        return Triple(cursorPosition, leftOfCursor, rightOfCursor)
    }

    // Get substring to the left of the cursor
    private fun getLeftOfCursor(cursorPosition: Int, expression: String) : String {
        if (expression.isEmpty()) return ""
        return expression.substring(0, cursorPosition)
    }

    // Get substring to the right of the cursor
    private fun getRightOfCursor(cursorPosition: Int, expression: String) : String {
        if (expression.isEmpty()) return ""
        return expression.substring(cursorPosition)
    }

    // ----------------------------------------------------------------------------------------------
    // TOAST
    // ----------------------------------------------------------------------------------------------
    // Show toast message
    fun showToast(context : Context, message: String) {
        toast?.cancel() // Cancel the previous Toast if it exists
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast?.show()
    }
}