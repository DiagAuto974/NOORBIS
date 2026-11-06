package com.example.data.repository

import com.example.data.local.AstroDao
import com.example.data.local.CollectedTextEntity
import com.example.data.local.LevelRecordEntity
import com.example.data.local.PlayerProfileEntity
import com.example.domain.model.FloatingTextRelic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AstroRepository(private val dao: AstroDao) {

    val playerProfile: Flow<PlayerProfileEntity> = dao.observeProfile().map { profile ->
        val base = profile ?: PlayerProfileEntity()
        if (base.unlockedLevel < 135) base.copy(unlockedLevel = 135) else base
    }

    val collectedTexts: Flow<List<CollectedTextEntity>> = dao.observeAllCollectedTexts()

    val levelRecords: Flow<List<LevelRecordEntity>> = dao.observeLevelRecords()

    suspend fun ensureInitialized() {
        val current = dao.getProfileOnce()
        if (current == null) {
            dao.upsertProfile(PlayerProfileEntity(unlockedLevel = 135))
        } else if (current.unlockedLevel < 135 || current.maxShields < 3) {
            dao.upsertProfile(
                current.copy(
                    unlockedLevel = maxOf(current.unlockedLevel, 135),
                    maxShields = current.maxShields.coerceIn(3, 7)
                )
            )
        }
    }

    suspend fun getProfileOnce(): PlayerProfileEntity {
        return dao.getProfileOnce() ?: PlayerProfileEntity()
    }

    suspend fun updateCumulativeStats(
        totalTargetsSaved: Int,
        totalNeuronsPurified: Int,
        maxShields: Int
    ) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(
            current.copy(
                totalTargetsSaved = maxOf(current.totalTargetsSaved, totalTargetsSaved),
                totalNeuronsPurified = maxOf(current.totalNeuronsPurified, totalNeuronsPurified),
                maxShields = maxOf(current.maxShields, maxShields).coerceIn(3, 7)
            )
        )
    }

    fun computeCollectedTextId(levelNumber: Int, relic: FloatingTextRelic): String {
        return if (relic.isBrainBonus) {
            "BRAIN_L${relic.brainLevel}"
        } else if (relic.fruitId.isNotEmpty() && relic.scriptureRef.isNotEmpty()) {
            "M_${relic.fruitId}_${relic.scriptureRef}".replace(Regex("[^a-zA-Z0-9_]"), "_")
        } else {
            "L${levelNumber}_${relic.letter}"
        }
    }

    /**
     * Attribue le bonus de partage (+3 Pensées Positives) UNE SEULE FOIS par texte.
     * Retourne `true` si la récompense est accordée, `false` si ce texte a déjà donné sa récompense.
     */
    suspend fun claimSharedTextRewardOnce(
        textId: String,
        bonusPoints: Int
    ): Pair<Boolean, PlayerProfileEntity> {
        val existingText = dao.getCollectedTextById(textId)
        val currentProfile = dao.getProfileOnce() ?: PlayerProfileEntity()
        if (existingText != null && existingText.hasClaimedShareReward) {
            return Pair(false, currentProfile)
        }

        if (existingText != null) {
            dao.upsertCollectedText(existingText.copy(hasClaimedShareReward = true))
        }

        val prevSaved = currentProfile.totalTargetsSaved
        val newTotalTargetsSaved = prevSaved + 3
        val crossedFiftyMilestone = (prevSaved / 50) < (newTotalTargetsSaved / 50)
        val newMaxShields = if (crossedFiftyMilestone) {
            (currentProfile.maxShields + 1).coerceIn(3, 7)
        } else {
            currentProfile.maxShields.coerceIn(3, 7)
        }
        val newTotalScore = currentProfile.totalScore + bonusPoints
        val updatedProfile = currentProfile.copy(
            totalScore = newTotalScore,
            highScore = maxOf(currentProfile.highScore, newTotalScore),
            totalTargetsSaved = newTotalTargetsSaved,
            maxShields = newMaxShields
        )
        dao.upsertProfile(updatedProfile)
        return Pair(true, updatedProfile)
    }

    suspend fun updateSelectedSphereColor(colorId: String) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(current.copy(selectedColorId = colorId))
    }

    suspend fun updateSettings(
        isRoundJoystickEnabled: Boolean? = null,
        isDirectTextEnabled: Boolean? = null,
        isGridEnabled: Boolean? = null,
        isMiniMapEnabled: Boolean? = null,
        isSoundEnabled: Boolean? = null,
        isEffectsSoundEnabled: Boolean? = null,
        isInventoryAscending: Boolean? = null
    ) {
        val current = dao.getProfileOnce() ?: PlayerProfileEntity()
        dao.upsertProfile(
            current.copy(
                isRoundJoystickEnabled = isRoundJoystickEnabled ?: current.isRoundJoystickEnabled,
                isDirectTextEnabled = isDirectTextEnabled ?: current.isDirectTextEnabled,
                isGridEnabled = isGridEnabled ?: current.isGridEnabled,
                isMiniMapEnabled = isMiniMapEnabled ?: current.isMiniMapEnabled,
                isSoundEnabled = isSoundEnabled ?: current.isSoundEnabled,
                isEffectsSoundEnabled = isEffectsSoundEnabled ?: current.isEffectsSoundEnabled,
                isInventoryAscending = isInventoryAscending ?: current.isInventoryAscending
            )
        )
    }

    suspend fun saveCollectedText(
        levelNumber: Int,
        sectorName: String,
        relic: FloatingTextRelic,
        sphereColorId: String
    ) {
        val itemId = computeCollectedTextId(levelNumber, relic)
        val existing = dao.getCollectedTextById(itemId)
        val updatedCount = (existing?.collectedCount ?: 0) + 1

        val entity = CollectedTextEntity(
            id = itemId,
            levelNumber = levelNumber,
            letter = if (relic.isBrainBonus) "🧠" else relic.letter,
            title = relic.title,
            content = relic.content,
            sectorName = sectorName,
            pointsAwarded = relic.points,
            sphereColorId = sphereColorId,
            collectedCount = updatedCount,
            lastCollectedTimestamp = System.currentTimeMillis(),
            hasClaimedShareReward = existing?.hasClaimedShareReward ?: false
        )
        dao.upsertCollectedText(entity)

        // Update level record flags for the specific slot collected (only if fruit relic)
        if (!relic.isBrainBonus) {
            val existingRecord = dao.getLevelRecordOnce(levelNumber) ?: LevelRecordEntity(levelNumber = levelNumber)
            val updatedRecord = existingRecord.copy(
                hasTextA = existingRecord.hasTextA || relic.slotIndex == 0 || relic.letter.equals("A", ignoreCase = true),
                hasTextB = existingRecord.hasTextB || relic.slotIndex == 1 || relic.letter.equals("B", ignoreCase = true),
                hasTextC = existingRecord.hasTextC || relic.slotIndex == 2 || relic.letter.equals("C", ignoreCase = true)
            )
            dao.upsertLevelRecord(updatedRecord)
        }

        // Add points to player profile immediately so inventory & score stay in sync
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val newTotal = profile.totalScore + relic.points
        dao.upsertProfile(
            profile.copy(
                totalScore = newTotal,
                highScore = maxOf(profile.highScore, newTotal)
            )
        )
    }

    suspend fun recordLevelCompletion(
        levelNumber: Int,
        levelRunScore: Int,
        completionBonus: Int,
        targetScoreDelta: Int = 0,
        collectedTextA: Boolean = false,
        collectedTextB: Boolean = false,
        collectedTextC: Boolean = false,
        totalTargetsSaved: Int? = null,
        totalNeuronsPurified: Int? = null,
        maxShields: Int? = null
    ) {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val newTotal = (profile.totalScore + completionBonus + targetScoreDelta).coerceAtLeast(0)
        val nextUnlocked = maxOf(profile.unlockedLevel, levelNumber + 1)

        dao.upsertProfile(
            profile.copy(
                totalScore = newTotal,
                highScore = maxOf(profile.highScore, levelRunScore, newTotal),
                unlockedLevel = nextUnlocked,
                gamesPlayed = profile.gamesPlayed + 1,
                totalTargetsSaved = if (totalTargetsSaved != null) maxOf(profile.totalTargetsSaved, totalTargetsSaved) else profile.totalTargetsSaved,
                totalNeuronsPurified = if (totalNeuronsPurified != null) maxOf(profile.totalNeuronsPurified, totalNeuronsPurified) else profile.totalNeuronsPurified,
                maxShields = if (maxShields != null) maxOf(profile.maxShields, maxShields).coerceIn(3, 7) else profile.maxShields.coerceIn(3, 7)
            )
        )

        val existingRecord = dao.getLevelRecordOnce(levelNumber) ?: LevelRecordEntity(levelNumber = levelNumber)
        dao.upsertLevelRecord(
            existingRecord.copy(
                bestScore = maxOf(existingRecord.bestScore, levelRunScore),
                hasTextA = existingRecord.hasTextA || collectedTextA,
                hasTextB = existingRecord.hasTextB || collectedTextB,
                hasTextC = existingRecord.hasTextC || collectedTextC,
                isCompleted = true
            )
        )
    }

    suspend fun recordGameOver(
        levelNumber: Int,
        runScore: Int,
        targetScoreDelta: Int = 0,
        totalTargetsSaved: Int? = null,
        totalNeuronsPurified: Int? = null,
        maxShields: Int? = null
    ) {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val newTotal = (profile.totalScore + targetScoreDelta).coerceAtLeast(0)
        dao.upsertProfile(
            profile.copy(
                totalScore = newTotal,
                highScore = maxOf(profile.highScore, runScore, newTotal),
                gamesPlayed = profile.gamesPlayed + 1,
                totalTargetsSaved = if (totalTargetsSaved != null) maxOf(profile.totalTargetsSaved, totalTargetsSaved) else profile.totalTargetsSaved,
                totalNeuronsPurified = if (totalNeuronsPurified != null) maxOf(profile.totalNeuronsPurified, totalNeuronsPurified) else profile.totalNeuronsPurified,
                maxShields = if (maxShields != null) maxOf(profile.maxShields, maxShields).coerceIn(3, 7) else profile.maxShields.coerceIn(3, 7)
            )
        )
        val existingRecord = dao.getLevelRecordOnce(levelNumber) ?: LevelRecordEntity(levelNumber = levelNumber)
        if (runScore > existingRecord.bestScore) {
            dao.upsertLevelRecord(existingRecord.copy(bestScore = runScore))
        }
    }
}
