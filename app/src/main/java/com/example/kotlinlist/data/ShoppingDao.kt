package com.example.kotlinlist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {

    @Query("SELECT * FROM shopping_table WHERE name LIKE '%' || :searchQuery || '%' ORDER BY isChecked ASC, id DESC")
    fun getItems(searchQuery: String): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_table")
    fun getAllItems(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: ShoppingItem)

    @Update
    suspend fun update(item: ShoppingItem)

    @Delete
    suspend fun delete(item: ShoppingItem)


    @Query("DELETE FROM shopping_table")
    suspend fun deleteAll()


    @Query("DELETE FROM shopping_table WHERE isChecked = 1")
    suspend fun deleteCompleted()
}