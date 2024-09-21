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
        // Get the DisplayFragment and call its updateEquation method
        val displayFragment = supportFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment
        // TODO
    }


    // Called when a number button is clicked
   fun onNumberClick(number: String) {
        // Get the DisplayFragment and call its updateEquation method
        val displayFragment = supportFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment
        displayFragment.updateEquation(number)
    }

    // Called when an operator button is clicked
    fun onOperatorClick(operator: String) {
        // Get the DisplayFragment and call its updateEquation method
        val displayFragment = supportFragmentManager.findFragmentById(R.id.displayFragment) as DisplayFragment
        displayFragment.updateEquation(operator)
    }
}