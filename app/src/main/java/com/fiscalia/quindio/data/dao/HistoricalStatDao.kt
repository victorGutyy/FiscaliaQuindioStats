package com.fiscalia.quindio.data.dao

import androidx.room.*
import com.fiscalia.quindio.data.entity.HistoricalStat
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricalStatDao {

    @Query("SELECT * FROM historical_stats ORDER BY archivedDate DESC, fecha DESC")
    fun getAllHistorical(): Flow<List<HistoricalStat>>

    @Insert
    suspend fun insert(historicalStat: HistoricalStat)

    @Insert
    suspend fun insertAll(stats: List<HistoricalStat>)

    @Query("SELECT * FROM historical_stats WHERE fecha BETWEEN :startDate AND :endDate")
    suspend fun getStatsBetweenDates(startDate: String, endDate: String): List<HistoricalStat>

    @Query("DELETE FROM historical_stats WHERE archivedDate < :date")
    suspend fun deleteOldRecords(date: String)
}