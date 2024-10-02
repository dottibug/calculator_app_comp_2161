package com.example.calculator

import android.content.Context
import android.widget.Toast

class AppUtils {
    var toast: Toast? = null

    // Show toast message
    fun showToast(context : Context, message: String) {
        toast?.cancel() // Cancel the previous Toast if it exists
        toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toast?.show()
    }
}