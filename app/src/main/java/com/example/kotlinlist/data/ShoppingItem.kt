package com.example.kotlinlist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_table")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val quantity: Int,
    val isChecked: Boolean = false
)