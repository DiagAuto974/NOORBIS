package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(
    entities = [
        PlayerProfileEntity::class,
        CollectedTextEntity::class,
        LevelRecordEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AstroDatabase : RoomDatabase() {
    abstract fun astroDao(): AstroDao

    companion object {
        @Volatile
        private var INSTANCE: AstroDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `player_profile_new` (`id` INTEGER NOT NULL, `selectedColorId` TEXT NOT NULL, `totalScore` INTEGER NOT NULL, `highScore` INTEGER NOT NULL, `unlockedLevel` INTEGER NOT NULL, `gamesPlayed` INTEGER NOT NULL, `totalTargetsSaved` INTEGER NOT NULL, `totalNeuronsPurified` INTEGER NOT NULL, `maxShields` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                connection.execSQL(
                    "INSERT INTO `player_profile_new` (`id`, `selectedColorId`, `totalScore`, `highScore`, `unlockedLevel`, `gamesPlayed`, `totalTargetsSaved`, `totalNeuronsPurified`, `maxShields`) SELECT `id`, `selectedColorId`, `totalScore`, `highScore`, `unlockedLevel`, `gamesPlayed`, 0, 0, 3 FROM `player_profile`"
                )
                connection.execSQL("DROP TABLE `player_profile`")
                connection.execSQL("ALTER TABLE `player_profile_new` RENAME TO `player_profile`")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "CREATE TABLE IF NOT EXISTS `player_profile_new` (`id` INTEGER NOT NULL, `selectedColorId` TEXT NOT NULL, `totalScore` INTEGER NOT NULL, `highScore` INTEGER NOT NULL, `unlockedLevel` INTEGER NOT NULL, `gamesPlayed` INTEGER NOT NULL, `totalTargetsSaved` INTEGER NOT NULL, `totalNeuronsPurified` INTEGER NOT NULL, `maxShields` INTEGER NOT NULL, `isRoundJoystickEnabled` INTEGER NOT NULL, `isDirectTextEnabled` INTEGER NOT NULL, `isGridEnabled` INTEGER NOT NULL, `isMiniMapEnabled` INTEGER NOT NULL, `isSoundEnabled` INTEGER NOT NULL, `isInventoryAscending` INTEGER NOT NULL, PRIMARY KEY(`id`))"
                )
                connection.execSQL(
                    "INSERT INTO `player_profile_new` (`id`, `selectedColorId`, `totalScore`, `highScore`, `unlockedLevel`, `gamesPlayed`, `totalTargetsSaved`, `totalNeuronsPurified`, `maxShields`, `isRoundJoystickEnabled`, `isDirectTextEnabled`, `isGridEnabled`, `isMiniMapEnabled`, `isSoundEnabled`, `isInventoryAscending`) SELECT `id`, `selectedColorId`, `totalScore`, `highScore`, `unlockedLevel`, `gamesPlayed`, `totalTargetsSaved`, `totalNeuronsPurified`, `maxShields`, 1, 1, 0, 1, 1, 0 FROM `player_profile`"
                )
                connection.execSQL("DROP TABLE `player_profile`")
                connection.execSQL("ALTER TABLE `player_profile_new` RENAME TO `player_profile`")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE `player_profile` ADD COLUMN `isEffectsSoundEnabled` INTEGER NOT NULL DEFAULT 1"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    "ALTER TABLE `collected_texts` ADD COLUMN `hasClaimedShareReward` INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getInstance(context: Context): AstroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AstroDatabase::class.java,
                    "astrosphere_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
