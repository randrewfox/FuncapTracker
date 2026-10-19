package com.funcap.tracker

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funcap.tracker.model.TestType
import com.funcap.tracker.reminder.ReminderScheduler
import com.funcap.tracker.ui.FuncapTrackerAppRoot
import com.funcap.tracker.ui.FuncapViewModel
import com.funcap.tracker.ui.theme.FuncapTrackerTheme

class FuncapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderScheduler.ensureNotificationChannel(this)
    }
}

class MainActivity : ComponentActivity() {

    private val viewModel: FuncapViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge rendering optimized for the Pixel 9 Pro display
        enableEdgeToEdge()

        // If launched from a periodic reminder notification, pre-select the requested test type
        intent?.getStringExtra(ReminderScheduler.EXTRA_TEST_TYPE)?.let { raw ->
            runCatching { TestType.valueOf(raw) }.getOrNull()?.let { type ->
                viewModel.startNewTest(type)
            }
        }

        setContent {
            val settings by viewModel.userSettings.collectAsStateWithLifecycle()
            FuncapTrackerTheme(
                themeMode = settings.themeMode,
                useDynamicColor = settings.useDynamicColor
            ) {
                FuncapTrackerAppRoot(viewModel = viewModel)
            }
        }
    }
}
