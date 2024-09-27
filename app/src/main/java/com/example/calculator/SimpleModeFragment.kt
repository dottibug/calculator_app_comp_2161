package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentSimpleModeBinding

class SimpleModeFragment : Fragment() {
    private lateinit var binding: FragmentSimpleModeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using data binding
        binding = FragmentSimpleModeBinding.inflate(inflater, container, false)
        return binding.root
    }
}