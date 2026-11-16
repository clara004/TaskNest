
package com.example.tasknest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tasknest.data.TaskEntity
import com.example.tasknest.data.TaskRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleScreen(
    repository: TaskRepository,
    onTaskClick: (TaskEntity) -> Unit = {}
) {
    // Observe tasks from Room database
    val tasks by repository.allTasks.collectAsState(
        initial = emptyList()
    )

    val scope = rememberCoroutineScope()

    // Date formatting
    val dateFormat = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US)
    }

    val today = remember {
        dateFormat.format(Date())
    }

    // Calculate the next 6 days (7 days including today)
    val weekEndDate = remember {
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 6)
        }.let { dateFormat.format(it.time) }
    }

    // Selected filter
    var selectedFilter by remember {
        mutableStateOf("All")
    }

    // Filter tasks
    val filteredTasks = when (selectedFilter) {

        "Today" -> tasks.filter {
            it.dueDate == today
        }

        "Upcoming" -> tasks.filter {
            it.dueDate > today
        }

        "This week" -> tasks.filter {
            it.dueDate >= today &&
                    it.dueDate <= weekEndDate
        }

        "Overdue" -> tasks.filter {
            it.dueDate < today && !it.isCompleted
        }

        else -> tasks
    }

    // Sort and group tasks by date
    val groupedTasks = filteredTasks
        .sortedWith(
            compareBy<TaskEntity> { it.dueDate }
                .thenBy {
                    when (it.priority) {
                        "High" -> 0
                        "Medium" -> 1
                        else -> 2
                    }
                }
                .thenBy { it.isCompleted }
        )
        .groupBy { it.dueDate }
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = 20.dp,
            vertical = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // HEADER
        item {
            Column {

                Text(
                    text = "My Schedule",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Organize your tasks and stay on track.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Summary card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor =
                            MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement =
                            Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {
                            Text(
                                text = "TOTAL TASKS",
                                style = MaterialTheme.typography.labelMedium
                            )

                            Text(
                                text = "${tasks.size}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "PENDING",
                                style = MaterialTheme.typography.labelMedium
                            )

                            Text(
                                text = "${tasks.count { !it.isCompleted }}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // FILTERS
        item {
            Column {
                Text(
                    text = "Filter tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "All",
                        "Today",
                        "Upcoming",
                        "This week",
                        "Overdue"
                    ).forEach { filter ->

                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = {
                                selectedFilter = filter
                            },
                            label = {
                                Text(filter)
                            }
                        )
                    }
                }
            }
        }

        // TASK COUNT
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${filteredTasks.size} tasks",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // EMPTY STATE
        if (groupedTasks.isEmpty()) {

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "📅",
                            fontSize = 42.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (tasks.isEmpty()) {
                                "No scheduled tasks"
                            } else {
                                "No ${selectedFilter.lowercase()} tasks"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Your tasks will appear here.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

        } else {

            // GROUP TASKS BY DATE
            groupedTasks.forEach { (date, dateTasks) ->

                // DATE HEADER
                item(key = "header_$date") {

                    Column {

                        Text(
                            text = when (date) {
                                today -> "Today"
                                else -> formatScheduleDate(date)
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${dateTasks.size} ${
                                if (dateTasks.size == 1) "task" else "tasks"
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // TASK CARDS
                items(
                    items = dateTasks,
                    key = { it.id }
                ) { task ->

                    ScheduleTaskCard(
                        task = task,

                        // Update completion directly in Room
                        onCompletionChanged = { completed ->

                            scope.launch {
                                repository.setTaskCompleted(
                                    taskId = task.id,
                                    completed = completed
                                )
                            }
                        },

                        // Ready for Task Details navigation
                        onClick = {
                            onTaskClick(task)
                        }
                    )
                }
            }
        }

        // FOOTER
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


// TASK CARD
@Composable
private fun ScheduleTaskCard(
    task: TaskEntity,
    onCompletionChanged: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Completion checkbox
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = onCompletionChanged
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Priority indicator
            Box(
                modifier = Modifier
                    .size(5.dp, 55.dp)
                    .clip(CircleShape)
                    .background(
                        when (task.priority) {
                            "High" -> MaterialTheme.colorScheme.error
                            "Medium" -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.subject,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    PriorityBadge(task.priority)

                    Text(
                        text = if (task.isCompleted) {
                            "Completed"
                        } else {
                            "Pending"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "›",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


// PRIORITY BADGE
@Composable
private fun PriorityBadge(priority: String) {

    val backgroundColor = when (priority) {
        "High" -> MaterialTheme.colorScheme.errorContainer
        "Medium" -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    val textColor = when (priority) {
        "High" -> MaterialTheme.colorScheme.onErrorContainer
        "Medium" -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = priority,
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}


// FORMAT DATE
private fun formatScheduleDate(date: String): String {
    return try {
        val input = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.US
        )

        val output = SimpleDateFormat(
            "EEEE, MMMM d",
            Locale.US
        )

        output.format(input.parse(date) ?: return date)

    } catch (e: Exception) {
        date
    }
}
