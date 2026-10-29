
package com.example.tasknest.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao
) {
    val allTasks: Flow<List<TaskEntity>> =
        taskDao.getAllTasks()

    val pendingTasks: Flow<List<TaskEntity>> =
        taskDao.getPendingTasks()

    val completedTasks: Flow<List<TaskEntity>> =
        taskDao.getCompletedTasks()

    suspend fun insertTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun setTaskCompleted(
        taskId: Long,
        completed: Boolean
    ) {
        taskDao.setTaskCompleted(taskId, completed)
    }

    suspend fun deleteAllTasks() {
        taskDao.deleteAllTasks()
    }
}
