package com.example.calculator

import android.content.Context
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

// Common utilities across fragments
class FragmentUtilities {
    // Get cursor position and the substrings to the left and right of the cursor
    fun getEquationParts(displayFragment: DisplayFragment, equation: String) : Triple<Int,
            String, String> {
        val cursorPosition = displayFragment.getCursorPosition()
        val leftOfCursor = getLeftOfCursor(cursorPosition, equation)
        val rightOfCursor = getRightOfCursor(cursorPosition, equation)
        return Triple(cursorPosition, leftOfCursor, rightOfCursor)
    }

    // Get substring to the left of the cursor
    private fun getLeftOfCursor(cursorPosition: Int, equation: String) : String {
        return equation.substring(0, cursorPosition)
    }

    // Get substring to the right of the cursor
    private fun getRightOfCursor(cursorPosition: Int, equation: String) : String {
        return equation.substring(cursorPosition)
    }

    // Show toast message
    fun showToast(message: String, context : Context) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // ----------------------------------------------------------------------------------------------
    // STYLING FUNCTIONS
    // ----------------------------------------------------------------------------------------------
    fun highlightOperators(equation: String, context: Context): SpannableString {
        Log.i("testcat", "equation: $equation")

        val spannable = SpannableString(equation)
        val operators = setOf('+', '−', '×', '÷')

        for (i in equation.indices) {
            if (equation[i] in operators) {
                if (equation[i] in operators) {
                    val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.aero))
                    spannable.setSpan(colorSpan, i, i + 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        return spannable

        // TODO change colors of just the operators. Send that back. With the equation sent back,
    //      call another function to change ~ to -
    }
}