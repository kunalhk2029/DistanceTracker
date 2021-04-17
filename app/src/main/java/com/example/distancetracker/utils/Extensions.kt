package com.example.distancetracker.utils

import android.view.View
import android.widget.Button

object Extensions {

    fun View.hide()
    {
        this.visibility = View.GONE
    }

    fun View.show()
    {
        this.visibility = View.VISIBLE
    }

    fun Button.enable()
    {
        this.isEnabled = true
    }

    fun Button.disable()
    {
        this.isEnabled = false
    }
}