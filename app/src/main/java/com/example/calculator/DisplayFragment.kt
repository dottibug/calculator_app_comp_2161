package com.example.calculator

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentDisplayBinding

// Fragment to display the running equation and result of the calculation
class DisplayFragment : Fragment() {

    private lateinit var binding: FragmentDisplayBinding
     private lateinit var equationInput: EditText
    private lateinit var resultView: TextView
    private val fragUtils = FragmentUtilities()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        equationInput = binding.editViewEquation
        resultView = binding.textViewResult

        // Disable keyboard input
        equationInput.showSoftInputOnFocus = false

        // Set the cursor to the end of the equation
        equationInput.requestFocus()
    }

    // Get the cursor position in the equation EditText
    fun getCursorPosition() : Int {
        return equationInput.selectionStart
    }

    // Set the cursor position in the equation EditText
    fun setCursorPosition(position: Int) {
        if (equationInput.text.isNotEmpty()) {
            equationInput.setSelection(position)
        }
    }

    // Render equation in the EditText
    fun renderEquation(equation: String, cursorPosition: Int, cursorOffset: Int) {
        // Replace ~ in equation with - for the UI    −
        val updatedEquation = equation.replace("~", "−")
        val styledEquation = fragUtils.highlightOperators(updatedEquation, requireContext())
        equationInput.setText(styledEquation)
        equationInput.setSelection(cursorPosition + cursorOffset)
    }

    // Render result in the TextView
    fun renderResult(result: String) {
        resultView.text = result
    }

    // Render final result (with darker text color) in the TextView
    fun renderFinalResult(result: String) {
        Log.i("testcat", "renderFinalResult: $result")
        val styledResult = colorFinalResult(result, requireContext())
        Log.i("testcat", "renderFinalResult: $styledResult")
        resultView.text = styledResult

//        resultView.text = styledResult
    }

    // Color final result
    private fun colorFinalResult(result: String, context: Context): SpannableString {
        val spannable = SpannableString(result)
        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context, R.color.jet))
        spannable.setSpan(colorSpan, 0, result.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    ///////////////// OLD

    fun getEquation() : String {
        return equationInput.text.toString()
    }

    fun displayEquation(newEquation: String) {
        // Replaces ~ in equation string with - in the UI
        val updatedEquation = newEquation.replace("~", "-")
        equationInput.setText(updatedEquation)
        equationInput.setSelection(updatedEquation.length)
    }

    fun clearDisplay() {
        clearEquation()
        clearResult()
    }

    private fun clearEquation() {
        equationInput.setText("")
    }

    fun clearResult() {
        resultView.text = ""
    }

    fun updateResult(newResult: String) {
        resultView.text = newResult
    }

}