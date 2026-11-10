package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AstroDao {
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun observeProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getProfileOnce(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: PlayerProfileEntity)

    @Query("SELECT * FROM collected_texts ORDER BY levelNumber ASC, letter ASC")
    fun observeAllCollectedTexts(): Flow<List<CollectedTextEntity>>

    @Query("SELECT * FROM collected_texts ORDER BY levelNumber ASC, letter ASC")
    suspend fun getAllCollectedTextsOnce(): List<CollectedTextEntity>

    @Query("SELECT * FROM collected_texts WHERE id = :id")
    suspend fun getCollectedTextById(id: String): CollectedTextEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCollectedText(textEntity: CollectedTextEntity)

    @Query("DELETE FROM collected_texts")
    suspend fun clearAllCollectedTexts()

    @Query("SELECT * FROM level_records ORDER BY levelNumber ASC")
    fun observeLevelRecords(): Flow<List<LevelRecordEntity>>

    @Query("SELECT * FROM level_records ORDER BY levelNumber ASC")
    suspend fun getAllLevelRecordsOnce(): List<LevelRecordEntity>

    @Query("SELECT * FROM level_records WHERE levelNumber = :levelNumber")
    suspend fun getLevelRecordOnce(levelNumber: Int): LevelRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLevelRecord(record: LevelRecordEntity)

    @Query("DELETE FROM level_records")
    suspend fun clearAllLevelRecords()
}
