package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentDisplayBinding

// Fragment to display the running equation and result of the calculation
class DisplayFragment : Fragment() {

    private lateinit var binding: FragmentDisplayBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }
}