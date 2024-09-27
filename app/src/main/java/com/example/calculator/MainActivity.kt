package com.example.calculator

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import com.example.calculator.databinding.ActivityMainBinding

//
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var operatorClicked : Boolean = false
    private lateinit var displayFragment: DisplayFragment
    private var equalsClicked : Boolean = false
    private var finalResult : String = ""
    private var currentEquation : String = ""
    private var openBracketClicked : Boolean = false
    private var userSettingsDecimalPlaces : String = "10" // default to 10 decimal places
    private var digitCount : Int = 0
    private var memory : String = ""

    private var isScientificMode : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Inflates the layout using the data binding object and sets the root view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sets up the window insets so app content is not covered by system UI
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // NAVIGATION FUNCTIONS
    fun onCalculatorModeClick(navController: NavController) {
        isScientificMode = !isScientificMode

        if (isScientificMode) {
            // Navigate to scientific mode
            navController.navigate(SimpleModeFragmentDirections.actionSimpleModeFragmentToScientificModeFragment())
        } else {
            // Navigate to simple mode
            navController.navigate(ScientificModeFragmentDirections.actionScientificModeFragmentToSimpleModeFragment())
        }
    }


    // Equals button click handler
    fun onEqualsClick() {
        // TEST START
        if (digitCount > 12) {
            showToast("Maximum digits reached")
            return
        }
        // TEST END

        if (currentEquation.isEmpty() || currentEquation.last() == '+' || currentEquation.last() == '~' || currentEquation.last() == '×' || currentEquation.last() == '÷') {
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



    // Calculate the result of the equation
    private fun calculateResult() : String {
        val calculator = Calculator()

        // Check for missing closing brackets
        val openBrackets = currentEquation.count { it == '(' }
        val closedBrackets = currentEquation.count { it == ')' }
        if (openBrackets > closedBrackets) {
            // Add missing closing brackets ONLY if there is a number somewhere after it
            if (currentEquation.substringAfterLast('(').any { it.isDigit() }) {
                val missingBrackets = openBrackets - closedBrackets
                val equationWithAutoBrackets = currentEquation + ")".repeat(missingBrackets)
                openBracketClicked = false

                // Check length of result
                val result = calculator.calculate(this, equationWithAutoBrackets, userSettingsDecimalPlaces)
                if (checkDigitCount(result) > 12) {
                    showToast("Maximum digits reached")
                    // TODO Change this to return the most recent result and equation that was
                    //  not too long (instead of returning a blank string)??
                    return ""
                } else {
                    return result
                }

//                return calculator.calculate(this, equationWithAutoBrackets, userSettingsDecimalPlaces)
            } else {
                return if (equalsClicked) "error" else ""
            }
        }
        displayFragment.displayEquation(currentEquation)

        // Check length of result
        val result = calculator.calculate(this, currentEquation, userSettingsDecimalPlaces)
        if (checkDigitCount(result) > 12) {
             showToast("Maximum digits reached")
            // TODO Change this to return the most recent result and equation that was
            //  not too long (instead of returning a blank string)??
            return ""
        }

        return result
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkDigitCount(result : String) : Int {
        digitCount = result.count { it.isDigit() }
        if (digitCount > 12) {
            return digitCount
        } else {
            return digitCount
        }
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

            if (leftOfCursor.last() == '-') {
                currentEquation = leftOfCursor.dropLast(1)
                displayFragment.displayEquation(currentEquation)
                val newCursorPosition = cursorPosition - 1
                displayFragment.setCursorPosition(newCursorPosition)
            } else {
                currentEquation = "$leftOfCursor$rightOfCursor"
                displayFragment.displayEquation(currentEquation)
                val newCursorPosition = cursorPosition - 1
                displayFragment.setCursorPosition(newCursorPosition)
                val result = calculateResult()
                displayFragment.updateResult(result)
            }
        }
    }


    ////////////// MEMORY FUNCTIONS //////////////
    fun onMemoryClear() {
        // Clear memory to ""
    }

    fun onMemoryStore() {
        // If no finalResult, check equation for ONE number to store
        // If there is a finalResult, store that
    }

    fun onMemoryRecall() {}

    fun onMemoryAdd() {}

    fun onMemorySubtract() {}

}

