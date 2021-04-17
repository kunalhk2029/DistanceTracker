package com.example.distancetracker

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Data_table")
data class Store(@PrimaryKey(autoGenerate=true) val id: Int, @ColumnInfo(name="Date")val date :String, @ColumnInfo(name="Dist")val dist:String,
            @ColumnInfo(name="Time")val time : String)

