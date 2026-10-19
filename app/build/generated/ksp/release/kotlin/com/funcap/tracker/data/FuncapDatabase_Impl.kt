package com.funcap.tracker.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class FuncapDatabase_Impl : FuncapDatabase() {
  private val _assessmentDao: Lazy<AssessmentDao> = lazy {
    AssessmentDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "e8c68c157b7dcf0a2943f964e0599e14", "fb68d763ff0832f1dd7d5c54404c61d8") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `funcap_assessments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestampEpochMillis` INTEGER NOT NULL, `testType` TEXT NOT NULL, `totalScore` REAL NOT NULL, `equivalentFuncap27Score` REAL NOT NULL, `domainScoresJson` TEXT NOT NULL, `itemResponsesJson` TEXT NOT NULL, `notes` TEXT NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'e8c68c157b7dcf0a2943f964e0599e14')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `funcap_assessments`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsFuncapAssessments: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsFuncapAssessments.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFuncapAssessments.put("timestampEpochMillis", TableInfo.Column("timestampEpochMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFuncapAssessments.put("testType", TableInfo.Column("testType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFuncapAssessments.put("totalScore", TableInfo.Column("totalScore", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFuncapAssessments.put("equivalentFuncap27Score", TableInfo.Column("equivalentFuncap27Score", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFuncapAssessments.put("domainScoresJson", TableInfo.Column("domainScoresJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFuncapAssessments.put("itemResponsesJson", TableInfo.Column("itemResponsesJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsFuncapAssessments.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysFuncapAssessments: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesFuncapAssessments: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoFuncapAssessments: TableInfo = TableInfo("funcap_assessments", _columnsFuncapAssessments, _foreignKeysFuncapAssessments, _indicesFuncapAssessments)
        val _existingFuncapAssessments: TableInfo = read(connection, "funcap_assessments")
        if (!_infoFuncapAssessments.equals(_existingFuncapAssessments)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |funcap_assessments(com.funcap.tracker.data.AssessmentEntity).
              | Expected:
              |""".trimMargin() + _infoFuncapAssessments + """
              |
              | Found:
              |""".trimMargin() + _existingFuncapAssessments)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "funcap_assessments")
  }

  public override fun clearAllTables() {
    super.performClear(false, "funcap_assessments")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(AssessmentDao::class, AssessmentDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun assessmentDao(): AssessmentDao = _assessmentDao.value
}
