package com.fiscalia.quindio.repository

import com.fiscalia.quindio.data.dao.DailyStatDao
import com.fiscalia.quindio.data.dao.HistoricalStatDao
import com.fiscalia.quindio.data.entity.DailyStat
import com.fiscalia.quindio.data.entity.HistoricalStat
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

// Eliminamos @Singleton y @Inject - creamos el repositorio manualmente
class StatRepository(
    private val dailyStatDao: DailyStatDao,
    private val historicalStatDao: HistoricalStatDao
) {

    fun getTodayStats(date: LocalDate): Flow<List<DailyStat>> =
        dailyStatDao.getStatsByDate(date)

    fun getAllDailyStats(): Flow<List<DailyStat>> =
        dailyStatDao.getAllStats()

    fun getAllHistoricalStats(): Flow<List<HistoricalStat>> =
        historicalStatDao.getAllHistorical()

    suspend fun saveStat(stat: DailyStat): Long =
        dailyStatDao.insert(stat)

    suspend fun updateStat(stat: DailyStat) =
        dailyStatDao.update(stat)

    suspend fun archiveDailyStats() {
        val allStats = dailyStatDao.getAllStatsSync()
        val today = LocalDate.now()

        val historicalStats = allStats.map { dailyStat ->
            HistoricalStat(
                fecha = dailyStat.fecha,
                hora = dailyStat.hora,
                sede = dailyStat.sede,
                guarda = dailyStat.guarda,
                funcionarios = dailyStat.funcionarios,
                contratistas = dailyStat.contratistas,
                visitantes = dailyStat.visitantes,
                archivedDate = today
            )
        }

        if (historicalStats.isNotEmpty()) {
            historicalStatDao.insertAll(historicalStats)
        }
    }

    suspend fun clearDailyStats() {
        dailyStatDao.deleteByDate(LocalDate.now().minusDays(1))
    }

    suspend fun getStatsForExport(date: LocalDate): List<DailyStat> {
        return dailyStatDao.getAllStatsSync().filter { it.fecha == date }
    }
}