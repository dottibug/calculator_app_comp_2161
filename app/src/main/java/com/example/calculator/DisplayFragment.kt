package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentDisplayBinding

// Fragment to display the running equation and result of the calculation
class DisplayFragment : Fragment() {

    private lateinit var binding: FragmentDisplayBinding
     lateinit var equation: EditText
    private lateinit var result: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize the equation
        equation = binding.editViewEquation
        result = binding.textViewResult

        // Disable keyboard input
        equation.showSoftInputOnFocus = false

        // Set the cursor to the end of the equation
        equation.requestFocus()
    }

    fun getEquation() : String {
        return equation.text.toString()
    }

    fun displayEquation(newEquation: String) {
        // Replaces ~ in equation string with - in the UI
        val updatedEquation = newEquation.replace("~", "-")
        equation.setText(updatedEquation)
        equation.setSelection(updatedEquation.length)
    }

    fun clearDisplay() {
        clearEquation()
        clearResult()
    }

    private fun clearEquation() {
        equation.setText("")
    }

    fun clearResult() {
        result.text = ""
    }

    fun updateResult(newResult: String) {
        result.text = newResult
    }

    fun getCursorPosition() : Int {
        return equation.selectionStart
    }

    fun setCursorPosition(position: Int) {
        if (equation.text.isNotEmpty()) {
            equation.setSelection(position)
        }
    }
}