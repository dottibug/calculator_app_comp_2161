package com.example.calculator

import android.os.Bundle
import android.widget.Toast
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
    private var equalsClicked : Boolean = false
    private var finalResult : String = ""
    private var currentEquation : String = ""

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
        currentEquation = ""
        finalResult = ""
        equalsClicked = false
        operatorClicked = false
    }

    // Equals button click handler
    fun onEqualsClick() {
        if (currentEquation.isEmpty()) {
            showToast("Invalid equation")
            return
        }

        // note: after clicking equals, the equation should be cleared and only the result show
        equalsClicked = true
        finalResult = calculateResult()
        displayFragment.updateResult(finalResult)
        currentEquation = ""
        displayFragment.displayEquation(currentEquation)
        operatorClicked = false
    }

    // Number button click handler
    fun onNumberClick(number: String) {

        val cursorPosition = displayFragment.getCursorPosition()
        val leftOfCursor = currentEquation.substring(0, cursorPosition)
        val rightOfCursor = currentEquation.substring(cursorPosition)

        currentEquation = "$leftOfCursor$number$rightOfCursor"

        // note: if equalsClicked is TRUE and a user clicks a number, that number should become
        //  the first number in the new equation and the result should be cleared
        if (equalsClicked) {
            currentEquation = ""
            displayFragment.displayEquation(currentEquation)
            updateEquation(number)
            displayFragment.displayEquation(currentEquation)
            displayFragment.clearResult()
            equalsClicked = false
        } else {
            displayFragment.displayEquation(currentEquation)
            val newCursorPosition = cursorPosition + 1
            displayFragment.setCursorPosition(newCursorPosition)
        }

        if (operatorClicked) {
            val result = calculateResult()
            displayFragment.updateResult(result)
        }
    }

    // Update the equation string
    private fun updateEquation(input: String) {
        currentEquation = "$currentEquation$input"
    }

    // Operator button click handler
    fun onOperatorClick(operator: String) {
        if (currentEquation.isEmpty() && finalResult.isEmpty()) {
            showToast("Invalid equation")
            return
        }

        // note: if equalsClicked is TRUE, the finalResult should become the first number in the
        //  new equation, with the operator appended to it; final result should be cleared
        if (equalsClicked) {
            operatorClicked = true
            currentEquation = "$finalResult$operator"
            displayFragment.displayEquation(currentEquation)
            displayFragment.clearResult()
            equalsClicked = false
            // Exit the function after setting equalsClicked to false or the else block will run
            return
        } else {
            displayFragment.clearResult()

            val cursorPosition = displayFragment.getCursorPosition()
            val leftOfCursor = currentEquation.substring(0, cursorPosition)
            val rightOfCursor = currentEquation.substring(cursorPosition)

            currentEquation = "$leftOfCursor$operator$rightOfCursor"
            displayFragment.displayEquation(currentEquation)

            val newCursorPosition = cursorPosition + 1
            displayFragment.setCursorPosition(newCursorPosition)

            // if newCursorPosition is < currentEquation.length, the operator was potentially added between
            // two numbers and should be calculated
            if (newCursorPosition < currentEquation.length) {
                val result = calculateResult()
                displayFragment.updateResult(result)
            }

            operatorClicked = true
            equalsClicked = false
        }
    }

    // Calculate the result of the equation
    private fun calculateResult() : String {
        val calculator = Calculator()
        return calculator.calculate(this, currentEquation)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    fun onBackspaceClick() {
        // get the cursor position and the current equation
        // if the cursor is at the beginning of the equation, do nothing
        // else, delete the character at the cursor position
        val cursorPosition = displayFragment.getCursorPosition()
        if (cursorPosition == 0) {
            return
        } else {
            // Delete the character at the cursor position
            val leftOfCursor = currentEquation.substring(0, cursorPosition - 1)
            val rightOfCursor = currentEquation.substring(cursorPosition)
            currentEquation = "$leftOfCursor$rightOfCursor"
            displayFragment.displayEquation(currentEquation)
            val newCursorPosition = cursorPosition - 1
            displayFragment.setCursorPosition(newCursorPosition)
        }
    }
}

