package com.example.calculator

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.calculator.databinding.FragmentDisplayBinding

// Fragment to display the running equation and result of the calculation
class DisplayFragment : Fragment() {
    private lateinit var binding: FragmentDisplayBinding
    private lateinit var expressionInput: EditText
    private lateinit var resultView: TextView
    private lateinit var imm: InputMethodManager
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentDisplayBinding.inflate(inflater, container, false)
        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        expressionInput = binding.editViewExpression
        resultView = binding.textViewResult

        expressionInput.showSoftInputOnFocus = false
        // BUG The keyboard keeps opening when we request focus on the EditText multiline
        expressionInput.requestFocus()
        imm.hideSoftInputFromWindow(expressionInput.windowToken, 0)
    }

    // Get the cursor position in the equation EditText
    fun getCursorPosition() : Int {
        return expressionInput.selectionStart
    }

    // Render equation in the EditText
    fun renderExpression(exp: String, cursorPosition: Int, cursorOffset: Int) {
        // Replace ~ in equation with - for the UI    −
        val updatedEquation = exp.replace("~", "−")
        val styledEquation = highlightOperators(updatedEquation, requireContext())
        expressionInput.setText(styledEquation)
        expressionInput.setSelection(cursorPosition + cursorOffset)
    }

    // Render result in the TextView
    fun renderResult(result: String) {
        resultView.text = result
    }

    // Render final result (with darker text color) in the TextView
    fun renderFinalResult(result: String) {
        val styledResult = colorFinalResult(result, requireContext())
        resultView.text = styledResult
    }

    // ----------------------------------------------------------------------------------------------
    // STYLING
    // ----------------------------------------------------------------------------------------------
    // Color the operators in the equation
    private fun highlightOperators(equation: String, context: Context): SpannableString {
        val spannable = SpannableString(equation)
        val operators = setOf('+', '−', '×', '÷')

        for (i in equation.indices) {
            if (equation[i] in operators) {
                if (equation[i] in operators) {
                    val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.carolinaBlue))
                    spannable.setSpan(colorSpan, i, i + 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        return spannable
    }

    // Color final result
    private fun colorFinalResult(result: String, context: Context): SpannableString {
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)

        val spannable = SpannableString(result)
        if (isDarkMode) {
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.carolinaBlue))
            spannable.setSpan(colorSpan, 0, result.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.steelBlue))
            spannable.setSpan(colorSpan, 0, result.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return spannable
    }
}