package com.fiscalia.quindio

import android.app.Application
import com.fiscalia.quindio.worker.DailyResetWorker

class FiscaliaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar WorkManager para reinicio diario
        DailyResetWorker.schedule(this)
    }
}