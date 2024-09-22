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
    private var equalsClicked : Boolean = false
    private var finalResult : String = ""

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
        // TEST
        // note: after clicking equals, the equation should be cleared and only the result show
        finalResult = calculateResult()
        displayFragment.clearEquation()
        displayFragment.updateResult(finalResult)
        equalsClicked = true
        operatorClicked = false
        numberInput = ""


        // note: if a user clicks equals and then clicks another operator, the result should
        //  become the first number in the new equation and the old equation should be cleared
//        equalsClicked = true
//        val result = calculateResult()
//        displayFragment.updateResult(result)
//        operatorClicked = false // Reset operatorClicked after calculation
    }

    // Number button click handler
    fun onNumberClick(number: String) {
        // note: if equalsClicked is TRUE and a user clicks a number, that number should become
        //  the first number in the new equation and the result should be cleared

        numberInput += number

        if (equalsClicked) {
            displayFragment.clearEquation()
            displayFragment.updateEquation(numberInput)
            displayFragment.clearResult()
            equalsClicked = false
        } else {
            displayFragment.updateEquation(number)
        }

        if (operatorClicked) {
            val result = calculateResult()
            displayFragment.updateResult(result)
        }
    }

    // Operator button click handler
    fun onOperatorClick(operator: String) {
        // note: if equalsClicked is TRUE, the finalResult should become the first number in the
        //  new equation, with the operator appended to it; final result should be cleared
        if (equalsClicked) {
            operatorClicked = true
            displayFragment.updateEquation("$finalResult$operator")
            displayFragment.clearResult()
            equalsClicked = false
            // Exit the function after setting equalsClicked to false or the else block will run
            return
        } else {
            displayFragment.clearResult()
            displayFragment.updateEquation(operator)
            operatorClicked = true
            equalsClicked = false
        }
    }

    // Calculate the result of the equation
    private fun calculateResult() : String {
        val equation = displayFragment.getEquation()
        val calculator = Calculator()
        return calculator.calculate(equation)
    }
}

