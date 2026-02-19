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

    fun exportToExcel(stats: List<DailyStat>, date: LocalDate): File? {
        return try {
            val workbook: Workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Estadísticas ${date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))}")

            // Estilos
            val headerStyle = createHeaderStyle(workbook)
            val dataStyle = createDataStyle(workbook)
            val titleStyle = createTitleStyle(workbook)

            // Título
            val titleRow = sheet.createRow(0)
            val titleCell = titleRow.createCell(0)
            titleCell.setCellValue("FISCALÍA SECCIONAL QUINDÍO - ESTADÍSTICAS DIARIAS")
            titleCell.cellStyle = titleStyle
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6))

            // Fecha
            val dateRow = sheet.createRow(1)
            val dateCell = dateRow.createCell(0)
            dateCell.setCellValue("Fecha: ${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
            dateCell.cellStyle = dataStyle
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 6))

            // Espacio
            sheet.createRow(2)

            // Headers
            val headerRow = sheet.createRow(3)
            val headers = arrayOf("ID", "Hora", "Sede", "Guarda", "Funcionarios", "Contratistas", "Visitantes", "Total")

            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            // Datos
            var totalFuncionarios = 0
            var totalContratistas = 0
            var totalVisitantes = 0

            stats.forEachIndexed { index, stat ->
                val row = sheet.createRow(4 + index)

                row.createCell(0).apply {
                    setCellValue(stat.id.toDouble())
                    cellStyle = dataStyle
                }
                row.createCell(1).apply {
                    setCellValue(stat.hora.toString())
                    cellStyle = dataStyle
                }
                row.createCell(2).apply {
                    setCellValue(stat.sede)
                    cellStyle = dataStyle
                }
                row.createCell(3).apply {
                    setCellValue(stat.guarda)
                    cellStyle = dataStyle
                }
                row.createCell(4).apply {
                    setCellValue(stat.funcionarios.toDouble())
                    cellStyle = dataStyle
                }
                row.createCell(5).apply {
                    setCellValue(stat.contratistas.toDouble())
                    cellStyle = dataStyle
                }
                row.createCell(6).apply {
                    setCellValue(stat.visitantes.toDouble())
                    cellStyle = dataStyle
                }
                row.createCell(7).apply {
                    val total = stat.funcionarios + stat.contratistas + stat.visitantes
                    setCellValue(total.toDouble())
                    cellStyle = dataStyle
                }

                totalFuncionarios += stat.funcionarios
                totalContratistas += stat.contratistas
                totalVisitantes += stat.visitantes
            }

            // Fila de totales
            val totalRow = sheet.createRow(4 + stats.size + 1)
            totalRow.createCell(3).apply {
                setCellValue("TOTALES")
                cellStyle = headerStyle
            }
            totalRow.createCell(4).apply {
                setCellValue(totalFuncionarios.toDouble())
                cellStyle = headerStyle
            }
            totalRow.createCell(5).apply {
                setCellValue(totalContratistas.toDouble())
                cellStyle = headerStyle
            }
            totalRow.createCell(6).apply {
                setCellValue(totalVisitantes.toDouble())
                cellStyle = headerStyle
            }
            totalRow.createCell(7).apply {
                val grandTotal = totalFuncionarios + totalContratistas + totalVisitantes
                setCellValue(grandTotal.toDouble())
                cellStyle = headerStyle
            }

            // Ajustar anchos
            sheet.setColumnWidth(0, 8)
            sheet.setColumnWidth(1, 12)
            sheet.setColumnWidth(2, 25)
            sheet.setColumnWidth(3, 25)
            sheet.setColumnWidth(4, 15)
            sheet.setColumnWidth(5, 15)
            sheet.setColumnWidth(6, 15)
            sheet.setColumnWidth(7, 12)

            // Guardar archivo
            val fileName = "Fiscalia_Stats_${date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.xlsx"
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

    fun exportToCsv(stats: List<DailyStat>, date: LocalDate): File? {
        return try {
            val fileName = "Fiscalia_Stats_${date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.csv"
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                ?: context.filesDir
            val file = File(directory, fileName)

            file.bufferedWriter().use { writer ->
                writer.write("FISCALIA SECCIONAL QUINDIO - ESTADISTICAS DIARIAS\n")
                writer.write("Fecha,${date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}\n\n")
                writer.write("ID,Hora,Sede,Guarda,Funcionarios,Contratistas,Visitantes,Total\n")

                var totalFunc = 0
                var totalCont = 0
                var totalVis = 0

                stats.forEach { stat ->
                    val total = stat.funcionarios + stat.contratistas + stat.visitantes
                    writer.write("${stat.id},${stat.hora},${stat.sede},${stat.guarda},${stat.funcionarios},${stat.contratistas},${stat.visitantes},$total\n")
                    totalFunc += stat.funcionarios
                    totalCont += stat.contratistas
                    totalVis += stat.visitantes
                }

                val grandTotal = totalFunc + totalCont + totalVis
                writer.write(",,,TOTALES,$totalFunc,$totalCont,$totalVis,$grandTotal\n")
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
}