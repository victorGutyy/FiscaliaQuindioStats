package com.fiscalia.quindio.util

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.fiscalia.quindio.data.entity.DailyStat

class CardImageGenerator(private val context: Context) {

    // Colores institucionales Fiscalía
    private val colorAzul = Color.parseColor("#0D1B4E")       // Azul oscuro
    private val colorAzulMedio = Color.parseColor("#1A2F7A")   // Azul medio
    private val colorAmarillo = Color.parseColor("#F5C518")    // Amarillo
    private val colorBlanco = Color.WHITE
    private val colorGrisClaro = Color.parseColor("#F2F4F8")
    private val colorRojo = Color.parseColor("#C62828")
    private val colorMorado = Color.parseColor("#7B1FA2")
    private val colorVerde = Color.parseColor("#2E7D32")

    /**
     * Genera una imagen PNG con el resumen del día y la devuelve como Uri
     * lista para compartir por WhatsApp.
     */
    fun generarTarjeta(
        stats: List<DailyStat>,
        date: LocalDate,
        sede: String
    ): Uri? {
        return try {
            val width = 1080
            val height = 1400

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            dibujarFondo(canvas, width, height)
            dibujarEncabezado(canvas, width)
            dibujarFechaYSede(canvas, width, date, sede)
            dibujarEstadisticas(canvas, width, stats)
            dibujarPie(canvas, width, height)

            // Guardar archivo
            val fileName = "Fiscalia_Card_${date.format(DateTimeFormatter.ofPattern("yyyyMMdd"))}_${sede.replace(" ", "_")}.png"
            val directory = context.filesDir
            val file = File(directory, fileName)

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun dibujarFondo(canvas: Canvas, width: Int, height: Int) {
        // Fondo azul oscuro
        val paint = Paint().apply { color = colorAzul; isAntiAlias = true }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Banda amarilla decorativa superior
        val paintAmarillo = Paint().apply { color = colorAmarillo; isAntiAlias = true }
        canvas.drawRect(0f, 0f, width.toFloat(), 12f, paintAmarillo)

        // Banda amarilla inferior
        canvas.drawRect(0f, height - 12f, width.toFloat(), height.toFloat(), paintAmarillo)

        // Fondo blanco del cuerpo de contenido
        val paintBlanco = Paint().apply { color = colorGrisClaro; isAntiAlias = true }
        val rectF = RectF(40f, 260f, width - 40f, height - 120f)
        canvas.drawRoundRect(rectF, 24f, 24f, paintBlanco)
    }

    private fun dibujarEncabezado(canvas: Canvas, width: Int) {
        // Escudo / logo placeholder (rectángulo amarillo con texto)
        val paintEscudo = Paint().apply { color = colorAmarillo; isAntiAlias = true }
        canvas.drawRoundRect(RectF(width / 2f - 50f, 30f, width / 2f + 50f, 130f), 12f, 12f, paintEscudo)

        val paintTextoEscudo = Paint().apply {
            color = colorAzul
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("FISCALÍA", width / 2f, 80f, paintTextoEscudo)
        canvas.drawText("GENERAL", width / 2f, 100f, paintTextoEscudo)

        // Título principal
        val paintTitulo = Paint().apply {
            color = colorBlanco
            textSize = 38f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("FISCALÍA SECCIONAL QUINDÍO", width / 2f, 170f, paintTitulo)

        val paintSubtitulo = Paint().apply {
            color = colorAmarillo
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Registro de Estadísticas Diarias", width / 2f, 215f, paintSubtitulo)
    }

    private fun dibujarFechaYSede(canvas: Canvas, width: Int, date: LocalDate, sede: String) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy")
        val fechaStr = date.format(formatter).replaceFirstChar { it.uppercase() }

        // Banda de fecha (sobre el card blanco)
        val paintBanda = Paint().apply { color = colorAzulMedio; isAntiAlias = true }
        canvas.drawRoundRect(RectF(40f, 260f, width - 40f, 340f), 24f, 24f, paintBanda)

        val paintFecha = Paint().apply {
            color = colorBlanco
            textSize = 24f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(fechaStr, width / 2f, 298f, paintFecha)

        val paintSede = Paint().apply {
            color = colorAmarillo
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("📍 $sede", width / 2f, 330f, paintSede)
    }

    private fun dibujarEstadisticas(canvas: Canvas, width: Int, stats: List<DailyStat>) {
        val totalFunc = stats.sumOf { it.funcionarios }
        val totalCont = stats.sumOf { it.contratistas }
        val totalVis = stats.sumOf { it.visitantes }
        val totalMenores = stats.sumOf { it.menoresEdad }
        val grandTotal = totalFunc + totalCont + totalVis + totalMenores

        var y = 390f
        val paddingX = 80f
        val cardWidth = (width - paddingX * 2).toFloat()

        // Título de sección
        val paintSeccion = Paint().apply {
            color = colorAzul
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("RESUMEN DEL DÍA", width / 2f, y, paintSeccion)
        y += 10f

        // Línea separadora amarilla
        val paintLinea = Paint().apply { color = colorAmarillo; strokeWidth = 4f; isAntiAlias = true }
        canvas.drawLine(paddingX + 40f, y + 10f, width - paddingX - 40f, y + 10f, paintLinea)
        y += 50f

        // Tarjetas de cada categoría
        dibujarFilaEstadistica(canvas, paddingX, y, cardWidth, "👔 Funcionarios", totalFunc, colorAzulMedio)
        y += 140f
        dibujarFilaEstadistica(canvas, paddingX, y, cardWidth, "🔧 Contratistas", totalCont, Color.parseColor("#1565C0"))
        y += 140f
        dibujarFilaEstadistica(canvas, paddingX, y, cardWidth, "🧑 Visitantes", totalVis, colorVerde)
        y += 140f
        dibujarFilaEstadistica(canvas, paddingX, y, cardWidth, "👶 Menores de Edad", totalMenores, colorMorado)
        y += 160f

        // TOTAL GENERAL
        val paintTotalBg = Paint().apply { color = colorAzul; isAntiAlias = true }
        val totalRect = RectF(paddingX, y, width - paddingX, y + 120f)
        canvas.drawRoundRect(totalRect, 18f, 18f, paintTotalBg)

        // Borde amarillo
        val paintBorde = Paint().apply {
            color = colorAmarillo
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawRoundRect(totalRect, 18f, 18f, paintBorde)

        val paintTotalLabel = Paint().apply {
            color = colorAmarillo
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }
        canvas.drawText("TOTAL INGRESOS DEL DÍA", paddingX + 30f, y + 48f, paintTotalLabel)

        val paintTotalValor = Paint().apply {
            color = colorBlanco
            textSize = 56f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(grandTotal.toString(), width - paddingX - 30f, y + 90f, paintTotalValor)

        // Número de registros
        val paintRegistros = Paint().apply {
            color = colorAmarillo
            textSize = 20f
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }
        canvas.drawText("${stats.size} registro(s) guardado(s)", paddingX + 30f, y + 85f, paintRegistros)
    }

    private fun dibujarFilaEstadistica(
        canvas: Canvas,
        x: Float, y: Float,
        width: Float,
        label: String,
        value: Int,
        color: Int
    ) {
        // Fondo de la tarjeta
        val paintCard = Paint().apply { this.color = color; isAntiAlias = true }
        val rect = RectF(x, y, x + width, y + 115f)
        canvas.drawRoundRect(rect, 14f, 14f, paintCard)

        // Franja izquierda más oscura
        val paintFranja = Paint().apply {
            this.color = ajustarBrillo(color, 0.7f)
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(x, y, x + 14f, y + 115f), 14f, 14f, paintFranja)
        canvas.drawRect(x + 7f, y, x + 14f, y + 115f, paintFranja)

        // Texto label
        val paintLabel = Paint().apply {
            this.color = Color.WHITE
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }
        canvas.drawText(label, x + 30f, y + 46f, paintLabel)

        // Descripción
        val paintDesc = Paint().apply {
            this.color = Color.parseColor("#CCFFFFFF")
            textSize = 20f
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }
        canvas.drawText("personas registradas hoy", x + 30f, y + 78f, paintDesc)

        // Valor grande a la derecha
        val paintValor = Paint().apply {
            this.color = colorAmarillo
            textSize = 60f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(value.toString(), x + width - 24f, y + 82f, paintValor)
    }

    private fun dibujarPie(canvas: Canvas, width: Int, height: Int) {
        val paintTexto = Paint().apply {
            color = Color.parseColor("#AAFFFFFF")
            textSize = 22f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            "Generado por App Estadísticas — Fiscalía Seccional Quindío",
            width / 2f, height - 55f, paintTexto
        )
        canvas.drawText(
            "Sistema de Control de Acceso · Guardas de Seguridad",
            width / 2f, height - 28f, paintTexto
        )
    }

    private fun ajustarBrillo(color: Int, factor: Float): Int {
        val r = (Color.red(color) * factor).toInt().coerceIn(0, 255)
        val g = (Color.green(color) * factor).toInt().coerceIn(0, 255)
        val b = (Color.blue(color) * factor).toInt().coerceIn(0, 255)
        return Color.rgb(r, g, b)
    }
}
