package com.apriltag.aidetector.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Entity mapping representing a logged AprilTag scan record.
 */
@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "tag_id") val tagId: Int,
    @ColumnInfo(name = "family") val family: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "distance") val distance: Float,
    @ColumnInfo(name = "pitch") val pitch: Float,
    @ColumnInfo(name = "roll") val roll: Float,
    @ColumnInfo(name = "yaw") val yaw: Float,
    @ColumnInfo(name = "scenario") val scenario: String,
    @ColumnInfo(name = "custom_notes") val customNotes: String? = null
)

/**
 * Room Data Access Object (DAO) for local SQL transactions.
 */
@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanHistoryEntity>>

    @Query("SELECT * FROM scan_history WHERE tag_id = :tagId OR family LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchScans(tagId: Int, query: String): Flow<List<ScanHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanHistoryEntity)

    @Delete
    suspend fun deleteScan(scan: ScanHistoryEntity)

    @Query("DELETE FROM scan_history")
    suspend fun clearAll()
}

/**
 * Persistent SQLite Database wrapper.
 */
@Database(entities = [ScanHistoryEntity::class], version = 1, exportSchema = false)
abstract class ScanHistoryDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: ScanHistoryDatabase? = null

        fun getDatabase(context: Context): ScanHistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScanHistoryDatabase::class.java,
                    "apriltag_ai_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Scan History repository interface assisting clean Jetpack Compose ViewModels.
 */
class ScanHistoryRepository(private val dao: ScanHistoryDao) {
    val allScans: Flow<List<ScanHistoryEntity>> = dao.getAllScans()

    suspend fun addScan(scan: ScanHistoryEntity) = dao.insertScan(scan)

    suspend fun deleteScan(scan: ScanHistoryEntity) = dao.deleteScan(scan)

    suspend fun clearAll() = dao.clearAll()
}
