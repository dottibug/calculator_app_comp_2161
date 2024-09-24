package com.example.calculator

import android.os.Bundle
import android.util.Log
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
    private var openBracketClicked : Boolean = false
    private var userSettingsDecimalPlaces : String = "10" // default to 10 decimal places
    private var digitCount : Int = 0

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
        digitCount = 0
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

    // Number button click handler
    fun onNumberClick(number: String) {
        if (digitCount > 12) {
            showToast("Maximum digits reached")
            return
        }

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

        if (operatorClicked || openBracketClicked) {
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
        if (digitCount > 12) {
            showToast("Maximum digits reached")
            return
        }

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

            // if cursor is at the end of the equation, change the previous character to the new operator
            val lastChar = currentEquation.last()
            if (cursorPosition == currentEquation.length && (lastChar == '+' || lastChar == '~' || lastChar == '×' || lastChar == '÷')) {
                val newEquation = currentEquation.replace(lastChar.toString(), operator)
                currentEquation = newEquation
                displayFragment.displayEquation(currentEquation)
                operatorClicked = true
                equalsClicked = false
                return
            }

            else {
                val leftOfCursor = currentEquation.substring(0, cursorPosition)
                val rightOfCursor = currentEquation.substring(cursorPosition)

                // Prevent user from entering an operator to the left of an opening bracket
                if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '(') {
                    return
                }

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

    fun onBracketClick(bracket: String) {
        if (digitCount > 12) {
            showToast("Maximum digits reached")
            return
        }

        openBracketClicked = bracket == "("

        val cursorPosition = displayFragment.getCursorPosition()
        val leftOfCursor = currentEquation.substring(0, cursorPosition)
        val rightOfCursor = currentEquation.substring(cursorPosition)

        // Prevent user from entering closing bracket as the first character in an equation
        if (leftOfCursor.isEmpty() && bracket == ")") {
            return
        }

        // Prevent user from entering a closing bracket right beside an opening bracket
        if ((leftOfCursor.isNotEmpty() && leftOfCursor.last() == '(' && bracket == ")")) {
            return
        }

        // Prevent user from entering a closing bracket to the right of an operator or decimal
        if ((leftOfCursor.isNotEmpty() && leftOfCursor.last() in setOf('+', '~', '×', '÷', '.') &&
                    bracket == ")")) {
            return
        }

        // Prevent user from entering a closing bracket if there is no opening bracket
        if (!leftOfCursor.contains('(') && bracket == ")") {
            return
        }

        // Prevent user from entering an opening bracket if there is an operator to the immediate
        // right
        if (bracket == "(" && rightOfCursor.isNotEmpty() && rightOfCursor.first() in setOf('+', '~', '×', '÷')) {
            return
        }

        // Add a multiplication symbol if user enters a closing bracket right beside an opening
        // bracket
        if ((leftOfCursor.isNotEmpty() && leftOfCursor.last() == ')' && bracket == "(")) {
            val multiplication = "×("
            currentEquation = "$leftOfCursor$multiplication$rightOfCursor"
            updateEquation(cursorPosition + 1)
            updateResult()
            operatorClicked = false
            equalsClicked = false
            return
        }

        currentEquation = "$leftOfCursor$bracket$rightOfCursor"
        updateEquation(cursorPosition)
        updateResult()

        operatorClicked = false
        equalsClicked = false
    }

    private fun updateEquation(cursorPosition: Int) {
        displayFragment.displayEquation(currentEquation)
        val newCursorPosition = cursorPosition + 1
        displayFragment.setCursorPosition(newCursorPosition)
    }

    private fun updateResult() {
        val result = calculateResult()
        displayFragment.updateResult(result)
    }

    fun onDecimalClick() {
        if (digitCount > 12) {
            showToast("Maximum digits reached")
            return
        }

        val cursorPosition = displayFragment.getCursorPosition()
        val leftOfCursor = currentEquation.substring(0, cursorPosition)
        val rightOfCursor = currentEquation.substring(cursorPosition)

        // Prevent user from entering two decimal points in a row
        if ((leftOfCursor.isNotEmpty() && leftOfCursor.last() == '.')
            || (rightOfCursor.isNotEmpty() && rightOfCursor.first() == '.')) {
            return
       }

        // If there is a leading decimal point, add a 0 before it
        if (leftOfCursor.isEmpty() || leftOfCursor.last() in setOf('+', '~', '×', '÷', '(')) {
            val leadingZero = "0."
            currentEquation = "$leftOfCursor$leadingZero$rightOfCursor"
        } else {
            currentEquation = "$leftOfCursor.$rightOfCursor"
        }

        displayFragment.displayEquation(currentEquation)
        val newCursorPosition = cursorPosition + 1
        displayFragment.setCursorPosition(newCursorPosition)

        operatorClicked = false
        equalsClicked = false
        openBracketClicked = false

        val result = calculateResult()
        displayFragment.updateResult(result)
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

    fun onSignClick() {
        Log.i("testcat", "onSignClick")

        val cursorPosition = displayFragment.getCursorPosition()
        val leftOfCursor = currentEquation.substring(0, cursorPosition)
        val rightOfCursor = currentEquation.substring(cursorPosition)

        // If equation is empty -> (-
        if (currentEquation.isEmpty()) {
            currentEquation = "-("
            displayFragment.displayEquation(currentEquation)
            val newCursorPosition = 2
            displayFragment.setCursorPosition(newCursorPosition)
            return
        }

        // If equation is just a number -> change pos num to neg, and neg num to pos
            // 55 -> (-55
            // (-55  -> 55
        if (isNumber(currentEquation)) {
            if (currentEquation.contains("(-")) {
                currentEquation = currentEquation.replace("(-", "")
                displayFragment.displayEquation(currentEquation)
                val newCursorPosition = cursorPosition - 2
                displayFragment.setCursorPosition(newCursorPosition)
                return
            } else {
                currentEquation = "(-$currentEquation"
                displayFragment.displayEquation(currentEquation)
                val newCursorPosition = cursorPosition + 2
                displayFragment.setCursorPosition(newCursorPosition)
                return
            }

        }

        // If the last character to the left of the cursor is an operator -> (-
            // 55+ -> 55+(-
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() in setOf('+', '~', '×', '÷')) {
            val negativeSign = "(-"
            currentEquation = "$leftOfCursor$negativeSign$rightOfCursor"
            displayFragment.displayEquation(currentEquation)
            val newCursorPosition = cursorPosition + 2
            displayFragment.setCursorPosition(newCursorPosition)
            return
        }

        // If the last character to the left of the cursor is an opening bracket ->  -
            // 55+(  => 55+(-
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '(') {
            val negativeSign = "-"
            currentEquation = "$leftOfCursor$negativeSign$rightOfCursor"
            displayFragment.displayEquation(currentEquation)
            val newCursorPosition = cursorPosition + 1
            displayFragment.setCursorPosition(newCursorPosition)
        }

        // If the last character to the left of the cursor is a negative sign -> remove it
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last() == '-') {
            currentEquation = currentEquation.dropLast(1)
            displayFragment.displayEquation(currentEquation)
            val newCursorPosition = cursorPosition - 1
            displayFragment.setCursorPosition(newCursorPosition)
        }

        // If the last character to the left of the cursor is a number, get the substring of
        // digits between most recent operator and cursor position, and change the sign of that number
            // 55+63 -> 55+(-63
            // 55+(-63  ->  55+63
        if (leftOfCursor.isNotEmpty() && leftOfCursor.last().isDigit()) {
            val numberSubstring = getSubstringAfterLastOperator(leftOfCursor, cursorPosition)

            if (numberSubstring.contains('-')) {
                val newNumber = numberSubstring.replace("(-", "")
                val startIndex = cursorPosition - numberSubstring.length
                currentEquation = currentEquation.replaceRange(startIndex, cursorPosition, newNumber)
                displayFragment.displayEquation(currentEquation)
                val newCursorPosition = cursorPosition - 2
                displayFragment.setCursorPosition(newCursorPosition)
                val result = calculateResult()
                displayFragment.updateResult(result)
            } else {
                val newNumber = "(-$numberSubstring"
                val startIndex = cursorPosition - numberSubstring.length
                currentEquation = currentEquation.replaceRange(startIndex, cursorPosition, newNumber)
                displayFragment.displayEquation(currentEquation)
                val newCursorPosition = cursorPosition + 2
                displayFragment.setCursorPosition(newCursorPosition)
                val result = calculateResult()
                displayFragment.updateResult(result)
            }
        }
    }

    private fun getSubstringAfterLastOperator(input: String, cursorPosition: Int) : String {
        val lastOperatorIndex = input.lastIndexOfAny(charArrayOf('+', '~', '×', '÷'))
        return input.substring(lastOperatorIndex + 1, cursorPosition)
    }


    private fun isNumber(input: String): Boolean {
        // If equation does not contain operators, it is a number
        if (input.contains("+") || input.contains("~") || input.contains("×") || input.contains
                ("÷")
        ) {
            return false
        }
        return true
    }

}

