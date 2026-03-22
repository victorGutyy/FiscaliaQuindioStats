package com.fiscalia.quindio.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "historical_stats")
data class HistoricalStat(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fecha: LocalDate,
    val hora: LocalTime,
    val sede: String,
    val guarda: String,
    val funcionarios: Int,
    val contratistas: Int,
    val visitantes: Int,
    val menoresEdad: Int = 0,
    val archivedDate: LocalDate // Fecha en que se archivó
)