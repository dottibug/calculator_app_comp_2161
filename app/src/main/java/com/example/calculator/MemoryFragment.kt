package com.example.calculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.calculator.databinding.FragmentMemoryBinding

class MemoryFragment : Fragment() {

    private lateinit var binding: FragmentMemoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the dataBinding layout for this fragment
        binding = FragmentMemoryBinding.inflate(inflater, container, false)

        // Get the parent activity
        val activity = requireActivity() as MainActivity

        // Create click listeners
        binding.buttonMemoryClear.setOnClickListener {  }
        binding.buttonMemoryStore.setOnClickListener { activity.onMemoryStore() }
        binding.buttonMemoryRecall.setOnClickListener {  }
        binding.buttonMemoryAdd.setOnClickListener {  }
        binding.buttonMemorySubtract.setOnClickListener {  }

        return binding.root
    }
}