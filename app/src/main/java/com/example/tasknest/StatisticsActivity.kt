package com.example.tasknest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.tasknest.data.TaskDatabase
import com.example.tasknest.settings.UserPreferences
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val prefs = UserPreferences(applicationContext)
        val targetNightMode = when (prefs.themeMode) {
            "Dark" -> AppCompatDelegate.MODE_NIGHT_YES
            "Light" -> AppCompatDelegate.MODE_NIGHT_NO
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        delegate.localNightMode = targetNightMode

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val rootScrollView = findViewById<View>(R.id.root_scroll_view)
        ViewCompat.setOnApplyWindowInsetsListener(rootScrollView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        val btnClose = findViewById<Button>(R.id.btn_close)

        btnBack.setOnClickListener { finish() }
        btnClose.setOnClickListener { finish() }

        loadStatistics()
    }

    private fun loadStatistics() {
        val database = TaskDatabase.getDatabase(applicationContext)

        val tvOverviewHeadline = findViewById<TextView>(R.id.tv_overview_headline)
        val tvProgressSubtitle = findViewById<TextView>(R.id.tv_progress_subtitle)
        val progressBar = findViewById<LinearProgressIndicator>(R.id.progress_bar)
        val tvTotalCount = findViewById<TextView>(R.id.tv_total_count)
        val tvPendingCount = findViewById<TextView>(R.id.tv_pending_count)
        val tvCompletedCount = findViewById<TextView>(R.id.tv_completed_count)
        val tvHighPriorityCount = findViewById<TextView>(R.id.tv_high_priority_count)
        val tvNormalPriorityCount = findViewById<TextView>(R.id.tv_normal_priority_count)
        val tvInsightMessage = findViewById<TextView>(R.id.tv_insight_message)
        val btnShare = findViewById<Button>(R.id.btn_share_stats)

        lifecycleScope.launch {
            database.taskDao().getAllTasks().collect { tasks ->
                val total = tasks.size
                val completed = tasks.count { it.isCompleted }
                val pending = total - completed
                val highPriorityPending = tasks.count { it.priority == "High" && !it.isCompleted }
                val normalPriorityPending = pending - highPriorityPending
                val rate = if (total > 0) (completed * 100) / total else 0

                tvOverviewHeadline.text = if (pending == 0 && total > 0) {
                    "You're all caught up!"
                } else if (total == 0) {
                    "Ready to start?"
                } else {
                    "Let's get things done."
                }

                tvProgressSubtitle.text = "$completed of $total tasks completed · $rate%"
                progressBar.setProgressCompat(rate, true)

                tvTotalCount.text = total.toString()
                tvPendingCount.text = pending.toString()
                tvCompletedCount.text = completed.toString()
                tvHighPriorityCount.text = "$highPriorityPending pending"
                tvNormalPriorityCount.text = "$normalPriorityPending pending"

                tvInsightMessage.text = when {
                    total == 0 ->
                        "You don't have any tasks logged yet. Add your courses and assignments on the dashboard to start tracking!"
                    rate == 100 ->
                        "Outstanding! Every single task is completed. Take a well-deserved break!"
                    highPriorityPending > 0 ->
                        "You have $highPriorityPending urgent high-priority task(s). Tackle those first for maximum impact! ⚡"
                    rate >= 50 ->
                        "You are over halfway done with your workload! Keep this momentum going."
                    else ->
                        "Small daily progress adds up to big results. Pick one task and start now!"
                }

                btnShare.setOnClickListener {
                    val shareText = """
                        📊 TaskNest Study Statistics
                        -----------------------------
                        • Total Tasks: $total
                        • Completed: $completed ($rate%)
                        • Pending: $pending
                        • High Priority Pending: $highPriorityPending
                        
                        Organized with TaskNest
                    """.trimIndent()

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "My TaskNest Statistics")
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    startActivity(Intent.createChooser(intent, "Share statistics via"))
                }
            }
        }
    }
}
