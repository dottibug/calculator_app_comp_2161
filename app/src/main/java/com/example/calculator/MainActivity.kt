package com.example.calculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.calculator.databinding.ActivityMainBinding

//
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var operatorClicked : Boolean = false
    private lateinit var displayFragment: DisplayFragment
    private var numberInput : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflates the layout using the data binding object and sets the root view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sets up the window insets so app content is not covered by system UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the display fragment
        displayFragment = supportFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment
    }

    // Clear button click handler
    fun onClearClick() {
        displayFragment.clearDisplay()
        numberInput = ""
        operatorClicked = false
    }

    // Equals button click handler
    fun onEqualsClick() {
        // TODO
    }

    // Number button click handler
    fun onNumberClick(number: String) {
        numberInput += number
        displayFragment.updateEquation(number)

        if (operatorClicked) {
            val result = calculateResult()
            displayFragment.updateResult(result)
        }
    }

    // Operator button click handler
    fun onOperatorClick(operator: String) {
        if (operator != "=") {
            displayFragment.clearResult()
            displayFragment.updateEquation(operator)
            operatorClicked = true

        } else {
            calculateResult()
            operatorClicked = false
        }
    }

    // Calculate the result of the equation
    private fun calculateResult() : String {
        val equation = displayFragment.getEquation()
        val calculator = Calculator()
        return calculator.calculate(equation)
    }
}

