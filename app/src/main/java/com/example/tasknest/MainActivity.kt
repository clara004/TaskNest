
package com.example.tasknest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasknest.data.TaskDatabase
import com.example.tasknest.data.TaskEntity
import com.example.tasknest.data.TaskRepository
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = TaskDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao())

        setContent {
            MaterialTheme {
                var selectedTab by remember { mutableStateOf("Dashboard") }
                var selectedTask by remember { mutableStateOf<TaskEntity?>(null) }

                Scaffold(
                    bottomBar = {
                        if (selectedTask == null) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == "Dashboard",
                                    onClick = { selectedTab = "Dashboard" },
                                    icon = {
                                        Text("🏠")
                                    },
                                    label = {
                                        Text("Dashboard")
                                    }
                                )

                                NavigationBarItem(
                                    selected = selectedTab == "Schedule",
                                    onClick = { selectedTab = "Schedule" },
                                    icon = {
                                        Text("📅")
                                    },
                                    label = {
                                        Text("Schedule")
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (selectedTask != null) {

                            TaskDetailsScreen(
                                task = selectedTask!!,
                                repository = repository,
                                onBack = {
                                    selectedTask = null
                                },
                                onDeleted = {
                                    selectedTask = null
                                    selectedTab = "Schedule"
                                }
                            )

                        } else {

                            when (selectedTab) {
                                "Dashboard" -> {
                                    TaskNestDashboard(repository)
                                }

                                "Schedule" -> {
                                    ScheduleScreen(
                                        repository = repository,
                                        onTaskClick = { task ->
                                            selectedTask = task
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskNestDashboard(repository: TaskRepository) {
    val tasks by repository.allTasks.collectAsState(initial = emptyList())
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredTasks = when (selectedFilter) {
        "Pending" -> tasks.filter { !it.isCompleted }
        "Completed" -> tasks.filter { it.isCompleted }
        else -> tasks
    }
    val scope = rememberCoroutineScope()

    var showForm by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var formError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    val completedCount = tasks.count { it.isCompleted }
    val pendingCount = tasks.size - completedCount
    val progress = if (tasks.isEmpty()) {
        0
    } else {
        completedCount * 100 / tasks.size
    }
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis =
                            datePickerState.selectedDateMillis

                        if (selectedMillis != null) {
                            val formatter =
                                SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.US
                                ).apply {
                                    timeZone = TimeZone.getTimeZone("UTC")
                                }

                            dueDate = formatter.format(
                                Date(selectedMillis)
                            )
                            formError = null
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LazyColumn(
        state = listState,
        userScrollEnabled = true,
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 20.dp,
            vertical = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            // Dashboard header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "TaskNest",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your study life, organized.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "TN", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            // Welcome panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "YOUR DAILY OVERVIEW",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (pendingCount == 0) {
                            "You're all caught up!"
                        } else {
                            "Let's get things done."
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$completedCount of ${tasks.size} tasks completed · $progress%",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress / 100f)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onPrimary)
                        )
                    }
                }
            }
        }

        item {
            // Summary cards
            Text(
                text = "Your tasks",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard(
                    label = "Total",
                    count = tasks.size,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "Pending",
                    count = pendingCount,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "Done",
                    count = completedCount,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            // Add task button
            Button(
                onClick = {
                    showForm = !showForm
                    formError = null
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(if (showForm) "Cancel adding task" else "+ Add Task")
            }
        }

        if (showForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Create a task",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                formError = null
                            },
                            label = { Text("Task title") },
                            placeholder = { Text("e.g. Finish assignment") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = subject,
                            onValueChange = {
                                subject = it
                                formError = null
                            },
                            label = { Text("Subject") },
                            placeholder = { Text("e.g. Mobile Computing") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDatePicker = true }
                        ) {
                            OutlinedTextField(
                                value = dueDate,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("Due date") },
                                placeholder = { Text("Choose a date") },
                                trailingIcon = {
                                    Text("📅", modifier = Modifier.padding(end = 12.dp))
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Text(
                            text = "Priority",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Low", "Medium", "High").forEach { option ->
                                FilterChip(
                                    selected = priority == option,
                                    onClick = { priority = option },
                                    label = { Text(option) }
                                )
                            }
                        }

                        if (formError != null) {
                            Text(
                                text = formError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Button(
                            onClick = {
                                when {
                                    title.isBlank() ->
                                        formError = "Please enter a task title."

                                    subject.isBlank() ->
                                        formError = "Please enter a subject."

                                    !dueDate.matches(
                                        Regex("""\d{4}-\d{2}-\d{2}""")
                                    ) ->
                                        formError =
                                            "Enter the due date as YYYY-MM-DD."

                                    else -> {
                                        scope.launch {
                                            repository.insertTask(
                                                TaskEntity(
                                                    title = title.trim(),
                                                    subject = subject.trim(),
                                                    dueDate = dueDate.trim(),
                                                    priority = priority
                                                )
                                            )

                                            title = ""
                                            subject = ""
                                            dueDate = ""
                                            priority = "Medium"
                                            formError = null
                                            showForm = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Task")
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "My task list",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${tasks.size} tasks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Pending", "Completed").forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) }
                    )
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📚", fontSize = 38.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (tasks.isEmpty()) {
                                "No tasks yet"
                            } else {
                                "No ${selectedFilter.lowercase()} tasks"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (tasks.isEmpty()) {
                                "Add your first task to get started."
                            } else {
                                "Try another filter to see your tasks."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                items = filteredTasks,
                key = { task -> task.id }
            ) { task ->
                TaskCard(
                    task = task,
                    onCompletionChanged = { completed ->
                        scope.launch {
                            repository.setTaskCompleted(
                                taskId = task.id,
                                completed = completed
                            )
                        }
                    },
                    onDelete = {
                        scope.launch {
                            repository.deleteTask(task)
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "TaskNest · Stay focused, one task at a time.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onCompletionChanged: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onCompletionChanged
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (task.isCompleted) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = task.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                PriorityLabel(priority = task.priority)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "DUE DATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = task.dueDate,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = if (task.isCompleted) "Completed" else "Pending",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                TextButton(onClick = onDelete) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityLabel(priority: String) {
    val containerColor = when (priority) {
        "High" -> MaterialTheme.colorScheme.errorContainer
        "Low" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = when (priority) {
        "High" -> MaterialTheme.colorScheme.onErrorContainer
        "Low" -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = priority,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
