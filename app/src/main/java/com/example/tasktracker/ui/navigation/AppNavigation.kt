
package com.example.tasktracker.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Названия маршрутов
object AppRoutes {
    const val CATEGORIES = "categories"
    const val TASKS = "tasks"
    const val TASK_EDIT = "task_edit"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {

    NavHost(
        navController = navController,
        startDestination = AppRoutes.CATEGORIES
    ) {

        // Экран категорий
        composable(AppRoutes.CATEGORIES) {
            Text("Экран категорий")
        }

        // Экран задач
        composable(AppRoutes.TASKS) {
            Text("Экран задач")
        }

        // Экран добавления и редактирования задачи
        composable(AppRoutes.TASK_EDIT) {
            Text("Добавление / редактирование задачи")
        }
    }
}
