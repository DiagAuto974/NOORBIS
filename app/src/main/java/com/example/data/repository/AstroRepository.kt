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

    suspend fun exportSaveToJson(context: android.content.Context? = null): String {
        val profile = dao.getProfileOnce() ?: PlayerProfileEntity()
        val texts = dao.getAllCollectedTextsOnce()
        val records = dao.getAllLevelRecordsOnce()

        val root = org.json.JSONObject()
        root.put("app", "Noôrbis")
        root.put("version", 2)
        root.put("storageKey", "noorbis_web_state_v2")
        root.put("selectedColorId", profile.selectedColorId)
        root.put("unlockedLevel", profile.unlockedLevel)
        root.put("totalScore", profile.totalScore)
        root.put("highScore", profile.highScore)
        root.put("gamesPlayed", profile.gamesPlayed)
        root.put("totalTargetsSaved", profile.totalTargetsSaved)
        root.put("totalNeuronsPurified", profile.totalNeuronsPurified)
        root.put("positiveThoughtsCount", profile.totalTargetsSaved + profile.totalNeuronsPurified)
        root.put("maxShields", profile.maxShields)

        val settingsObj = org.json.JSONObject().apply {
            put("sfxEnabled", profile.isEffectsSoundEnabled)
            put("soundEnabled", profile.isSoundEnabled)
            put("inventorySortAscending", profile.isInventoryAscending)
            put("joystickEnabled", profile.isRoundJoystickEnabled)
            put("directTextEnabled", profile.isDirectTextEnabled)
            put("showGridEnabled", profile.isGridEnabled)
            put("showMiniMapEnabled", profile.isMiniMapEnabled)
        }
        root.put("settings", settingsObj)

        val collectedArr = org.json.JSONArray()
        texts.forEach { t ->
            val obj = org.json.JSONObject().apply {
                put("id", t.id)
                put("levelNumber", t.levelNumber)
                put("letter", t.letter)
                put("title", t.title)
                put("fruit", t.letter)
                put("situation", t.sectorName)
                put("reference", t.title)
                put("content", t.content)
                put("sectorName", t.sectorName)
                put("pointsAwarded", t.pointsAwarded)
                put("sphereColorId", t.sphereColorId)
                put("collectedCount", t.collectedCount)
                put("collectedAt", t.lastCollectedTimestamp)
                put("hasClaimedShareReward", t.hasClaimedShareReward)
            }
            collectedArr.put(obj)
        }
        root.put("collectedItems", collectedArr)

        val recordsObj = org.json.JSONObject()
        records.forEach { r ->
            val rObj = org.json.JSONObject().apply {
                put("levelNumber", r.levelNumber)
                put("bestScore", r.bestScore)
                put("hasTextA", r.hasTextA)
                put("hasTextB", r.hasTextB)
                put("hasTextC", r.hasTextC)
                put("isCompleted", r.isCompleted)
            }
            recordsObj.put(r.levelNumber.toString(), rObj)
        }
        root.put("levelRecords", recordsObj)

        val jsonStr = root.toString(2)
        context?.getSharedPreferences("noorbis_local_storage", android.content.Context.MODE_PRIVATE)
            ?.edit()
            ?.putString("noorbis_web_state_v2", jsonStr)
            ?.apply()
        return jsonStr
    }

    suspend fun importSaveFromJson(context: android.content.Context? = null, jsonString: String): Boolean {
        return try {
            val rawRoot = org.json.JSONObject(jsonString)
            val root = rawRoot.optJSONObject("state") ?: rawRoot

            val current = dao.getProfileOnce() ?: PlayerProfileEntity()
            val settings = root.optJSONObject("settings")

            val selectedColorId = root.optString("selectedColorId", current.selectedColorId)
            val totalScore = root.optInt("totalScore", current.totalScore).coerceAtLeast(0)
            val highScore = maxOf(totalScore, root.optInt("highScore", current.highScore))
            val unlockedLevel = root.optInt("unlockedLevel", current.unlockedLevel).coerceIn(1, 135)
            val gamesPlayed = root.optInt("gamesPlayed", current.gamesPlayed).coerceAtLeast(0)
            val totalTargetsSaved = root.optInt(
                "totalTargetsSaved",
                root.optInt("positiveThoughtsCount", current.totalTargetsSaved)
            ).coerceAtLeast(0)
            val totalNeuronsPurified = root.optInt("totalNeuronsPurified", current.totalNeuronsPurified).coerceAtLeast(0)
            val maxShields = root.optInt(
                "maxShields",
                (3 + (totalTargetsSaved + totalNeuronsPurified) / 50).coerceIn(3, 7)
            ).coerceIn(3, 7)

            val updatedProfile = current.copy(
                selectedColorId = selectedColorId,
                totalScore = totalScore,
                highScore = highScore,
                unlockedLevel = unlockedLevel,
                gamesPlayed = gamesPlayed,
                totalTargetsSaved = totalTargetsSaved,
                totalNeuronsPurified = totalNeuronsPurified,
                maxShields = maxShields,
                isRoundJoystickEnabled = settings?.optBoolean("joystickEnabled", current.isRoundJoystickEnabled)
                    ?: root.optBoolean("isRoundJoystickEnabled", current.isRoundJoystickEnabled),
                isDirectTextEnabled = settings?.optBoolean("directTextEnabled", current.isDirectTextEnabled)
                    ?: root.optBoolean("isDirectTextEnabled", current.isDirectTextEnabled),
                isGridEnabled = settings?.optBoolean("showGridEnabled", current.isGridEnabled)
                    ?: root.optBoolean("isGridEnabled", current.isGridEnabled),
                isMiniMapEnabled = settings?.optBoolean("showMiniMapEnabled", current.isMiniMapEnabled)
                    ?: root.optBoolean("isMiniMapEnabled", current.isMiniMapEnabled),
                isSoundEnabled = settings?.optBoolean("soundEnabled", current.isSoundEnabled)
                    ?: root.optBoolean("isSoundEnabled", current.isSoundEnabled),
                isEffectsSoundEnabled = settings?.optBoolean("sfxEnabled", current.isEffectsSoundEnabled)
                    ?: root.optBoolean("isEffectsSoundEnabled", current.isEffectsSoundEnabled),
                isInventoryAscending = settings?.optBoolean("inventorySortAscending", current.isInventoryAscending)
                    ?: root.optBoolean("isInventoryAscending", current.isInventoryAscending)
            )
            dao.upsertProfile(updatedProfile)

            val itemsArr = root.optJSONArray("collectedItems") ?: root.optJSONArray("collectedTexts")
            if (itemsArr != null) {
                for (i in 0 until itemsArr.length()) {
                    val item = itemsArr.optJSONObject(i) ?: continue
                    val rawId = item.opt("id")?.toString()?.takeIf { it.isNotBlank() } ?: continue
                    val idStr = if (rawId.all { it.isDigit() }) {
                        val numId = rawId.toIntOrNull() ?: 0
                        if (numId in 1001..1072) "BRAIN_L${numId - 1000}" else "WEB_M_$numId"
                    } else {
                        rawId
                    }
                    val entity = CollectedTextEntity(
                        id = idStr,
                        levelNumber = item.optInt("levelNumber", 1).coerceIn(1, 135),
                        letter = item.optString("letter", item.optString("fruit", "✦")).ifBlank { "✦" },
                        title = item.optString("title", item.optString("reference", "Méditation")).ifBlank { "Méditation" },
                        content = item.optString("content", ""),
                        sectorName = item.optString("sectorName", item.optString("situation", "Secteur 1")),
                        pointsAwarded = item.optInt("pointsAwarded", 100),
                        sphereColorId = item.optString("sphereColorId", selectedColorId),
                        collectedCount = item.optInt("collectedCount", 1).coerceAtLeast(1),
                        lastCollectedTimestamp = item.optLong("collectedAt", item.optLong("lastCollectedTimestamp", System.currentTimeMillis())),
                        hasClaimedShareReward = item.optBoolean("hasClaimedShareReward", false)
                    )
                    if (entity.content.isNotBlank()) {
                        dao.upsertCollectedText(entity)
                    }
                }
            }

            val recordsObj = root.optJSONObject("levelRecords")
            if (recordsObj != null) {
                val keys = recordsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val rObj = recordsObj.optJSONObject(key) ?: continue
                    val lvl = rObj.optInt("levelNumber", key.toIntOrNull() ?: 0)
                    if (lvl in 1..135) {
                        val isComp = rObj.optBoolean("isCompleted", false)
                        val rec = LevelRecordEntity(
                            levelNumber = lvl,
                            bestScore = rObj.optInt("bestScore", 0),
                            hasTextA = rObj.optBoolean("hasTextA", isComp),
                            hasTextB = rObj.optBoolean("hasTextB", isComp),
                            hasTextC = rObj.optBoolean("hasTextC", isComp),
                            isCompleted = isComp
                        )
                        dao.upsertLevelRecord(rec)
                    }
                }
            }

            context?.getSharedPreferences("noorbis_local_storage", android.content.Context.MODE_PRIVATE)
                ?.edit()
                ?.putString("noorbis_web_state_v2", root.toString())
                ?.apply()
            true
        } catch (e: Exception) {
            false
        }
    }
}
