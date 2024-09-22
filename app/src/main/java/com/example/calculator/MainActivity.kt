package com.example.calculator

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.calculator.databinding.ActivityMainBinding

//
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var operatorClicked : Boolean = false
    private var numberInput : String = ""
//    private var result : String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables edge-to-edge mode
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
    }

    // Equals button click handler
    fun onEqualsClick() {
        // TODO
    }

    // 9+1-5x2 should equal 0 if calculate is correct

    fun onNumberClick(number: String) {
        // Get the DisplayFragment and call its updateEquation method
        val displayFragment = supportFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment

//        operatorClicked = false

        numberInput += number
        displayFragment.updateEquation(number)


        if (operatorClicked) {
            val result = calculateResult()
            displayFragment.updateResult(result)
        }

    }

    // Called when an operator button is clicked
    fun onOperatorClick(operator: String) {
        // Get the DisplayFragment
        val displayFragment = supportFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment

        if (operator != "=") {
            // clear the result
            displayFragment.clearResult()

            // update the equation
            displayFragment.updateEquation(operator)

            // Set operatorClicked to true
            operatorClicked = true

        } else {
            // TODO Calculate the final result and update the display
            calculateResult()
//            val finalResult = calculateResult()
//            Log.i("testcat", "final result: $finalResult")
        }
    }

    // Calculate the result of the equation
    private fun calculateResult() : String {
        Log.i("testcat", "---calculateResult called")

        val displayFragment = supportFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment
        val equation = displayFragment.getEquation()

        // Split the equation string into mutable arrays for the numbers and operators
        // NOTE: × is not the letter x, it is the multiplication symbol
        val numbers = equation.split("+", "-", "×", "÷").toMutableList()
        val operators = equation.split(Regex("\\d+")).filter { it.isNotEmpty() }.toMutableList()

        Log.i("testcat", "numbers: $numbers")
        Log.i("testcat", "operators: $operators")

        // Handle multiplication and division first
        var i = 0
        while (i < operators.size) {
            if (operators[i] == "×" || operators[i] == "÷") {
                Log.i("testcat", "multiply or divide")
                Log.i("testcat", "numbers[i]: ${numbers[i]}")
                Log.i("testcat", "numbers[i+1]: ${numbers[i+1]}")
//                Log.i("testcat", "result [i] * [i+1]: ${numbers[i].toInt() * numbers[i+1].toInt()}")

                // Multiply or divide the two numbers (i and i+1)
                val currentResult = if (operators[i] == "×") numbers[i].toInt() * numbers[i+1].toInt()
                else numbers[i].toInt() / numbers[i+1].toInt()

                Log.i("testcat", "currentResult: $currentResult")

                // replace numbers[i] with the result
                numbers[i] = currentResult.toString()

                // remove numbers[i+1]
                numbers.removeAt(i+1)

                // remove operators[i]
                operators.removeAt(i)

                Log.i("testcat", "numbers AFTER removal and update: $numbers")

                // Reset i to 0 to iterate through the updated numbers and operators lists
                i = 0
                continue
            }
            i++
        }

        // Handle remaining calculation
         return testCalcFinish(numbers, operators)
    }

    private fun testCalcFinish(numbers: MutableList<String>, operators: MutableList<String>) :
            String {
        Log.i("testcat", "testCalcFinish called")

        // Handle addition and subtraction
        var testResult = numbers[0].toInt()

        for (i in operators.indices) {
            when (operators[i]) {
                "+" -> testResult += numbers[i+1].toInt()
                "-" -> testResult -= numbers[i+1].toInt()
                "×" -> testResult *= numbers[i+1].toInt()
                "÷" -> testResult /= numbers[i+1].toInt()
            }
        }

        Log.i("testcat", "testResult: $testResult")
        return testResult.toString()
    }

}

