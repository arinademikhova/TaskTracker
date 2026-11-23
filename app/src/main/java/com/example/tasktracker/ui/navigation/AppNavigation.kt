
package com.example.tasktracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tasktracker.ui.screens.CategoriesScreen
import com.example.tasktracker.ui.screens.TasksScreen
import com.example.tasktracker.ui.screens.TaskEditScreen
import com.example.tasktracker.ui.viewmodel.TaskViewModel

// Названия маршрутов
object AppRoutes {
    const val CATEGORIES = "categories"
    const val TASKS = "tasks"
    const val TASK_EDIT = "task_edit"
}

@Composable
fun AppNavigation(
    viewModel: TaskViewModel,
    navController: NavHostController = rememberNavController()
) {

    NavHost(
        navController = navController,
        startDestination = AppRoutes.CATEGORIES
    ) {

        // Экран категорий
        composable(AppRoutes.CATEGORIES) {
            CategoriesScreen(
                viewModel = viewModel,
                onOpenTasks = { categoryId ->
                    viewModel.filterByCategory(categoryId)
                    navController.navigate(AppRoutes.TASKS)
                }
            )
        }

        // Экран задач
        composable(AppRoutes.TASKS) {
            TasksScreen(
                viewModel = viewModel,

                onAddTask = {
                    navController.navigate(AppRoutes.TASK_EDIT)
                },

                onEditTask = { taskId ->
                    navController.navigate(
                        "${AppRoutes.TASK_EDIT}?taskId=$taskId"
                    )
                },

                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Экран добавления и редактирования задачи
        composable(
            route = "${AppRoutes.TASK_EDIT}?taskId={taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->

            val taskId = backStackEntry.arguments
                ?.getLong("taskId")
                ?.takeIf { it != -1L }

            TaskEditScreen(
                viewModel = viewModel,
                taskId = taskId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
