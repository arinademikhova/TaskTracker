
package com.example.tasktracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tasktracker.ui.viewmodel.TaskUiEvent
import com.example.tasktracker.ui.viewmodel.TaskViewModel

@Composable
fun TaskEditScreen(
    viewModel: TaskViewModel,
    taskId: Long?,
    onBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val editingTask by viewModel.editingTask.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current

    var title by remember(taskId) { mutableStateOf("") }
    var description by remember(taskId) { mutableStateOf("") }
    var selectedCategoryId by remember(taskId) {
        mutableStateOf<Long?>(null)
    }

    var expanded by remember { mutableStateOf(false) }

    // Загрузка задачи для редактирования
    LaunchedEffect(taskId) {
        if (taskId != null) {
            viewModel.loadTask(taskId)
        } else {
            viewModel.clearEditingTask()
        }
    }

    // Заполнение полей существующими данными
    LaunchedEffect(editingTask, categories) {
        if (taskId != null && editingTask != null) {
            title = editingTask!!.title
            description = editingTask!!.description
            selectedCategoryId = editingTask!!.categoryId
        } else if (taskId == null && selectedCategoryId == null) {
            selectedCategoryId = categories.firstOrNull()?.id
        }
    }

    // Обработка одноразовых событий
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
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            TextButton(onClick = onBack) {
                Text("← Назад")
            }

            Text(
                text = if (taskId == null) {
                    "Новая задача"
                } else {
                    "Редактирование задачи"
                },
                style = MaterialTheme.typography.headlineMedium
            )

            if (isLoading && taskId != null && editingTask == null) {
                CircularProgressIndicator()
            } else {

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название задачи") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание задачи") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                // Выбор категории
                Text(
                    text = "Категория",
                    style = MaterialTheme.typography.titleMedium
                )

                Box {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val selectedCategory = categories.find {
                            it.id == selectedCategoryId
                        }

                        Text(
                            selectedCategory?.name ?: "Выберите категорию"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        categories.forEach { category ->

                            DropdownMenuItem(
                                text = {
                                    Text(category.name)
                                },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val categoryId = selectedCategoryId

                        when {
                            title.isBlank() -> {
                                // Пустое название не допускается
                            }

                            categoryId == null -> {
                                // Категория обязательна
                            }

                            taskId == null -> {
                                viewModel.addTask(
                                    title = title,
                                    description = description,
                                    categoryId = categoryId
                                )
                                onBack()
                            }

                            editingTask != null -> {
                                viewModel.updateTask(
                                    editingTask!!.copy(
                                        title = title,
                                        description = description,
                                        categoryId = categoryId
                                    )
                                )
                                onBack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && categories.isNotEmpty()
                ) {
                    Text(
                        if (taskId == null) "Создать задачу"
                        else "Сохранить изменения"
                    )
                }

                if (categories.isEmpty()) {
                    Text(
                        text = "Сначала создайте хотя бы одну категорию.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
