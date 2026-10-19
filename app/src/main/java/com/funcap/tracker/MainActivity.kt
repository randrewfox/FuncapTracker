package com.funcap.tracker

import android.app.Application
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
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
        
        // Edge-to-edge configuration compliant with Android 15 (API 35+)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode = if (Build.VERSION.SDK_INT >= 35) {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else {
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

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
