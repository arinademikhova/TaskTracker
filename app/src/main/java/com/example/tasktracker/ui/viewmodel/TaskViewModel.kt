
package com.example.tasktracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tasktracker.data.local.Category
import com.example.tasktracker.data.local.Task
import com.example.tasktracker.data.local.TaskWithCategory
import com.example.tasktracker.data.repository.AppRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Одноразовые события интерфейса
sealed class TaskUiEvent {
    data class ShowMessage(val message: String) : TaskUiEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(
    private val repository: AppRepository
) : ViewModel() {

    // Список категорий
    val categories: StateFlow<List<Category>> =
        repository.getAllCategories().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Выбранная категория для фильтрации
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)

    val selectedCategoryId: StateFlow<Long?> =
        _selectedCategoryId.asStateFlow()

    // Список задач с учётом выбранного фильтра
    val tasks: StateFlow<List<TaskWithCategory>> =
        _selectedCategoryId
            .flatMapLatest { categoryId ->
                if (categoryId == null) {
                    repository.getAllTasks()
                } else {
                    repository.getTasksByCategory(categoryId)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean> =
        _isLoading.asStateFlow()

    // Задача, которую редактируем
    private val _editingTask = MutableStateFlow<Task?>(null)

    val editingTask: StateFlow<Task?> =
        _editingTask.asStateFlow()

    // Одноразовые события
    private val _events = MutableSharedFlow<TaskUiEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )

    val events = _events.asSharedFlow()

    // Добавление категории
    fun addCategory(name: String, colorHex: Long) {
        if (name.isBlank()) {
            sendMessage("Введите название категории")
            return
        }

        executeOperation("Категория добавлена") {
            repository.insertCategory(
                Category(
                    name = name.trim(),
                    colorHex = colorHex
                )
            )
        }
    }

    // Обновление категории
    fun updateCategory(category: Category) {
        if (category.name.isBlank()) {
            sendMessage("Введите название категории")
            return
        }

        executeOperation("Категория обновлена") {
            repository.updateCategory(
                category.copy(name = category.name.trim())
            )
        }
    }

    // Удаление категории
    fun deleteCategory(category: Category) {
        executeOperation("Категория и её задачи удалены") {
            repository.deleteCategory(category)
        }
    }

    // Добавление задачи
    fun addTask(
        title: String,
        description: String,
        categoryId: Long,
        onSuccess: () -> Unit = {}
    ) {
        if (title.isBlank()) {
            sendMessage("Введите название задачи")
            return
        }

        executeOperation(onSuccess = onSuccess) {
            repository.insertTask(
                Task(
                    title = title.trim(),
                    description = description.trim(),
                    categoryId = categoryId
                )
            )
        }
    }

    // Обновление задачи
    fun updateTask(
        task: Task,
        onSuccess: () -> Unit = {}
    ) {
        if (task.title.isBlank()) {
            sendMessage("Введите название задачи")
            return
        }

        executeOperation(onSuccess = onSuccess) {
            repository.updateTask(
                task.copy(
                    title = task.title.trim(),
                    description = task.description.trim()
                )
            )
        }
    }

    // Загрузка задачи для редактирования
    fun loadTask(taskId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _editingTask.value = null

                _editingTask.value = withContext(Dispatchers.IO) {
                    repository.getTaskById(taskId)
                }

                if (_editingTask.value == null) {
                    sendMessage("Задача не найдена")
                }
            } catch (e: Exception) {
                sendMessage("Ошибка загрузки задачи")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Очистка выбранной задачи
    fun clearEditingTask() {
        _editingTask.value = null
    }

    // Удаление задачи
    fun deleteTask(task: Task) {
        executeOperation("Задача удалена") {
            repository.deleteTask(task)
        }
    }

    // Изменение статуса выполнения задачи
    fun toggleTaskCompletion(task: Task) {
        executeOperation("Статус задачи изменён") {
            repository.updateTaskStatus(
                taskId = task.id,
                isCompleted = !task.isCompleted
            )
        }
    }

    // Выбор категории для фильтрации
    fun filterByCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    // Выполнение операции с базой данных
    private fun executeOperation(
        successMessage: String? = null,
        onSuccess: () -> Unit = {},
        operation: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                withContext(Dispatchers.IO) {
                    operation()
                }

                successMessage?.let { sendMessage(it) }

                // Выполняется только после успешной операции
                onSuccess()

            } catch (e: Exception) {
                sendMessage(
                    "Ошибка: ${e.localizedMessage ?: "не удалось выполнить операцию"}"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Отправка сообщения интерфейсу
    private fun sendMessage(message: String) {
        _events.tryEmit(TaskUiEvent.ShowMessage(message))
    }
}
