package com.fiscalia.quindio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fiscalia.quindio.data.database.AppDatabase
import com.fiscalia.quindio.data.entity.DailyStat
import com.fiscalia.quindio.repository.StatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class StatViewModel(application: Application) : AndroidViewModel(application) {

    // Creamos la base de datos y repositorio manualmente (sin inyección)
    private val database = AppDatabase.getDatabase(application)
    private val repository = StatRepository(
        database.dailyStatDao(),
        database.historicalStatDao()
    )

    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today.asStateFlow()

    val todayStats: StateFlow<List<DailyStat>> = repository.getTodayStats(_today.value)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess: SharedFlow<Boolean> = _saveSuccess.asSharedFlow()

    private val _errorMessage = MutableSharedFlow<String>()
    val errorMessage: SharedFlow<String> = _errorMessage.asSharedFlow()

    val sedes = listOf(
        "CTI CALARCÁ",
        "CTI CIRCASIA",
        "CTI QUIMBAYA",
        "CTI MONTENEGRO",
        "PATIO ÚNICO",
        "URI",
        "PALACIO DE JUSTICIA",
        "CAF",
        "CTI ARMENIA",
        "INFANCIA Y ADOLESCENCIA"
    )

    fun saveStat(
        sede: String,
        guarda: String,
        funcionarios: Int,
        contratistas: Int,
        visitantes: Int,
        menoresEdad: Int = 0
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (guarda.isBlank()) {
                    _errorMessage.emit("El nombre del guarda es obligatorio")
                    _isLoading.value = false
                    return@launch
                }

                val stat = DailyStat(
                    fecha = LocalDate.now(),
                    hora = LocalTime.now(),
                    sede = sede,
                    guarda = guarda,
                    funcionarios = funcionarios,
                    contratistas = contratistas,
                    visitantes = visitantes,
                    menoresEdad = menoresEdad
                )

                repository.saveStat(stat)
                _saveSuccess.emit(true)
            } catch (e: Exception) {
                _errorMessage.emit("Error al guardar: ${e.message}")
                _saveSuccess.emit(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkAndArchiveIfNeeded() {
        viewModelScope.launch {
            val lastRecord = todayStats.value.firstOrNull()
            lastRecord?.let {
                if (it.fecha.isBefore(LocalDate.now())) {
                    repository.archiveDailyStats()
                    repository.clearDailyStats()
                }
            }
        }
    }
}