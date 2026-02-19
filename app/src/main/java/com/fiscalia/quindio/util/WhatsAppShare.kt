package com.fiscalia.quindio.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class WhatsAppShare(private val context: Context) {

    fun shareFileViaWhatsApp(file: File, phoneNumber: String? = null) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = when (file.extension) {
                "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                "csv" -> "text/csv"
                else -> "*/*"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Estadísticas diarias Fiscalía Seccional Quindío")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            // Si se especifica número, abrir chat específico
            phoneNumber?.let {
                val formattedNumber = if (it.startsWith("+")) it else "+57$it"
                setPackage("com.whatsapp")
                putExtra("jid", "$formattedNumber@s.whatsapp.net")
            } ?: run {
                setPackage("com.whatsapp")
            }
        }

        // Verificar si WhatsApp está instalado
        val pm = context.packageManager
        if (intent.resolveActivity(pm) != null) {
            context.startActivity(Intent.createChooser(intent, "Compartir estadísticas"))
        } else {
            // Si no hay WhatsApp, usar compartir genérico
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(genericIntent, "Compartir archivo"))
        }
    }

    fun shareToMultipleApps(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = when (file.extension) {
                "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                "csv" -> "text/csv"
                else -> "*/*"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Estadísticas Fiscalía Quindío")
            putExtra(Intent.EXTRA_TEXT, "Adjunto estadísticas diarias de ingreso")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Compartir estadísticas vía"))
    }
}