
package com.example.tasktracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // Получение всех задач
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskWithCategory>>

    // Получение задач определённой категории
    @Transaction
    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY createdAt DESC")
    fun getTasksByCategory(categoryId: Long): Flow<List<TaskWithCategory>>

    // Получение задачи по ID
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    // Добавление задачи
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    // Обновление задачи
    @Update
    suspend fun updateTask(task: Task)

    // Удаление задачи
    @Delete
    suspend fun deleteTask(task: Task)

    // Изменение статуса выполнения задачи
    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, isCompleted: Boolean)

    // Получение количества задач в категории
    @Query("SELECT COUNT(*) FROM tasks WHERE categoryId = :categoryId")
    fun getTaskCountByCategory(categoryId: Long): Flow<Int>

}
