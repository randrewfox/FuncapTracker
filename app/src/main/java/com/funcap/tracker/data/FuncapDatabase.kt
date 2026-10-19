package com.funcap.tracker.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.funcap.tracker.model.FuncapDomain
import com.funcap.tracker.model.TestType
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

/**
 * Room Entity storing a completed FUNCAP55 or FUNCAP27 assessment.
 */
@Entity(tableName = "funcap_assessments")
data class AssessmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestampEpochMillis: Long,
    val testType: String, // "FUNCAP55" or "FUNCAP27"
    val totalScore: Double,
    val equivalentFuncap27Score: Double,
    val domainScoresJson: String,
    val itemResponsesJson: String,
    val notes: String = "",
) {
    fun parsedTestType(): TestType =
        runCatching { TestType.valueOf(testType) }.getOrDefault(TestType.FUNCAP27)

    fun parsedDomainScores(): Map<FuncapDomain, Double> {
        val result = mutableMapOf<FuncapDomain, Double>()
        runCatching {
            val json = JSONObject(domainScoresJson)
            for (domain in FuncapDomain.entries) {
                if (json.has(domain.code)) {
                    result[domain] = json.getDouble(domain.code)
                }
            }
        }
        return result
    }

    fun parsedItemResponses(): Map<Int, Int> {
        val result = mutableMapOf<Int, Int>()
        runCatching {
            val json = JSONObject(itemResponsesJson)
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                key.toIntOrNull()?.let { qId ->
                    result[qId] = json.getInt(key)
                }
            }
        }
        return result
    }

    companion object {
        fun encodeDomainScores(scores: Map<FuncapDomain, Double>): String {
            val json = JSONObject()
            scores.forEach { (domain, score) ->
                json.put(domain.code, score)
            }
            return json.toString()
        }

        fun encodeItemResponses(responses: Map<Int, Int>): String {
            val json = JSONObject()
            responses.forEach { (questionId, score) ->
                json.put(questionId.toString(), score)
            }
            return json.toString()
        }
    }
}

@Dao
interface AssessmentDao {

    @Query("SELECT * FROM funcap_assessments ORDER BY timestampEpochMillis DESC")
    fun observeAllAssessments(): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM funcap_assessments WHERE testType = :type ORDER BY timestampEpochMillis DESC")
    fun observeAssessmentsByType(type: String): Flow<List<AssessmentEntity>>

    @Query("SELECT * FROM funcap_assessments ORDER BY timestampEpochMillis DESC")
    suspend fun getAllAssessmentsOnce(): List<AssessmentEntity>

    @Query("SELECT * FROM funcap_assessments WHERE id = :id LIMIT 1")
    suspend fun getAssessmentById(id: Long): AssessmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: AssessmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(assessments: List<AssessmentEntity>)

    @Delete
    suspend fun deleteAssessment(assessment: AssessmentEntity)

    @Query("DELETE FROM funcap_assessments")
    suspend fun deleteAllAssessments()
}

@Database(
    entities = [AssessmentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class FuncapDatabase : RoomDatabase() {

    abstract fun assessmentDao(): AssessmentDao

    companion object {
        const val DATABASE_NAME = "funcap_tracker.db"

        @Volatile
        private var INSTANCE: FuncapDatabase? = null

        fun getInstance(context: Context): FuncapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FuncapDatabase::class.java,
                    DATABASE_NAME,
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
