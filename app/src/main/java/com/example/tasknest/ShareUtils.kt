package com.example.tasknest

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.example.tasknest.data.TaskEntity

fun buildShareText(task: TaskEntity): String {
    val status = if (task.isCompleted) "Completed" else "Pending"
    return buildString {
        appendLine("📚 ${task.title}")
        appendLine("Subject: ${task.subject}")
        appendLine("Due: ${task.dueDate}")
        appendLine("Priority: ${task.priority}")
        appendLine("Status: $status")
        append("Shared from TaskNest")
    }
}

fun shareTask(context: Context, task: TaskEntity): Boolean {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "TaskNest: ${task.title}")
        putExtra(Intent.EXTRA_TEXT, buildShareText(task))
    }

    return try {
        context.startActivity(Intent.createChooser(sendIntent, "Share task via"))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}