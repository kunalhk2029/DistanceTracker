package com.example.distancetracker.ResultSave

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable



data class Results(val dist: String , val time : String) : Serializable


