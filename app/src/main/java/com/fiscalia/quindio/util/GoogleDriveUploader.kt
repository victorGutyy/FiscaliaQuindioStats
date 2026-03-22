package com.fiscalia.quindio.util

import android.content.Context
import android.content.Intent
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import java.io.IOException

class GoogleDriveUploader(private val context: Context) {

    companion object {
        // ID de la carpeta compartida de CACYM (debes obtenerlo de la URL del Drive)
        const val CACYM_FOLDER_ID = "TU_FOLDER_ID_AQUI"
    }

    private fun getDriveService(accountName: String): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        ).setSelectedAccountName(accountName)

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory(),
            credential
        ).setApplicationName("Fiscalia Quindio Stats").build()
    }

    fun uploadToDrive(
        file: java.io.File,
        sede: String,
        accountName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val driveService = getDriveService(accountName)

            // Nombre del archivo con identificación de sede y fecha
            val fileName = "ESTADISTICA_${sede.replace(" ", "_")}_${System.currentTimeMillis()}.xlsx"

            val fileMetadata = File().apply {
                name = fileName
                parents = listOf(CACYM_FOLDER_ID)
                description = "Estadística de ingresos - Sede: $sede - Fecha: ${java.time.LocalDate.now()}"
            }

            val mediaContent = FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", file)

            // Subir en background thread
            Thread {
                try {
                    driveService.files().create(fileMetadata, mediaContent)
                        .setFields("id, webViewLink")
                        .execute()

                    onSuccess()
                } catch (e: IOException) {
                    onError("Error al subir: ${e.message}")
                }
            }.start()

        } catch (e: Exception) {
            onError("Error de configuración: ${e.message}")
        }
    }

    // Método alternativo: Abrir Drive para compartir manualmente
    fun shareToDriveFolder(file: java.io.File, sede: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_SUBJECT, "Estadística $sede - ${java.time.LocalDate.now()}")
            putExtra(Intent.EXTRA_TEXT, "Estadística diaria de ingresos\nSede: $sede\nFecha: ${java.time.LocalDate.now()}")
            putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            ))
            // Intent específico para Drive
            `package` = "com.google.android.apps.docs"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            // Si no hay Drive, usar selector genérico
            val genericIntent = Intent.createChooser(shareIntent, "Enviar estadística a Drive")
            context.startActivity(genericIntent)
        }
    }
}