
package com.example.tasktracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tasktracker.data.local.Category
import com.example.tasktracker.ui.viewmodel.TaskUiEvent
import com.example.tasktracker.ui.viewmodel.TaskViewModel

private val categoryColors = listOf(
    0xFFE57373L,
    0xFF64B5F6L,
    0xFF81C784L,
    0xFFFFB74DL,
    0xFFBA68C8L,
    0xFF4DB6ACL
)

@Composable
fun CategoriesScreen(
    viewModel: TaskViewModel,
    onOpenTasks: (Long?) -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    // Сбрасываем фильтр при открытии экрана категорий
    LaunchedEffect(Unit) {
        viewModel.filterByCategory(null)
    }

    // Получение одноразовых событий
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is TaskUiEvent.ShowMessage -> {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Text("+")
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Мои категории",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Организуйте задачи по категориям",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.filterByCategory(null)
                    onOpenTasks(null)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Все задачи")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Категорий пока нет. Создайте первую!")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = categories,
                        key = { it.id }
                    ) { category ->

                        val taskCount = tasks.count {
                            it.task.categoryId == category.id
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(
                                            color = Color(
                                                category.colorHex.toULong()
                                            ),
                                            shape = CircleShape
                                        )
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = "Задач: $taskCount",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                TextButton(
                                    onClick = {
                                        viewModel.filterByCategory(category.id)
                                        onOpenTasks(category.id)
                                    }
                                ) {
                                    Text("Открыть")
                                }

                                TextButton(
                                    onClick = {
                                        categoryToDelete = category
                                    }
                                ) {
                                    Text("Удалить")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог добавления категории
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var selectedColor by remember {
            mutableLongStateOf(categoryColors.first())
        }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
            },
            title = {
                Text("Новая категория")
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Название категории") },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Выберите цвет:")

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        categoryColors.forEach { color ->

                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape = CircleShape,
                                color = Color(color.toULong()),
                                onClick = {
                                    selectedColor = color
                                },
                                border = if (selectedColor == color) {
                                    androidx.compose.foundation.BorderStroke(
                                        3.dp,
                                        MaterialTheme.colorScheme.onSurface
                                    )
                                } else null
                            ) {}
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addCategory(name, selectedColor)
                        showAddDialog = false
                    }
                ) {
                    Text("Добавить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }

    // Подтверждение удаления категории
    categoryToDelete?.let { category ->

        AlertDialog(
            onDismissRequest = {
                categoryToDelete = null
            },
            title = {
                Text("Удалить категорию?")
            },
            text = {
                Text(
                    "Категория «${category.name}» будет удалена. " +
                            "Все задачи этой категории также будут удалены. " +
                            "Продолжить?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(category)
                        categoryToDelete = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        categoryToDelete = null
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}
