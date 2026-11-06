package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val selectedColorId: String = "cyan",
    val totalScore: Int = 0,
    val highScore: Int = 0,
    val unlockedLevel: Int = 135,
    val gamesPlayed: Int = 0,
    val totalTargetsSaved: Int = 0,
    val totalNeuronsPurified: Int = 0,
    val maxShields: Int = 3,
    val isRoundJoystickEnabled: Boolean = true,
    val isDirectTextEnabled: Boolean = true,
    val isGridEnabled: Boolean = false,
    val isMiniMapEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isEffectsSoundEnabled: Boolean = true,
    val isInventoryAscending: Boolean = false
)

@Entity(tableName = "collected_texts")
data class CollectedTextEntity(
    @PrimaryKey val id: String, // Format: "L{level}_{letter}" e.g., "L1_A"
    val levelNumber: Int,
    val letter: String, // "A", "B", or "C"
    val title: String,
    val content: String,
    val sectorName: String,
    val pointsAwarded: Int,
    val sphereColorId: String,
    val collectedCount: Int = 1,
    val lastCollectedTimestamp: Long = System.currentTimeMillis(),
    val hasClaimedShareReward: Boolean = false
)

@Entity(tableName = "level_records")
data class LevelRecordEntity(
    @PrimaryKey val levelNumber: Int,
    val bestScore: Int = 0,
    val hasTextA: Boolean = false,
    val hasTextB: Boolean = false,
    val hasTextC: Boolean = false,
    val isCompleted: Boolean = false
)
