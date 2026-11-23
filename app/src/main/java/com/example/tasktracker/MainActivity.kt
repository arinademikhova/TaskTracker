
package com.example.tasktracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.tasktracker.data.local.AppDatabase
import com.example.tasktracker.data.repository.AppRepository
import com.example.tasktracker.ui.navigation.AppNavigation
import com.example.tasktracker.ui.theme.TaskTrackerTheme
import com.example.tasktracker.ui.viewmodel.TaskViewModel
import com.example.tasktracker.ui.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Создание базы данных
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "task_tracker_database"
        ).build()

        // Создание репозитория
        val repository = AppRepository(
            categoryDao = database.categoryDao(),
            taskDao = database.taskDao()
        )

        // Создание фабрики ViewModel
        val factory = TaskViewModelFactory(repository)

        // Запуск интерфейса
        setContent {
            TaskTrackerTheme {

                // Получение ViewModel через Factory
                val taskViewModel: TaskViewModel = viewModel(
                    factory = factory
                )

                // Отображение навигации
                AppNavigation(
                    viewModel = taskViewModel
                )
            }
        }
    }
}
