package com.funcap.tracker.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.funcap.tracker.backup.BackupManager
import com.funcap.tracker.data.AssessmentDao
import com.funcap.tracker.data.AssessmentEntity
import com.funcap.tracker.data.FuncapDatabase
import com.funcap.tracker.data.UserPreferencesRepository
import com.funcap.tracker.data.UserSettings
import com.funcap.tracker.model.FuncapCatalog
import com.funcap.tracker.model.FuncapDomain
import com.funcap.tracker.model.TestType
import com.funcap.tracker.model.ThemeMode
import com.funcap.tracker.reminder.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import java.util.Locale

data class ActiveQuestionnaireState(
    val testType: TestType = TestType.FUNCAP27,
    val selectedDomainIndex: Int = 0,
    val responses: Map<Int, Int> = emptyMap(),
    val notes: String = ""
) {
    val questionsForCurrentTest
        get() = FuncapCatalog.questionsFor(testType)

    val answeredCount: Int
        get() = questionsForCurrentTest.count { responses.containsKey(it.id) }

    val totalCount: Int
        get() = questionsForCurrentTest.size

    val isComplete: Boolean
        get() = answeredCount == totalCount

    val mappedFuncap27AnsweredCount: Int
        get() {
            val funcap27Ids = FuncapCatalog.questionsFor(TestType.FUNCAP27).map { it.id }.toSet()
            return funcap27Ids.count { responses.containsKey(it) }
        }
}

class FuncapViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: AssessmentDao = FuncapDatabase.getInstance(application).assessmentDao()
    private val prefsRepo = UserPreferencesRepository(application)

    val assessments: StateFlow<List<AssessmentEntity>> = dao.observeAllAssessments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettings> = prefsRepo.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    private val _questionnaireState = MutableStateFlow(ActiveQuestionnaireState())
    val questionnaireState: StateFlow<ActiveQuestionnaireState> = _questionnaireState.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun startNewTest(testType: TestType, preserveResponses: Boolean = false) {
        if (preserveResponses) {
            _questionnaireState.update { currentState ->
                val validQuestions = FuncapCatalog.questionsFor(testType).map { it.id }.toSet()
                val filteredResponses = currentState.responses.filterKeys { it in validQuestions }
                ActiveQuestionnaireState(
                    testType = testType,
                    selectedDomainIndex = currentState.selectedDomainIndex,
                    responses = filteredResponses,
                    notes = currentState.notes
                )
            }
        } else {
            _questionnaireState.value = ActiveQuestionnaireState(testType = testType)
        }
    }

    fun switchTestType(testType: TestType) {
        startNewTest(testType = testType, preserveResponses = true)
    }

    fun selectDomainTab(index: Int) {
        _questionnaireState.update {
            it.copy(selectedDomainIndex = index.coerceIn(0, FuncapDomain.entries.lastIndex))
        }
    }

    fun answerQuestion(questionId: Int, score: Int) {
        _questionnaireState.update { state ->
            val updated = state.responses.toMutableMap()
            updated[questionId] = score.coerceIn(0, 6)
            state.copy(responses = updated)
        }
    }

    fun updateNotes(notes: String) {
        _questionnaireState.update { it.copy(notes = notes) }
    }

    fun submitCurrentTest(onSaved: (Long) -> Unit) {
        val state = _questionnaireState.value
        val calc = FuncapCatalog.calculateScores(state.testType, state.responses)
        val now = System.currentTimeMillis()
        val entity = AssessmentEntity(
            timestampEpochMillis = now,
            testType = state.testType.name,
            totalScore = calc.totalScore,
            equivalentFuncap27Score = calc.equivalentFuncap27Score,
            domainScoresJson = AssessmentEntity.encodeDomainScores(calc.domainScores),
            itemResponsesJson = AssessmentEntity.encodeItemResponses(state.responses),
            notes = state.notes.trim()
        )
        viewModelScope.launch {
            val newId = dao.insertAssessment(entity)
            if (state.testType == TestType.FUNCAP55) {
                // Also save the mapped FUNCAP27 assessment so the user gets a score for both tests
                val funcap27Responses = FuncapCatalog.getMappedFuncap27Responses(state.responses)
                if (funcap27Responses.isNotEmpty()) {
                    val calc27 = FuncapCatalog.calculateScores(TestType.FUNCAP27, funcap27Responses)
                    val entity27 = AssessmentEntity(
                        timestampEpochMillis = now + 1L,
                        testType = TestType.FUNCAP27.name,
                        totalScore = calc27.totalScore,
                        equivalentFuncap27Score = calc27.totalScore,
                        domainScoresJson = AssessmentEntity.encodeDomainScores(calc27.domainScores),
                        itemResponsesJson = AssessmentEntity.encodeItemResponses(funcap27Responses),
                        notes = state.notes.trim()
                    )
                    dao.insertAssessment(entity27)
                    _statusMessage.value = "Saved FUNCAP55 (${String.format(Locale.US, "%.2f", calc.totalScore)}) and mapped FUNCAP27 (${String.format(Locale.US, "%.2f", calc27.totalScore)}) scores."
                } else {
                    _statusMessage.value = "${state.testType.displayName} score (${String.format(Locale.US, "%.2f", calc.totalScore)}) saved."
                }
            } else {
                _statusMessage.value = "${state.testType.displayName} score (${String.format(Locale.US, "%.2f", calc.totalScore)}) saved."
            }
            onSaved(newId)
        }
    }

    fun deleteAssessment(entity: AssessmentEntity) {
        viewModelScope.launch {
            dao.deleteAssessment(entity)
            _statusMessage.value = "Assessment entry deleted."
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            prefsRepo.setThemeMode(mode)
        }
    }

    fun setUseDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setUseDynamicColor(enabled)
        }
    }

    fun updateReminderSettings(
        enabled: Boolean,
        intervalDays: Int,
        preferredTest: TestType
    ) {
        viewModelScope.launch {
            prefsRepo.updateReminderConfig(enabled, intervalDays, preferredTest)
            ReminderScheduler.schedulePeriodicReminder(
                context = getApplication(),
                enabled = enabled,
                intervalDays = intervalDays,
                preferredTestType = preferredTest
            )
            _statusMessage.value = if (enabled) {
                "Reminder scheduled every $intervalDays day(s) for ${preferredTest.displayName}."
            } else {
                "Periodic reminders paused."
            }
        }
    }

    fun triggerTestReminderNotification() {
        val current = userSettings.value
        ReminderScheduler.showReminderNotification(
            context = getApplication(),
            preferredTestType = current.preferredReminderTest
        )
        _statusMessage.value = "Sent test notification for ${current.preferredReminderTest.displayName}."
    }

    fun exportBackupJson(uri: Uri) {
        viewModelScope.launch {
            val all = dao.getAllAssessmentsOnce()
            val result = BackupManager.exportJsonToUri(getApplication(), uri, all)
            result.onSuccess { count ->
                prefsRepo.recordBackupTimestamp()
                _statusMessage.value = "Backed up $count assessment(s) to Google Drive / storage."
            }.onFailure { err ->
                _statusMessage.value = "Backup failed: ${err.localizedMessage}"
            }
        }
    }

    fun importBackupJson(uri: Uri) {
        viewModelScope.launch {
            val result = BackupManager.importJsonFromUri(getApplication(), uri)
            result.onSuccess { imported ->
                val existingTimestamps = dao.getAllAssessmentsOnce()
                    .map { it.timestampEpochMillis to it.testType }
                    .toSet()
                val newEntries = imported.filter {
                    (it.timestampEpochMillis to it.testType) !in existingTimestamps
                }
                dao.insertAll(newEntries)
                _statusMessage.value = "Restored ${newEntries.size} new assessment(s) (${imported.size} total in file)."
            }.onFailure { err ->
                _statusMessage.value = "Restore failed: ${err.localizedMessage}"
            }
        }
    }

    fun exportCsv(uri: Uri) {
        viewModelScope.launch {
            val all = dao.getAllAssessmentsOnce()
            val result = BackupManager.exportCsvToUri(getApplication(), uri, all)
            result.onSuccess { count ->
                _statusMessage.value = "Exported $count assessment(s) to CSV."
            }.onFailure { err ->
                _statusMessage.value = "CSV export failed: ${err.localizedMessage}"
            }
        }
    }
}
