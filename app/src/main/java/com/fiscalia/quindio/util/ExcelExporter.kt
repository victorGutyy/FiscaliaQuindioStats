package com.fiscalia.quindio.util

import android.content.Context
import android.os.Environment
import com.fiscalia.quindio.data.entity.DailyStat
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExcelExporter(private val context: Context) {

    fun exportToExcel(stats: List<DailyStat>, date: LocalDate, sede: String = ""): File? {
        return try {
            val workbook: Workbook = XSSFWorkbook()
            val sheetName = if (sede.isNotEmpty()) "Estadisticas_$sede" else "Estadisticas"
            val sheet = workbook.createSheet(sheetName)

            // Estilos
            val headerStyle = createHeaderStyle(workbook)
            val dataStyle = createDataStyle(workbook)
            val titleStyle = createTitleStyle(workbook)
            val totalStyle = createTotalStyle(workbook)

            // Título
            val titleRow = sheet.createRow(0)
            val titleCell = titleRow.createCell(0)
            titleCell.setCellValue("FISCALIA SECCIONAL QUINDIO - ESTADISTICAS DIARIAS")
            titleCell.cellStyle = titleStyle
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 8))

            // Fecha
            val dateRow = sheet.createRow(1)
            val dateCell = dateRow.createCell(0)
            dateCell.setCellValue("Fecha: ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
            dateCell.cellStyle = dataStyle
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 8))

            // Sede
            if (sede.isNotEmpty()) {
                val sedeRow = sheet.createRow(2)
                val sedeCell = sedeRow.createCell(0)
                sedeCell.setCellValue("Sede: $sede")
                sedeCell.cellStyle = dataStyle
                sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(2, 2, 0, 8))
            }

            // Espacio
            val headerRowIndex = if (sede.isNotEmpty()) 4 else 3
            sheet.createRow(headerRowIndex - 1)

            // Headers
            val headerRow = sheet.createRow(headerRowIndex)
            val headers = arrayOf(
                "ID", "Hora", "Sede", "Guarda",
                "Funcionarios", "Contratistas", "Visitantes", "Menores_Edad", "Total"
            )
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            // Datos
            var totalFuncionarios = 0
            var totalContratistas = 0
            var totalVisitantes = 0
            var totalMenoresEdad = 0

            stats.forEachIndexed { index, stat ->
                val row = sheet.createRow(headerRowIndex + 1 + index)
                val total = stat.funcionarios + stat.contratistas + stat.visitantes + stat.menoresEdad

                row.createCell(0).apply { setCellValue(stat.id.toDouble()); cellStyle = dataStyle }
                row.createCell(1).apply { setCellValue(stat.hora.toString()); cellStyle = dataStyle }
                row.createCell(2).apply { setCellValue(stat.sede); cellStyle = dataStyle }
                row.createCell(3).apply { setCellValue(stat.guarda); cellStyle = dataStyle }
                row.createCell(4).apply { setCellValue(stat.funcionarios.toDouble()); cellStyle = dataStyle }
                row.createCell(5).apply { setCellValue(stat.contratistas.toDouble()); cellStyle = dataStyle }
                row.createCell(6).apply { setCellValue(stat.visitantes.toDouble()); cellStyle = dataStyle }
                row.createCell(7).apply { setCellValue(stat.menoresEdad.toDouble()); cellStyle = dataStyle }
                row.createCell(8).apply { setCellValue(total.toDouble()); cellStyle = dataStyle }

                totalFuncionarios += stat.funcionarios
                totalContratistas += stat.contratistas
                totalVisitantes += stat.visitantes
                totalMenoresEdad += stat.menoresEdad
            }

            // Fila de totales
            val grandTotal = totalFuncionarios + totalContratistas + totalVisitantes + totalMenoresEdad
            val totalRow = sheet.createRow(headerRowIndex + 1 + stats.size + 1)
            totalRow.createCell(0).apply { setCellValue(""); cellStyle = totalStyle }
            totalRow.createCell(1).apply { setCellValue(""); cellStyle = totalStyle }
            totalRow.createCell(2).apply { setCellValue(""); cellStyle = totalStyle }
            totalRow.createCell(3).apply { setCellValue("TOTALES"); cellStyle = totalStyle }
            totalRow.createCell(4).apply { setCellValue(totalFuncionarios.toDouble()); cellStyle = totalStyle }
            totalRow.createCell(5).apply { setCellValue(totalContratistas.toDouble()); cellStyle = totalStyle }
            totalRow.createCell(6).apply { setCellValue(totalVisitantes.toDouble()); cellStyle = totalStyle }
            totalRow.createCell(7).apply { setCellValue(totalMenoresEdad.toDouble()); cellStyle = totalStyle }
            totalRow.createCell(8).apply { setCellValue(grandTotal.toDouble()); cellStyle = totalStyle }

            // ✅ FIX: setColumnWidth usa unidades de 1/256 de carácter
            // Para ver el contenido correctamente: caracteres_deseados × 256
            sheet.setColumnWidth(0, 6 * 256)   // ID → 6 chars
            sheet.setColumnWidth(1, 12 * 256)  // Hora → 12 chars
            sheet.setColumnWidth(2, 25 * 256)  // Sede → 25 chars
            sheet.setColumnWidth(3, 25 * 256)  // Guarda → 25 chars
            sheet.setColumnWidth(4, 15 * 256)  // Funcionarios
            sheet.setColumnWidth(5, 15 * 256)  // Contratistas
            sheet.setColumnWidth(6, 15 * 256)  // Visitantes
            sheet.setColumnWidth(7, 15 * 256)  // Menores de Edad
            sheet.setColumnWidth(8, 12 * 256)  // Total

            // Guardar archivo
            val sedeSuffix = if (sede.isNotEmpty()) "_${sede.replace(" ", "_")}" else ""
            val fileName = "Fiscalia_Stats${sedeSuffix}_${date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.xlsx"
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            val file = File(directory, fileName)

            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportToCsv(stats: List<DailyStat>, date: LocalDate, sede: String = ""): File? {
        return try {
            val sedeSuffix = if (sede.isNotEmpty()) "_${sede.replace(" ", "_")}" else ""
            val fileName = "Fiscalia_Stats${sedeSuffix}_${date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.csv"
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            val file = File(directory, fileName)

            file.bufferedWriter().use { writer ->
                writer.write("FISCALIA SECCIONAL QUINDIO - ESTADISTICAS DIARIAS\n")
                if (sede.isNotEmpty()) writer.write("Sede,$sede\n")
                writer.write("Fecha,${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}\n\n")
                writer.write("ID,Hora,Sede,Guarda,Funcionarios,Contratistas,Visitantes,Menores_Edad,Total\n")

                var totalFunc = 0; var totalCont = 0; var totalVis = 0; var totalMenores = 0

                stats.forEach { stat ->
                    val total = stat.funcionarios + stat.contratistas + stat.visitantes + stat.menoresEdad
                    writer.write("${stat.id},${stat.hora},${stat.sede},${stat.guarda},${stat.funcionarios},${stat.contratistas},${stat.visitantes},${stat.menoresEdad},$total\n")
                    totalFunc += stat.funcionarios; totalCont += stat.contratistas
                    totalVis += stat.visitantes; totalMenores += stat.menoresEdad
                }

                val grandTotal = totalFunc + totalCont + totalVis + totalMenores
                writer.write(",,,TOTALES,$totalFunc,$totalCont,$totalVis,$totalMenores,$grandTotal\n")
            }

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.DARK_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply {
                color = IndexedColors.WHITE.index
                bold = true
            })
            alignment = HorizontalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }
    }

    private fun createDataStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
            alignment = HorizontalAlignment.CENTER
        }
    }

    private fun createTitleStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            setFont(workbook.createFont().apply {
                bold = true
                fontHeightInPoints = 14
                color = IndexedColors.DARK_BLUE.index
            })
            alignment = HorizontalAlignment.CENTER
        }
    }

    // ✅ NUEVO: estilo para fila de totales (fondo amarillo, texto azul oscuro)
    private fun createTotalStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GOLD.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply {
                bold = true
                color = IndexedColors.DARK_BLUE.index
            })
            alignment = HorizontalAlignment.CENTER
            borderBottom = BorderStyle.MEDIUM
            borderTop = BorderStyle.MEDIUM
            borderLeft = BorderStyle.MEDIUM
            borderRight = BorderStyle.MEDIUM
        }
    }
}
