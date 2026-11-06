package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AstroDatabase
import com.example.data.repository.AstroRepository
import com.example.domain.model.BrainBonusCatalog
import com.example.domain.model.SpaceSectorCatalog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Noôrbis", appName)
  }

  @Test
  fun `verify 135 levels and brain bonuses only in first 72 levels`() {
    assertEquals(135, SpaceSectorCatalog.levels.size)
    for (lvl in 1..72) {
      assertNotNull("Expected brain bonus for level $lvl", BrainBonusCatalog.getFragmentForLevel(lvl))
    }
    for (lvl in 73..135) {
      assertNull("Expected no brain bonus for level $lvl", BrainBonusCatalog.getFragmentForLevel(lvl))
    }
  }

  @Test
  fun `level completion only validates collected relics and persists cumulative counters`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AstroDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    try {
      val repo = AstroRepository(db.astroDao())
      repo.ensureInitialized()

      // Complete level 1 with only relic A and C collected (not B)
      repo.recordLevelCompletion(
        levelNumber = 1,
        levelRunScore = 1200,
        completionBonus = 300,
        targetScoreDelta = 20,
        collectedTextA = true,
        collectedTextB = false,
        collectedTextC = true,
        totalTargetsSaved = 52,
        totalNeuronsPurified = 18,
        maxShields = 4
      )

      val records = repo.levelRecords.first()
      val level1Record = records.find { it.levelNumber == 1 }
      assertNotNull(level1Record)
      assertTrue(level1Record!!.hasTextA)
      assertFalse(level1Record.hasTextB)
      assertTrue(level1Record.hasTextC)
      assertTrue(level1Record.isCompleted)

      val profile = repo.getProfileOnce()
      assertEquals(52, profile.totalTargetsSaved)
      assertEquals(18, profile.totalNeuronsPurified)
      assertEquals(4, profile.maxShields)
    } finally {
      db.close()
    }
  }

  @Test
  fun `settings are persisted in Room and survive repository reads`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AstroDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    try {
      val repo = AstroRepository(db.astroDao())
      repo.ensureInitialized()

      // Verify initial default settings
      val initial = repo.getProfileOnce()
      assertTrue(initial.isRoundJoystickEnabled)
      assertTrue(initial.isDirectTextEnabled)
      assertFalse(initial.isGridEnabled)
      assertTrue(initial.isMiniMapEnabled)
      assertTrue(initial.isSoundEnabled)
      assertTrue(initial.isEffectsSoundEnabled)
      assertFalse(initial.isInventoryAscending)

      // Update all 7 settings
      repo.updateSettings(
        isRoundJoystickEnabled = false,
        isDirectTextEnabled = false,
        isGridEnabled = true,
        isMiniMapEnabled = false,
        isSoundEnabled = false,
        isEffectsSoundEnabled = false,
        isInventoryAscending = true
      )

      // Verify persisted values
      val updated = repo.getProfileOnce()
      assertFalse(updated.isRoundJoystickEnabled)
      assertFalse(updated.isDirectTextEnabled)
      assertTrue(updated.isGridEnabled)
      assertFalse(updated.isMiniMapEnabled)
      assertFalse(updated.isSoundEnabled)
      assertFalse(updated.isEffectsSoundEnabled)
      assertTrue(updated.isInventoryAscending)
    } finally {
      db.close()
    }
  }

  @Test
  fun `non-destructive migrations 1 to 2 and 2 to 3 preserve player progress`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbFile = context.getDatabasePath("migration_test_astro.db")
    dbFile.delete()

    // Create a version 1 SQLite database directly with existing player progress
    val sqliteDb = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
    try {
      sqliteDb.execSQL(
        "CREATE TABLE IF NOT EXISTS `player_profile` (`id` INTEGER NOT NULL, `selectedColorId` TEXT NOT NULL, `totalScore` INTEGER NOT NULL, `highScore` INTEGER NOT NULL, `unlockedLevel` INTEGER NOT NULL, `gamesPlayed` INTEGER NOT NULL, PRIMARY KEY(`id`))"
      )
      sqliteDb.execSQL(
        "INSERT INTO `player_profile` (`id`, `selectedColorId`, `totalScore`, `highScore`, `unlockedLevel`, `gamesPlayed`) VALUES (1, 'gold', 4500, 6200, 135, 12)"
      )
      sqliteDb.execSQL(
        "CREATE TABLE IF NOT EXISTS `collected_texts` (`id` TEXT NOT NULL, `levelNumber` INTEGER NOT NULL, `letter` TEXT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `sectorName` TEXT NOT NULL, `pointsAwarded` INTEGER NOT NULL, `sphereColorId` TEXT NOT NULL, `collectedCount` INTEGER NOT NULL, `lastCollectedTimestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))"
      )
      sqliteDb.execSQL(
        "CREATE TABLE IF NOT EXISTS `level_records` (`levelNumber` INTEGER NOT NULL, `bestScore` INTEGER NOT NULL, `hasTextA` INTEGER NOT NULL, `hasTextB` INTEGER NOT NULL, `hasTextC` INTEGER NOT NULL, `isCompleted` INTEGER NOT NULL, PRIMARY KEY(`levelNumber`))"
      )
      sqliteDb.execSQL(
        "INSERT INTO `level_records` (`levelNumber`, `bestScore`, `hasTextA`, `hasTextB`, `hasTextC`, `isCompleted`) VALUES (1, 1800, 1, 0, 1, 1)"
      )
      sqliteDb.version = 1
    } finally {
      sqliteDb.close()
    }

    // Open with Room at version 4 using MIGRATION_1_2, MIGRATION_2_3 and MIGRATION_3_4 (no fallbackToDestructiveMigration)
    val migratedDb = Room.databaseBuilder(context, AstroDatabase::class.java, "migration_test_astro.db")
      .addMigrations(AstroDatabase.MIGRATION_1_2, AstroDatabase.MIGRATION_2_3, AstroDatabase.MIGRATION_3_4)
      .allowMainThreadQueries()
      .build()
    try {
      val repo = AstroRepository(migratedDb.astroDao())
      val profile = repo.getProfileOnce()
      assertEquals("gold", profile.selectedColorId)
      assertEquals(4500, profile.totalScore)
      assertEquals(6200, profile.highScore)
      assertEquals(135, profile.unlockedLevel)
      assertEquals(12, profile.gamesPlayed)
      assertEquals(3, profile.maxShields)
      assertTrue(profile.isRoundJoystickEnabled)
      assertTrue(profile.isDirectTextEnabled)
      assertFalse(profile.isGridEnabled)
      assertTrue(profile.isMiniMapEnabled)
      assertTrue(profile.isSoundEnabled)
      assertTrue(profile.isEffectsSoundEnabled)
      assertFalse(profile.isInventoryAscending)

      val level1 = migratedDb.astroDao().getLevelRecordOnce(1)
      assertNotNull(level1)
      assertEquals(1800, level1!!.bestScore)
      assertTrue(level1.hasTextA)
      assertFalse(level1.hasTextB)
      assertTrue(level1.hasTextC)
      assertTrue(level1.isCompleted)
    } finally {
      migratedDb.close()
      dbFile.delete()
    }
  }

  @Test
  fun `inventory keyword search matches meditations and brain fragments ignoring case and accents`() {
    val meditationItem = com.example.data.local.CollectedTextEntity(
      id = "L1_AMOUR",
      levelNumber = 1,
      letter = "Amour",
      title = "Amour • Malgré l'attente (Genèse 29.20)",
      content = "L'amour transforme l'attente. Les années deviennent légères.",
      sectorName = "Ceinture d'Orion",
      pointsAwarded = 100,
      sphereColorId = "cyan",
      collectedCount = 1,
      lastCollectedTimestamp = 1000L
    )
    val brainItem = com.example.data.local.CollectedTextEntity(
      id = "BRAIN_12",
      levelNumber = 12,
      letter = "🧠",
      title = "Fragment #12 • Présomption d'innocence",
      content = "Nul ne peut être condamné sans preuve certaine.",
      sectorName = "Nébuleuse Juridique",
      pointsAwarded = 150,
      sphereColorId = "gold",
      collectedCount = 1,
      lastCollectedTimestamp = 2000L
    )

    // Matches title without accents
    assertTrue(com.example.ui.screens.matchesInventorySearch(meditationItem, "genese 29"))
    // Matches content keyword
    assertTrue(com.example.ui.screens.matchesInventorySearch(meditationItem, "legeres"))
    // Matches sector
    assertTrue(com.example.ui.screens.matchesInventorySearch(meditationItem, "orion"))
    // Matches level number
    assertTrue(com.example.ui.screens.matchesInventorySearch(brainItem, "niveau 12"))
    // Matches brain fragment content
    assertTrue(com.example.ui.screens.matchesInventorySearch(brainItem, "presomption"))
    // Non-matching keyword returns false
    assertFalse(com.example.ui.screens.matchesInventorySearch(meditationItem, "introuvable"))
  }
}

