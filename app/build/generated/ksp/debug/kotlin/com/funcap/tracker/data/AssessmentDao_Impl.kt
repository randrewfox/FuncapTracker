package com.funcap.tracker.`data`

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Double
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AssessmentDao_Impl(
  __db: RoomDatabase,
) : AssessmentDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfAssessmentEntity: EntityInsertAdapter<AssessmentEntity>

  private val __deleteAdapterOfAssessmentEntity: EntityDeleteOrUpdateAdapter<AssessmentEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfAssessmentEntity = object : EntityInsertAdapter<AssessmentEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `funcap_assessments` (`id`,`timestampEpochMillis`,`testType`,`totalScore`,`equivalentFuncap27Score`,`domainScoresJson`,`itemResponsesJson`,`notes`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: AssessmentEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.timestampEpochMillis)
        statement.bindText(3, entity.testType)
        statement.bindDouble(4, entity.totalScore)
        statement.bindDouble(5, entity.equivalentFuncap27Score)
        statement.bindText(6, entity.domainScoresJson)
        statement.bindText(7, entity.itemResponsesJson)
        statement.bindText(8, entity.notes)
      }
    }
    this.__deleteAdapterOfAssessmentEntity = object : EntityDeleteOrUpdateAdapter<AssessmentEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `funcap_assessments` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: AssessmentEntity) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insertAssessment(assessment: AssessmentEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfAssessmentEntity.insertAndReturnId(_connection, assessment)
    _result
  }

  public override suspend fun insertAll(assessments: List<AssessmentEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfAssessmentEntity.insert(_connection, assessments)
  }

  public override suspend fun deleteAssessment(assessment: AssessmentEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfAssessmentEntity.handle(_connection, assessment)
  }

  public override fun observeAllAssessments(): Flow<List<AssessmentEntity>> {
    val _sql: String = "SELECT * FROM funcap_assessments ORDER BY timestampEpochMillis DESC"
    return createFlow(__db, false, arrayOf("funcap_assessments")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestampEpochMillis: Int = getColumnIndexOrThrow(_stmt, "timestampEpochMillis")
        val _columnIndexOfTestType: Int = getColumnIndexOrThrow(_stmt, "testType")
        val _columnIndexOfTotalScore: Int = getColumnIndexOrThrow(_stmt, "totalScore")
        val _columnIndexOfEquivalentFuncap27Score: Int = getColumnIndexOrThrow(_stmt, "equivalentFuncap27Score")
        val _columnIndexOfDomainScoresJson: Int = getColumnIndexOrThrow(_stmt, "domainScoresJson")
        val _columnIndexOfItemResponsesJson: Int = getColumnIndexOrThrow(_stmt, "itemResponsesJson")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<AssessmentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AssessmentEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestampEpochMillis: Long
          _tmpTimestampEpochMillis = _stmt.getLong(_columnIndexOfTimestampEpochMillis)
          val _tmpTestType: String
          _tmpTestType = _stmt.getText(_columnIndexOfTestType)
          val _tmpTotalScore: Double
          _tmpTotalScore = _stmt.getDouble(_columnIndexOfTotalScore)
          val _tmpEquivalentFuncap27Score: Double
          _tmpEquivalentFuncap27Score = _stmt.getDouble(_columnIndexOfEquivalentFuncap27Score)
          val _tmpDomainScoresJson: String
          _tmpDomainScoresJson = _stmt.getText(_columnIndexOfDomainScoresJson)
          val _tmpItemResponsesJson: String
          _tmpItemResponsesJson = _stmt.getText(_columnIndexOfItemResponsesJson)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          _item = AssessmentEntity(_tmpId,_tmpTimestampEpochMillis,_tmpTestType,_tmpTotalScore,_tmpEquivalentFuncap27Score,_tmpDomainScoresJson,_tmpItemResponsesJson,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeAssessmentsByType(type: String): Flow<List<AssessmentEntity>> {
    val _sql: String = "SELECT * FROM funcap_assessments WHERE testType = ? ORDER BY timestampEpochMillis DESC"
    return createFlow(__db, false, arrayOf("funcap_assessments")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, type)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestampEpochMillis: Int = getColumnIndexOrThrow(_stmt, "timestampEpochMillis")
        val _columnIndexOfTestType: Int = getColumnIndexOrThrow(_stmt, "testType")
        val _columnIndexOfTotalScore: Int = getColumnIndexOrThrow(_stmt, "totalScore")
        val _columnIndexOfEquivalentFuncap27Score: Int = getColumnIndexOrThrow(_stmt, "equivalentFuncap27Score")
        val _columnIndexOfDomainScoresJson: Int = getColumnIndexOrThrow(_stmt, "domainScoresJson")
        val _columnIndexOfItemResponsesJson: Int = getColumnIndexOrThrow(_stmt, "itemResponsesJson")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<AssessmentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AssessmentEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestampEpochMillis: Long
          _tmpTimestampEpochMillis = _stmt.getLong(_columnIndexOfTimestampEpochMillis)
          val _tmpTestType: String
          _tmpTestType = _stmt.getText(_columnIndexOfTestType)
          val _tmpTotalScore: Double
          _tmpTotalScore = _stmt.getDouble(_columnIndexOfTotalScore)
          val _tmpEquivalentFuncap27Score: Double
          _tmpEquivalentFuncap27Score = _stmt.getDouble(_columnIndexOfEquivalentFuncap27Score)
          val _tmpDomainScoresJson: String
          _tmpDomainScoresJson = _stmt.getText(_columnIndexOfDomainScoresJson)
          val _tmpItemResponsesJson: String
          _tmpItemResponsesJson = _stmt.getText(_columnIndexOfItemResponsesJson)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          _item = AssessmentEntity(_tmpId,_tmpTimestampEpochMillis,_tmpTestType,_tmpTotalScore,_tmpEquivalentFuncap27Score,_tmpDomainScoresJson,_tmpItemResponsesJson,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllAssessmentsOnce(): List<AssessmentEntity> {
    val _sql: String = "SELECT * FROM funcap_assessments ORDER BY timestampEpochMillis DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestampEpochMillis: Int = getColumnIndexOrThrow(_stmt, "timestampEpochMillis")
        val _columnIndexOfTestType: Int = getColumnIndexOrThrow(_stmt, "testType")
        val _columnIndexOfTotalScore: Int = getColumnIndexOrThrow(_stmt, "totalScore")
        val _columnIndexOfEquivalentFuncap27Score: Int = getColumnIndexOrThrow(_stmt, "equivalentFuncap27Score")
        val _columnIndexOfDomainScoresJson: Int = getColumnIndexOrThrow(_stmt, "domainScoresJson")
        val _columnIndexOfItemResponsesJson: Int = getColumnIndexOrThrow(_stmt, "itemResponsesJson")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<AssessmentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: AssessmentEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestampEpochMillis: Long
          _tmpTimestampEpochMillis = _stmt.getLong(_columnIndexOfTimestampEpochMillis)
          val _tmpTestType: String
          _tmpTestType = _stmt.getText(_columnIndexOfTestType)
          val _tmpTotalScore: Double
          _tmpTotalScore = _stmt.getDouble(_columnIndexOfTotalScore)
          val _tmpEquivalentFuncap27Score: Double
          _tmpEquivalentFuncap27Score = _stmt.getDouble(_columnIndexOfEquivalentFuncap27Score)
          val _tmpDomainScoresJson: String
          _tmpDomainScoresJson = _stmt.getText(_columnIndexOfDomainScoresJson)
          val _tmpItemResponsesJson: String
          _tmpItemResponsesJson = _stmt.getText(_columnIndexOfItemResponsesJson)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          _item = AssessmentEntity(_tmpId,_tmpTimestampEpochMillis,_tmpTestType,_tmpTotalScore,_tmpEquivalentFuncap27Score,_tmpDomainScoresJson,_tmpItemResponsesJson,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAssessmentById(id: Long): AssessmentEntity? {
    val _sql: String = "SELECT * FROM funcap_assessments WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestampEpochMillis: Int = getColumnIndexOrThrow(_stmt, "timestampEpochMillis")
        val _columnIndexOfTestType: Int = getColumnIndexOrThrow(_stmt, "testType")
        val _columnIndexOfTotalScore: Int = getColumnIndexOrThrow(_stmt, "totalScore")
        val _columnIndexOfEquivalentFuncap27Score: Int = getColumnIndexOrThrow(_stmt, "equivalentFuncap27Score")
        val _columnIndexOfDomainScoresJson: Int = getColumnIndexOrThrow(_stmt, "domainScoresJson")
        val _columnIndexOfItemResponsesJson: Int = getColumnIndexOrThrow(_stmt, "itemResponsesJson")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: AssessmentEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestampEpochMillis: Long
          _tmpTimestampEpochMillis = _stmt.getLong(_columnIndexOfTimestampEpochMillis)
          val _tmpTestType: String
          _tmpTestType = _stmt.getText(_columnIndexOfTestType)
          val _tmpTotalScore: Double
          _tmpTotalScore = _stmt.getDouble(_columnIndexOfTotalScore)
          val _tmpEquivalentFuncap27Score: Double
          _tmpEquivalentFuncap27Score = _stmt.getDouble(_columnIndexOfEquivalentFuncap27Score)
          val _tmpDomainScoresJson: String
          _tmpDomainScoresJson = _stmt.getText(_columnIndexOfDomainScoresJson)
          val _tmpItemResponsesJson: String
          _tmpItemResponsesJson = _stmt.getText(_columnIndexOfItemResponsesJson)
          val _tmpNotes: String
          _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          _result = AssessmentEntity(_tmpId,_tmpTimestampEpochMillis,_tmpTestType,_tmpTotalScore,_tmpEquivalentFuncap27Score,_tmpDomainScoresJson,_tmpItemResponsesJson,_tmpNotes)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllAssessments() {
    val _sql: String = "DELETE FROM funcap_assessments"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
