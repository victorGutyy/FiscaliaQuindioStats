package com.fiscalia.quindio.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fiscalia.quindio.data.dao.DailyStatDao
import com.fiscalia.quindio.data.dao.HistoricalStatDao
import com.fiscalia.quindio.data.entity.DailyStat
import com.fiscalia.quindio.data.entity.HistoricalStat

@Database(
    entities = [DailyStat::class, HistoricalStat::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dailyStatDao(): DailyStatDao
    abstract fun historicalStatDao(): HistoricalStatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fiscalia_stats_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}