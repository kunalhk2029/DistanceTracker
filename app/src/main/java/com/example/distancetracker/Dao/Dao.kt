package com.example.distancetracker.Dao


import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.Dao
import com.example.distancetracker.Store

//@Dao
//interface Dao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//     fun insert(article : Store) : Long
//    // Id that was inserted
//    @Query("SELECT * FROM Data_table")
//    fun getAllArticels() : LiveData<List<Store>>
//
//    @Delete()
//     fun delete(article: Store)
//
//}