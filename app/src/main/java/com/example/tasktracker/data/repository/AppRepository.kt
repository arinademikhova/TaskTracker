
package com.example.tasktracker.data.repository

import com.example.tasktracker.data.local.Category
import com.example.tasktracker.data.local.CategoryDao
import com.example.tasktracker.data.local.Task
import com.example.tasktracker.data.local.TaskDao
import com.example.tasktracker.data.local.TaskWithCategory
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val categoryDao: CategoryDao,
    private val taskDao: TaskDao
) {

    // Получение всех категорий
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories()
    }

    // Получение категории по ID
    suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)
    }

    // Добавление категории
    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category)
    }

    // Обновление категории
    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category)
    }

    // Удаление категории
    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category)
    }

    // ---------------- TASKS ----------------

    // Получение всех задач вместе с категориями
    fun getAllTasks(): Flow<List<TaskWithCategory>> {
        return taskDao.getAllTasks()
    }

    // Получение задач определённой категории
    fun getTasksByCategory(categoryId: Long): Flow<List<TaskWithCategory>> {
        return taskDao.getTasksByCategory(categoryId)
    }

    // Получение задачи по ID
    suspend fun getTaskById(id: Long): Task? {
        return taskDao.getTaskById(id)
    }

    // Добавление задачи
    suspend fun insertTask(task: Task): Long {
        return taskDao.insertTask(task)
    }

    // Обновление задачи
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    // Удаление задачи
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    // Изменение статуса задачи
    suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean) {
        taskDao.updateTaskStatus(taskId, isCompleted)
    }

    // Получение количества задач в категории
    fun getTaskCountByCategory(categoryId: Long): Flow<Int> {
        return taskDao.getTaskCountByCategory(categoryId)
    }
}
