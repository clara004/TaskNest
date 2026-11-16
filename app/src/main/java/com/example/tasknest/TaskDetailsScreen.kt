
package com.example.tasknest

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasknest.data.TaskEntity
import com.example.tasknest.data.TaskRepository
import kotlinx.coroutines.launch

@Composable
fun TaskDetailsScreen(
    task: TaskEntity,
    repository: TaskRepository,
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    onShare: (TaskEntity) -> Unit = {}
) {
    val scope = rememberCoroutineScope()

    val tasks by repository.allTasks.collectAsState(
        initial = emptyList()
    )

    val currentTask = tasks.find { it.id == task.id } ?: task

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    // Editable fields
    var title by remember(task.id) {
        mutableStateOf(task.title)
    }

    var subject by remember(task.id) {
        mutableStateOf(task.subject)
    }

    var dueDate by remember(task.id) {
        mutableStateOf(task.dueDate)
    }

    var priority by remember(task.id) {
        mutableStateOf(task.priority)
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    // Delete confirmation
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Delete Task?")
            },
            text = {
                Text("Are you sure you want to delete this task?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.deleteTask(currentTask)
                            showDeleteDialog = false
                            onDeleted()
                        }
                    }
                ) {
                    Text(
                        "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {

        // Back button
        TextButton(
            onClick = onBack
        ) {
            Text("← Back to Schedule")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isEditing) "Edit Task" else "Task Details",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Task information card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                if (isEditing) {

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            errorMessage = ""
                        },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Subject
                    OutlinedTextField(
                        value = subject,
                        onValueChange = {
                            subject = it
                            errorMessage = ""
                        },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Due date
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = {
                            dueDate = it
                            errorMessage = ""
                        },
                        label = { Text("Due Date") },
                        placeholder = {
                            Text("yyyy-MM-dd")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Priority selection
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Low", "Medium", "High").forEach { option ->
                            FilterChip(
                                selected = priority == option,
                                onClick = {
                                    priority = option
                                },
                                label = {
                                    Text(option)
                                }
                            )
                        }
                    }

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                } else {

                    Text(
                        text = currentTask.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider()

                    DetailItem(
                        label = "Subject",
                        value = currentTask.subject
                    )

                    DetailItem(
                        label = "Due Date",
                        value = currentTask.dueDate
                    )

                    DetailItem(
                        label = "Priority",
                        value = currentTask.priority
                    )

                    DetailItem(
                        label = "Status",
                        value = if (currentTask.isCompleted) {
                            "Completed"
                        } else {
                            "Pending"
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isEditing) {

            // Save button
            Button(
                onClick = {
                    when {
                        title.isBlank() -> {
                            errorMessage = "Please enter a task title."
                        }

                        subject.isBlank() -> {
                            errorMessage = "Please enter a subject."
                        }

                        dueDate.isBlank() -> {
                            errorMessage = "Please enter a due date."
                        }

                        !dueDate.matches(
                            Regex("\\d{4}-\\d{2}-\\d{2}")
                        ) -> {
                            errorMessage = "Use date format yyyy-MM-dd."
                        }

                        else -> {
                            scope.launch {
                                repository.updateTask(
                                    currentTask.copy(
                                        title = title.trim(),
                                        subject = subject.trim(),
                                        dueDate = dueDate.trim(),
                                        priority = priority
                                    )
                                )

                                isEditing = false
                                errorMessage = ""
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save Changes")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    title = currentTask.title
                    subject = currentTask.subject
                    dueDate = currentTask.dueDate
                    priority = currentTask.priority
                    errorMessage = ""
                    isEditing = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel")
            }

        } else {

            // Edit button
            Button(
                onClick = {
                    title = currentTask.title
                    subject = currentTask.subject
                    dueDate = currentTask.dueDate
                    priority = currentTask.priority
                    isEditing = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Edit Task")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Completion button
            Button(
                onClick = {
                    scope.launch {
                        repository.setTaskCompleted(
                            taskId = currentTask.id,
                            completed = !currentTask.isCompleted
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    if (currentTask.isCompleted) {
                        "Mark as Pending"
                    } else {
                        "Mark as Completed"
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Share button
            OutlinedButton(
                onClick = { onShare(currentTask) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Share Task")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delete button
            OutlinedButton(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete Task")
            }
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
