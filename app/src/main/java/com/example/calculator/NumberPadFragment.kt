package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentNumberPadBinding
import android.util.Log

class NumberPadFragment : Fragment() {

    private lateinit var binding: FragmentNumberPadBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentNumberPadBinding.inflate(inflater, container, false)

        // Get the parent activity
        val activity = requireActivity() as MainActivity

        // Create click listeners
        createNumberClickListeners(activity)
        createOperatorClickListeners(activity)

        return binding.root
    }

    // Create a click listener for each operator button
    private fun createOperatorClickListeners(activity: MainActivity) {
        binding.buttonDivide.setOnClickListener {
            activity.onOperatorClick("÷")
        }
        binding.buttonMultiply.setOnClickListener {
            activity.onOperatorClick("×")
        }
        binding.buttonSubtract.setOnClickListener {
            activity.onOperatorClick("-")
        }
        binding.buttonAdd.setOnClickListener {
            activity.onOperatorClick("+")
        }
        binding.buttonEquals.setOnClickListener {
            activity.onEqualsClick()
        }
    }

    // Create a click listener for each number button
    private fun createNumberClickListeners(activity: MainActivity) {
        binding.button0.setOnClickListener {
            activity.onNumberClick("0")
        }

        binding.button1.setOnClickListener {
            activity.onNumberClick("1")
        }

        binding.button2.setOnClickListener {
            activity.onNumberClick("2")
        }

        binding.button3.setOnClickListener {
            activity.onNumberClick("3")
        }

        binding.button4.setOnClickListener {
            activity.onNumberClick("4")
        }

        binding.button5.setOnClickListener {
            activity.onNumberClick("5")
        }

        binding.button6.setOnClickListener {
            activity.onNumberClick("6")
        }

        binding.button7.setOnClickListener {
            activity.onNumberClick("7")
        }

        binding.button8.setOnClickListener {
            activity.onNumberClick("8")
        }

        binding.button9.setOnClickListener {
            activity.onNumberClick("9")
        }
    }
}