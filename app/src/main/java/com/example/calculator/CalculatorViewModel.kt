package com.example.calculator

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalculatorViewModel : ViewModel() {
    // STATE PROPERTIES --------------------------------------------------------

    // Equation state
    private val _equation = MutableStateFlow("")
    val equation = _equation.asStateFlow()

    // Result state
    private val _result = MutableStateFlow("")
    val result = _result.asStateFlow()


    // METHODS -----------------------------------------------------------------

    // Update equation
    fun onNumberClicked(number: String) {
        _equation.value += number
        Log.i("testcat", "equation value: ${_equation.value}")
    }

    // Update result
    fun updateResult(result: String) {
        _result.value = result
    }
}