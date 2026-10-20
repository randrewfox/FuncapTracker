package com.funcap.tracker.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import java.util.Calendar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.funcap.tracker.data.AssessmentEntity
import com.funcap.tracker.data.UserSettings
import com.funcap.tracker.model.FuncapCatalog
import com.funcap.tracker.model.FuncapDomain
import com.funcap.tracker.model.FuncapQuestion
import com.funcap.tracker.model.TestType
import com.funcap.tracker.model.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class AppDestination {
    DASHBOARD,
    TAKE_TEST,
    RESULT_DETAIL,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuncapTrackerAppRoot(viewModel: FuncapViewModel) {
    val context = LocalContext.current
    val assessments by viewModel.assessments.collectAsStateWithLifecycle()
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()
    val questionnaireState by viewModel.questionnaireState.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    var currentDestination by rememberSaveable { mutableStateOf(AppDestination.DASHBOARD) }
    var selectedAssessmentId by rememberSaveable { mutableStateOf<Long?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    val appLaunchPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                appLaunchPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (currentDestination) {
                        AppDestination.DASHBOARD -> "FUNCAP Score Tracker"
                        AppDestination.TAKE_TEST -> "${questionnaireState.testType.displayName} Assessment"
                        AppDestination.RESULT_DETAIL -> "Assessment Breakdown"
                        AppDestination.SETTINGS -> "Settings & Cloud Backup"
                    }
                    Text(text = titleText, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    if (currentDestination == AppDestination.TAKE_TEST ||
                        currentDestination == AppDestination.RESULT_DETAIL
                    ) {
                        IconButton(onClick = { currentDestination = AppDestination.DASHBOARD }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    // Quick Light/Dark Mode toggle button in top bar for instant theme switching
                    val isDark = settings.themeMode == ThemeMode.DARK
                    IconButton(
                        onClick = {
                            viewModel.setThemeMode(
                                if (isDark) ThemeMode.LIGHT else ThemeMode.DARK
                            )
                        }
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Light or Dark Theme"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (currentDestination != AppDestination.TAKE_TEST) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.DASHBOARD ||
                            currentDestination == AppDestination.RESULT_DETAIL,
                        onClick = { currentDestination = AppDestination.DASHBOARD },
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text("History & Log") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            viewModel.startNewTest(TestType.FUNCAP27)
                            currentDestination = AppDestination.TAKE_TEST
                        },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        label = { Text("Take Test") }
                    )
                    NavigationBarItem(
                        selected = currentDestination == AppDestination.SETTINGS,
                        onClick = { currentDestination = AppDestination.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings") }
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
            when (currentDestination) {
                AppDestination.DASHBOARD -> DashboardScreen(
                    assessments = assessments,
                    settings = settings,
                    onStartTest = { type ->
                        viewModel.startNewTest(type)
                        currentDestination = AppDestination.TAKE_TEST
                    },
                    onOpenDetail = { id ->
                        selectedAssessmentId = id
                        currentDestination = AppDestination.RESULT_DETAIL
                    },
                    onDeleteAssessment = { viewModel.deleteAssessment(it) }
                )

                AppDestination.TAKE_TEST -> TakeTestScreen(
                    state = questionnaireState,
                    onSelectDomain = viewModel::selectDomainTab,
                    onAnswerQuestion = viewModel::answerQuestion,
                    onUpdateNotes = viewModel::updateNotes,
                    onSwitchTestType = viewModel::switchTestType,
                    onSubmit = {
                        viewModel.submitCurrentTest { savedId ->
                            selectedAssessmentId = savedId
                            currentDestination = AppDestination.RESULT_DETAIL
                        }
                    }
                )

                AppDestination.RESULT_DETAIL -> {
                    val entity = remember(assessments, selectedAssessmentId) {
                        assessments.find { it.id == selectedAssessmentId }
                            ?: assessments.firstOrNull()
                    }
                    if (entity != null) {
                        ResultDetailScreen(
                            assessment = entity,
                            onBackToDashboard = { currentDestination = AppDestination.DASHBOARD }
                        )
                    } else {
                        LaunchedEffect(Unit) {
                            currentDestination = AppDestination.DASHBOARD
                        }
                    }
                }

                AppDestination.SETTINGS -> SettingsScreen(
                    settings = settings,
                    onThemeModeChange = viewModel::setThemeMode,
                    onDynamicColorChange = viewModel::setUseDynamicColor,
                    onUpdateReminders = viewModel::updateReminderSettings,
                    onTestNotification = viewModel::triggerTestReminderNotification,
                    onExportJson = viewModel::exportBackupJson,
                    onImportJson = viewModel::importBackupJson,
                    onExportCsv = viewModel::exportCsv
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DashboardScreen(
    assessments: List<AssessmentEntity>,
    settings: UserSettings,
    onStartTest: (TestType) -> Unit,
    onOpenDetail: (Long) -> Unit,
    onDeleteAssessment: (AssessmentEntity) -> Unit
) {
    var filterType by rememberSaveable { mutableStateOf<TestType?>(null) }
    val filteredAssessments = remember(assessments, filterType) {
        if (filterType == null) assessments
        else assessments.filter { it.parsedTestType() == filterType }
    }

    val latest55 = remember(assessments) {
        assessments.firstOrNull { it.parsedTestType() == TestType.FUNCAP55 }
    }
    val latest27 = remember(assessments) {
        assessments.firstOrNull { it.parsedTestType() == TestType.FUNCAP27 }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Launch Cards for FUNCAP27 & FUNCAP55
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickTestCard(
                    modifier = Modifier.weight(1f),
                    testType = TestType.FUNCAP27,
                    latestScore = latest27?.totalScore,
                    latestTimestamp = latest27?.timestampEpochMillis,
                    onStart = { onStartTest(TestType.FUNCAP27) }
                )
                QuickTestCard(
                    modifier = Modifier.weight(1f),
                    testType = TestType.FUNCAP55,
                    latestScore = latest55?.totalScore,
                    latestTimestamp = latest55?.timestampEpochMillis,
                    onStart = { onStartTest(TestType.FUNCAP55) }
                )
            }
        }

        // Reminder Status Banner
        item {
            OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            val timeStr = remember(settings.reminderHour, settings.reminderMinute) {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, settings.reminderHour)
                                    set(Calendar.MINUTE, settings.reminderMinute)
                                }
                                SimpleDateFormat("h:mm a", Locale.US).format(cal.time)
                            }
                            Text(
                                text = if (settings.remindersEnabled) {
                                    "Reminder: Every ${settings.reminderIntervalDays} days at $timeStr (${settings.preferredReminderTest.displayName})"
                                } else {
                                    "Periodic reminders are currently paused"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            val lastTaken = assessments.firstOrNull()?.timestampEpochMillis
                            if (lastTaken != null && settings.remindersEnabled) {
                                val nextDue = lastTaken + settings.reminderIntervalDays * 86_400_000L
                                val dateFmt = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }
                                Text(
                                    text = "Next suggested check-in: ${dateFmt.format(Date(nextDue))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Longitudinal Trend Chart
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Score Trend Over Time (0–6 Scale)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Healthy control avg: 5.8 • ME/CFS study avg: 3.3",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterType == null,
                            onClick = { filterType = null },
                            label = { Text("All (${assessments.size})") }
                        )
                        FilterChip(
                            selected = filterType == TestType.FUNCAP27,
                            onClick = { filterType = TestType.FUNCAP27 },
                            label = {
                                Text("FUNCAP27 (${assessments.count { it.parsedTestType() == TestType.FUNCAP27 }})")
                            }
                        )
                        FilterChip(
                            selected = filterType == TestType.FUNCAP55,
                            onClick = { filterType = TestType.FUNCAP55 },
                            label = {
                                Text("FUNCAP55 (${assessments.count { it.parsedTestType() == TestType.FUNCAP55 }})")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (filteredAssessments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Take your first FUNCAP27 or FUNCAP55 test to see your longitudinal chart.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        val sortedFilteredAssessments = remember(filteredAssessments) {
                            filteredAssessments.sortedBy { it.timestampEpochMillis }
                        }
                        ScoreTrendChart(
                            assessments = sortedFilteredAssessments,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }
                }
            }
        }

        // Historical Log Header
        item {
            Text(
                text = "Assessment Log (${filteredAssessments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Historical Log Items
        items(filteredAssessments, key = { it.id }) { item ->
            AssessmentLogCard(
                assessment = item,
                onClick = { onOpenDetail(item.id) },
                onDelete = { onDeleteAssessment(item) }
            )
        }
    }
}

@Composable
private fun QuickTestCard(
    modifier: Modifier = Modifier,
    testType: TestType,
    latestScore: Double?,
    latestTimestamp: Long?,
    onStart: () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        onClick = onStart
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = testType.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${testType.questionCount} questions",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (latestScore != null && latestTimestamp != null) {
                val fmt = remember { SimpleDateFormat("MMM d", Locale.US) }
                Text(
                    text = String.format(Locale.US, "Latest: %.2f / 6", latestScore),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Taken ${fmt.format(Date(latestTimestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "No entries yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 12.dp)
            ) {
                Text("Start ${testType.displayName}")
            }
        }
    }
}

@Composable
private fun ScoreTrendChart(
    assessments: List<AssessmentEntity>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val dateFmt = remember { SimpleDateFormat("MMM d", Locale.US) }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            if (assessments.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height

            // Draw horizontal grid lines for scores 0, 2, 4, 6
            for (scoreLevel in listOf(0, 2, 4, 6)) {
                val y = height - (scoreLevel / 6f) * height
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val points = assessments.mapIndexed { idx, item ->
                val x = if (assessments.size == 1) {
                    width / 2f
                } else {
                    (idx.toFloat() / (assessments.size - 1).toFloat()) * width
                }
                val y = height - (item.totalScore.toFloat().coerceIn(0f, 6f) / 6f) * height
                Offset(x, y) to item
            }

            if (points.size > 1) {
                val path = Path().apply {
                    moveTo(points.first().first.x, points.first().first.y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].first.x, points[i].first.y)
                    }
                }
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            points.forEach { (offset, entity) ->
                val dotColor = if (entity.parsedTestType() == TestType.FUNCAP55) {
                    tertiaryColor
                } else {
                    primaryColor
                }
                drawCircle(
                    color = dotColor,
                    radius = 5.dp.toPx(),
                    center = offset
                )
            }
        }

        if (assessments.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFmt.format(Date(assessments.first().timestampEpochMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Scale: 0.0 (floor) to 6.0 (unproblematic)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFmt.format(Date(assessments.last().timestampEpochMillis)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AssessmentLogCard(
    assessment: AssessmentEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US) }
    val domains = remember(assessment.domainScoresJson) { assessment.parsedDomainScores() }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (assessment.parsedTestType() == TestType.FUNCAP55) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = assessment.testType,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateFormat.format(Date(assessment.timestampEpochMillis)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (assessment.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = assessment.notes,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format(Locale.US, "%.2f", assessment.totalScore),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete log entry",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Compact Domain A-H summary strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FuncapDomain.entries.forEach { domain ->
                    val score = domains[domain] ?: 0.0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = domain.code,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f", score),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TakeTestScreen(
    state: ActiveQuestionnaireState,
    onSelectDomain: (Int) -> Unit,
    onAnswerQuestion: (Int, Int) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onSwitchTestType: (TestType) -> Unit,
    onSubmit: () -> Unit
) {
    val domains = FuncapDomain.entries
    val currentDomain = domains[state.selectedDomainIndex]
    val domainQuestions = remember(state.testType, currentDomain) {
        state.questionsForCurrentTest.filter { it.domain == currentDomain }
    }
    var showInstructionsDialog by remember { mutableStateOf(false) }

    if (showInstructionsDialog) {
        AlertDialog(
            onDismissRequest = { showInstructionsDialog = false },
            title = { Text("FUNCAP Scoring Instructions") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(FuncapCatalog.INSTRUCTIONS_HEADING, fontWeight = FontWeight.SemiBold)
                    Text(FuncapCatalog.INSTRUCTIONS_BODY)
                }
            },
            confirmButton = {
                TextButton(onClick = { showInstructionsDialog = false }) {
                    Text("Got It")
                }
            }
        )
    }

    val listState = rememberLazyListState()

    LaunchedEffect(state.selectedDomainIndex, state.testType) {
        listState.scrollToItem(0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Test Variant Switcher + Progress Header
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.testType == TestType.FUNCAP27,
                            onClick = { onSwitchTestType(TestType.FUNCAP27) },
                            label = { Text("FUNCAP27 (27 items)") }
                        )
                        FilterChip(
                            selected = state.testType == TestType.FUNCAP55,
                            onClick = { onSwitchTestType(TestType.FUNCAP55) },
                            label = { Text("FUNCAP55 (55 items)") }
                        )
                    }
                    IconButton(onClick = { showInstructionsDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Instructions")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val progressText = if (state.testType == TestType.FUNCAP55) {
                        "Answered ${state.answeredCount} of ${state.totalCount} (FUNCAP27 mapped: ${state.mappedFuncap27AnsweredCount}/27)"
                    } else {
                        "Answered ${state.answeredCount} of ${state.totalCount}"
                    }
                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.labelMedium
                    )
                    val pct = if (state.totalCount > 0) {
                        (state.answeredCount * 100) / state.totalCount
                    } else 0
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = {
                        if (state.totalCount > 0) {
                            state.answeredCount.toFloat() / state.totalCount.toFloat()
                        } else 0f
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Scrollable Domain Tabs (A through H)
        ScrollableTabRow(
            selectedTabIndex = state.selectedDomainIndex,
            edgePadding = 12.dp
        ) {
            domains.forEachIndexed { idx, domain ->
                val done = remember(state.responses, state.questionsForCurrentTest, domain) {
                    val dQuestions = state.questionsForCurrentTest.filter { it.domain == domain }
                    dQuestions.isNotEmpty() && dQuestions.all { state.responses.containsKey(it.id) }
                }
                Tab(
                    selected = state.selectedDomainIndex == idx,
                    onClick = { onSelectDomain(idx) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${domain.code}: ${domain.shortTitle}")
                            if (done) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Completed section",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            }
        }

        // Questions List for Current Domain
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Section ${currentDomain.code}: ${currentDomain.title}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Base your answers on an average day during the last month.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            items(domainQuestions, key = { it.id }) { question ->
                QuestionItemCard(
                    question = question,
                    testType = state.testType,
                    selectedScore = state.responses[question.id],
                    onSelectScore = { score -> onAnswerQuestion(question.id, score) }
                )
            }

            if (state.selectedDomainIndex == domains.lastIndex) {
                item {
                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = onUpdateNotes,
                        label = { Text("Optional notes (e.g., medication change, PEM recovery)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (state.selectedDomainIndex > 0) {
                        OutlinedButton(
                            onClick = { onSelectDomain(state.selectedDomainIndex - 1) }
                        ) {
                            Text("Previous Section")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (state.selectedDomainIndex < domains.lastIndex) {
                        Button(
                            onClick = { onSelectDomain(state.selectedDomainIndex + 1) }
                        ) {
                            Text("Next Section (${domains[state.selectedDomainIndex + 1].code})")
                        }
                    } else {
                        Button(
                            onClick = onSubmit,
                            enabled = state.answeredCount > 0
                        ) {
                            Text(
                                if (state.isComplete) "Calculate & Save Score"
                                else "Save Partial (${state.answeredCount}/${state.totalCount})"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionItemCard(
    question: FuncapQuestion,
    testType: TestType,
    selectedScore: Int?,
    onSelectScore: (Int) -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${question.displayNumber(testType)}. ${question.text}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (testType == TestType.FUNCAP55 && question.funcap27Number != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "FUNCAP27 #${question.funcap27Number}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            // 0 to 6 Rating Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FuncapCatalog.SCORE_OPTIONS.forEach { option ->
                    val isSelected = selectedScore == option.value
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                            .clickable { onSelectScore(option.value) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.value.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Show full official meaning of the selected score (or tap hint)
            Spacer(modifier = Modifier.height(8.dp))
            val selectedOption = FuncapCatalog.SCORE_OPTIONS.find { it.value == selectedScore }
            Text(
                text = selectedOption?.let { "${it.value}: ${it.fullDescription}" }
                    ?: "Tap 0 (Cannot do this) to 6 (Unproblematic – does not affect other activities)",
                style = MaterialTheme.typography.bodySmall,
                color = if (selectedOption != null) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selectedOption != null) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun ResultDetailScreen(
    assessment: AssessmentEntity,
    onBackToDashboard: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.US) }
    val domains = remember(assessment.domainScoresJson) { assessment.parsedDomainScores() }
    val responses = remember(assessment.itemResponsesJson) { assessment.parsedItemResponses() }
    val testType = remember(assessment.testType) { assessment.parsedTestType() }
    val questions = remember(testType) { FuncapCatalog.questionsFor(testType) }
    val costlyActivities = remember(questions, responses) {
        questions.filter { (responses[it.id] ?: 6) <= 2 }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${testType.displayName} Total Score",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f / 6.00", assessment.totalScore),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (testType == TestType.FUNCAP55) {
                        Text(
                            text = String.format(
                                Locale.US,
                                "Equivalent FUNCAP27 Subset Score: %.2f / 6.00",
                                assessment.equivalentFuncap27Score
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateFormat.format(Date(assessment.timestampEpochMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (assessment.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Notes: ${assessment.notes}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // Subscale Profile A-H vs Sommerfelt et al. (2024) Benchmarks
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Subscale Profile (Domains A–H)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    FuncapDomain.entries.forEach { domain ->
                        val score = domains[domain] ?: 0.0
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${domain.code}. ${domain.title}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = String.format(Locale.US, "%.2f / 6", score),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (score.toFloat() / 6f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = "Validation study avg — ME/CFS: ${domain.mecfsStudyMean} | Healthy control: ${domain.healthyControlMean}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Most Costly Activities (Rated 0 to 2)
        if (costlyActivities.isNotEmpty()) {
            item {
                OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Most Costly Activities (Scored 0–2)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Activities you cannot do or that reduce your capacity for one or more days afterwards:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        costlyActivities.forEach { q ->
                            val s = responses[q.id] ?: 0
                            Text(
                                text = "• [${q.domain.code}${q.displayNumber(testType)}] ${q.text} — Score $s",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        item {
            Button(
                onClick = onBackToDashboard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(
    settings: UserSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onUpdateReminders: (Boolean, Int, TestType, Int, Int) -> Unit,
    onTestNotification: () -> Unit,
    onExportJson: (android.net.Uri) -> Unit,
    onImportJson: (android.net.Uri) -> Unit,
    onExportCsv: (android.net.Uri) -> Unit
) {
    val context = LocalContext.current
    var customIntervalText by remember(settings.reminderIntervalDays) {
        mutableStateOf(settings.reminderIntervalDays.toString())
    }

    val packageInfo = remember(context) {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0L)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        }.getOrNull()
    }
    val versionName = packageInfo?.versionName ?: "1.1.2"
    val versionCode = packageInfo?.longVersionCode ?: 5L

    var showPermissionRequiredDialog by remember { mutableStateOf(false) }

    val openAppSettings = remember(context) {
        {
            runCatching {
                val intent = android.content.Intent(
                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                ).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        }
    }

    if (showPermissionRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRequiredDialog = false },
            title = { Text("Notification Permission Required") },
            text = {
                Text(
                    "Notifications are currently disabled in Android settings for FUNCAP Tracker. " +
                    "To receive test notifications and scheduled reminders, please enable Notifications in App Settings."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionRequiredDialog = false
                        openAppSettings()
                    }
                ) {
                    Text("Open App Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRequiredDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(onExportJson) }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(onImportJson) }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let(onExportCsv) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Theme & Appearance Section (Light Mode / Dark Mode Toggle)
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Appearance & Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Direct Light / Dark Mode Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dark Mode Toggle",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Switch between Light Mode and Dark Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.themeMode == ThemeMode.DARK,
                        onCheckedChange = { isDark ->
                            onThemeModeChange(if (isDark) ThemeMode.DARK else ThemeMode.LIGHT)
                        }
                    )
                }

                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            label = { Text(mode.label) }
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pixel Material You Dynamic Colors",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Adapt app accents to your wallpaper",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.useDynamicColor,
                        onCheckedChange = onDynamicColorChange
                    )
                }
            }
        }

        // 2. Customizable Assessment Reminders Section
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Customizable Assessment Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Scheduled Reminders",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Receive periodic notifications to take FUNCAP55 or FUNCAP27",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.remindersEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (!granted) {
                                    showPermissionRequiredDialog = true
                                }
                            }
                            onUpdateReminders(
                                enabled,
                                settings.reminderIntervalDays,
                                settings.preferredReminderTest,
                                settings.reminderHour,
                                settings.reminderMinute
                            )
                        }
                    )
                }

                AnimatedVisibility(visible = settings.remindersEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Reminder Frequency Presets",
                            style = MaterialTheme.typography.labelLarge
                        )
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                7 to "Weekly (7d)",
                                14 to "Bi-weekly (14d)",
                                30 to "Monthly (30d)",
                                60 to "Every 2 Months (60d)",
                                90 to "Quarterly (90d)"
                            ).forEach { (days, label) ->
                                FilterChip(
                                    selected = settings.reminderIntervalDays == days,
                                    onClick = {
                                        customIntervalText = days.toString()
                                        onUpdateReminders(
                                            true,
                                            days,
                                            settings.preferredReminderTest,
                                            settings.reminderHour,
                                            settings.reminderMinute
                                        )
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customIntervalText,
                                onValueChange = { value ->
                                    customIntervalText = value.filter { it.isDigit() }
                                },
                                label = { Text("Custom Interval (Days)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Button(
                                onClick = {
                                    val days = customIntervalText.toIntOrNull()?.coerceIn(1, 365) ?: 30
                                    onUpdateReminders(
                                        true,
                                        days,
                                        settings.preferredReminderTest,
                                        settings.reminderHour,
                                        settings.reminderMinute
                                    )
                                }
                            ) {
                                Text("Apply")
                            }
                        }

                        HorizontalDivider()

                        val reminderTimeCal = remember(settings.reminderHour, settings.reminderMinute) {
                            Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, settings.reminderHour)
                                set(Calendar.MINUTE, settings.reminderMinute)
                            }
                        }
                        val reminderTimeFmt = remember { SimpleDateFormat("h:mm a", Locale.US) }
                        val formattedReminderTime = reminderTimeFmt.format(reminderTimeCal.time)

                        val timePickerDialog = remember(context, settings.reminderHour, settings.reminderMinute) {
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    onUpdateReminders(
                                        settings.remindersEnabled,
                                        settings.reminderIntervalDays,
                                        settings.preferredReminderTest,
                                        hourOfDay,
                                        minute
                                    )
                                },
                                settings.reminderHour,
                                settings.reminderMinute,
                                false
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Reminder Time of Day",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Notification scheduled for $formattedReminderTime",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(onClick = { timePickerDialog.show() }) {
                                Text(formattedReminderTime)
                            }
                        }

                        HorizontalDivider()

                        Text(
                            text = "Default Questionnaire for Reminder",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TestType.entries.forEach { type ->
                                FilterChip(
                                    selected = settings.preferredReminderTest == type,
                                    onClick = {
                                        onUpdateReminders(
                                            settings.remindersEnabled,
                                            settings.reminderIntervalDays,
                                            type,
                                            settings.reminderHour,
                                            settings.reminderMinute
                                        )
                                    },
                                    label = { Text(type.displayName) }
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val isGranted = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (isGranted) {
                                        onTestNotification()
                                    } else {
                                        showPermissionRequiredDialog = true
                                    }
                                } else {
                                    onTestNotification()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Test Reminder Notification Now")
                        }
                    }
                }
            }
        }

        // 3. Zero-Setup Cloud Backup & Google Drive Integration
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Cloud Backup & Google Drive",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "• Automatic Cloud Sync: Android Auto Backup is enabled for your Google Account (`funcap_tracker.db` & preferences).\n" +
                           "• Direct Google Drive Backup: Use the buttons below to save or restore your full JSON backup or CSV log directly to your Google Drive via the Android system file picker—no API keys or OAuth setup needed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (settings.lastBackupEpochMillis > 0L) {
                    val fmt = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US)
                    Text(
                        text = "Last manual Drive backup: ${fmt.format(Date(settings.lastBackupEpochMillis))}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = {
                        val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                        exportJsonLauncher.launch("funcap_backup_$dateStamp.json")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back Up to Google Drive (JSON)")
                }

                OutlinedButton(
                    onClick = {
                        importJsonLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Restore Backup from Google Drive (JSON)")
                }

                OutlinedButton(
                    onClick = {
                        val dateStamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                        exportCsvLauncher.launch("funcap_scores_$dateStamp.csv")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.TableView, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Clinical Spreadsheet (CSV to Drive)")
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "FUNCAP Tracker v$versionName ($versionCode)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
