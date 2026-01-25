package com.example.kotlinlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlinlist.data.ShoppingDatabase
import com.example.kotlinlist.data.ShoppingItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ShoppingDatabase.getDatabase(application).shoppingDao()


    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()


    val items = _searchText.flatMapLatest { query ->
        dao.getItems(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allItems = dao.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    fun addItem(name: String, quantity: String) {
        if (name.isNotBlank() && quantity.isNotBlank()) {
            viewModelScope.launch {
                dao.insert(ShoppingItem(name = name, quantity = quantity.toIntOrNull() ?: 1))
            }
        }
    }

    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch { dao.update(item) }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch { dao.delete(item) }
    }


    fun deleteAllItems() {
        viewModelScope.launch { dao.deleteAll() }
    }
}