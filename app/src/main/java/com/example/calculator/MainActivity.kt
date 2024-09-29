package com.example.calculator

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import com.example.calculator.databinding.ActivityMainBinding

//
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var isScientificMode : Boolean = false


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

    // Navigate to the correct fragment based on the calculator mode
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
}

