package com.fiscalia.quindio.data.dao

import androidx.room.*
import com.fiscalia.quindio.data.entity.DailyStat
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyStatDao {

    @Query("SELECT * FROM daily_stats WHERE fecha = :date ORDER BY hora DESC")
    fun getStatsByDate(date: LocalDate): Flow<List<DailyStat>>

    @Query("SELECT * FROM daily_stats ORDER BY fecha DESC, hora DESC")
    fun getAllStats(): Flow<List<DailyStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stat: DailyStat): Long

    @Update
    suspend fun update(stat: DailyStat)

    @Delete
    suspend fun delete(stat: DailyStat)

    @Query("DELETE FROM daily_stats WHERE fecha = :date")
    suspend fun deleteByDate(date: LocalDate)

    @Query("SELECT * FROM daily_stats WHERE fecha = :date LIMIT 1")
    suspend fun getStatForDate(date: LocalDate): DailyStat?

    @Query("SELECT * FROM daily_stats")
    suspend fun getAllStatsSync(): List<DailyStat>
}