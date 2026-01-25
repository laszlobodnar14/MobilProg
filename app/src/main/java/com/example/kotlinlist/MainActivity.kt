package com.example.kotlinlist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlinlist.data.ShoppingItem
import com.example.kotlinlist.ui.theme.KotlinListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KotlinListTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ShoppingListScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(viewModel: ShoppingViewModel = viewModel()) {

    val items by viewModel.items.collectAsState()


    val allItems by viewModel.allItems.collectAsState()

    val searchText by viewModel.searchText.collectAsState()

    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ShoppingItem?>(null) }


    val totalItems = allItems.size
    val completedItems = allItems.count { it.isChecked }
    val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f


    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Bevásárlólista") },
                    actions = {

                        IconButton(onClick = { shareList(context, allItems) }) {
                            Icon(Icons.Default.Share, contentDescription = "Megosztás")
                        }

                        IconButton(onClick = { viewModel.deleteAllItems() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Összes törlése")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )

                OutlinedTextField(
                    value = searchText,
                    onValueChange = viewModel::onSearchTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Keresés...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )


                if (totalItems > 0) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Haladás:", style = MaterialTheme.typography.labelMedium)
                            Text("$completedItems / $totalItems", style = MaterialTheme.typography.labelMedium)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                itemToEdit = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Hozzáadás")
            }
        }
    ) { innerPadding ->

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (searchText.isBlank()) {
                    Text("A lista üres.")
                } else {
                    Text("Nincs találat erre: '$searchText'")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(items) { item ->
                    ShoppingListItem(
                        item = item,
                        onEdit = {
                            itemToEdit = item
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteItem(item) },
                        onCheckedChange = { isChecked ->
                            viewModel.updateItem(item.copy(isChecked = isChecked))
                        }
                    )
                }
            }
        }

        if (showDialog) {
            ItemDialog(
                initialName = itemToEdit?.name ?: "",
                initialQuantity = itemToEdit?.quantity?.toString() ?: "",
                isEditing = itemToEdit != null,
                onDismiss = { showDialog = false },
                onSave = { name, quantity ->
                    if (itemToEdit == null) {
                        viewModel.addItem(name, quantity)
                    } else {
                        val updatedItem = itemToEdit!!.copy(
                            name = name,
                            quantity = quantity.toIntOrNull() ?: 1
                        )
                        viewModel.updateItem(updatedItem)
                    }
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun ShoppingListItem(
    item: ShoppingItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),

        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange
            )


            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,

                    textDecoration = if (item.isChecked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                )
                Text(
                    text = "Mennyiség: ${item.quantity}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }


            if (!item.isChecked) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Szerkesztés", tint = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Törlés", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}


@Composable
fun ItemDialog(
    initialName: String,
    initialQuantity: String,
    isEditing: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {

    var name by remember { mutableStateOf(initialName) }
    var quantity by remember { mutableStateOf(initialQuantity) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditing) "Termék módosítása" else "Új termék") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Termék neve") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it },
                    label = { Text("Mennyiség") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, quantity) }) {
                Text(if (isEditing) "Módosítás" else "Mentés")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Mégse")
            }
        }
    )
}


fun shareList(context: android.content.Context, items: List<ShoppingItem>) {

    val text = StringBuilder()
    text.append("🛒 Bevásárlólistám:\n\n")

    items.forEach { item ->
        val status = if (item.isChecked) "[✔ Megvéve]" else "[ ]"
        text.append("$status ${item.name} (${item.quantity})\n")
    }

    val sendIntent = android.content.Intent().apply {
        action = android.content.Intent.ACTION_SEND
        putExtra(android.content.Intent.EXTRA_TEXT, text.toString())
        type = "text/plain"
    }

    val shareIntent = android.content.Intent.createChooser(sendIntent, "Lista küldése...")
    context.startActivity(shareIntent)
}