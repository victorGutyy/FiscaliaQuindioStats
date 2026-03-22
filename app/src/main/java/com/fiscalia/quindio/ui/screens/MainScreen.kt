package com.fiscalia.quindio.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiscalia.quindio.ui.components.NumberDropdown
import com.fiscalia.quindio.ui.components.SedeDropdown
import com.fiscalia.quindio.ui.theme.FiscaliaBlue
import com.fiscalia.quindio.ui.theme.FiscaliaGray
import com.fiscalia.quindio.ui.theme.FiscaliaRed
import com.fiscalia.quindio.ui.theme.FiscaliaText
import com.fiscalia.quindio.ui.theme.FiscaliaYellow
import com.fiscalia.quindio.util.CardImageGenerator
import com.fiscalia.quindio.util.ExcelExporter
import com.fiscalia.quindio.util.GoogleDriveUploader
import com.fiscalia.quindio.viewmodel.StatViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: StatViewModel = viewModel()) {
    val context = LocalContext.current
    val todayStats by viewModel.todayStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedSede by remember { mutableStateOf("") }
    var guardaName by remember { mutableStateOf("") }
    var funcionarios by remember { mutableIntStateOf(0) }
    var contratistas by remember { mutableIntStateOf(0) }
    var visitantes by remember { mutableIntStateOf(0) }
    var menoresEdad by remember { mutableIntStateOf(0) }

    val excelExporter = remember { ExcelExporter(context) }
    val googleDriveUploader = remember { GoogleDriveUploader(context) }
    val cardImageGenerator = remember { CardImageGenerator(context) }

    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collectLatest { success ->
            if (success) {
                Toast.makeText(context, "Estadística guardada exitosamente", Toast.LENGTH_SHORT).show()
                funcionarios = 0
                contratistas = 0
                visitantes = 0
                menoresEdad = 0
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "FISCALÍA SECCIONAL QUINDÍO",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            "Registro de Estadísticas",
                            style = MaterialTheme.typography.bodySmall,
                            color = FiscaliaYellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FiscaliaBlue
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ayuda",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Tarjeta de fecha/hora ──────────────────────────────────────
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FiscaliaBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = LocalDate.now().format(
                                    DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy")
                                ).replaceFirstChar { it.uppercase() },
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = java.time.LocalTime.now().format(
                                    DateTimeFormatter.ofPattern("HH:mm:ss")
                                ),
                                color = FiscaliaYellow,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = FiscaliaYellow,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // ── Formulario de nuevo registro ───────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Nuevo Registro",
                            style = MaterialTheme.typography.headlineSmall,
                            color = FiscaliaBlue,
                            fontWeight = FontWeight.Bold
                        )

                        Divider(color = FiscaliaYellow, thickness = 2.dp)

                        SedeDropdown(
                            sedes = viewModel.sedes,
                            selectedSede = selectedSede,
                            onSedeSelected = { selectedSede = it }
                        )

                        OutlinedTextField(
                            value = guardaName,
                            onValueChange = { guardaName = it },
                            label = { Text("Nombre del Guarda") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FiscaliaBlue,
                                focusedLabelColor = FiscaliaBlue
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text(
                            text = "Cantidad de Personas",
                            style = MaterialTheme.typography.titleMedium,
                            color = FiscaliaText
                        )

                        // Fila 1: Funcionarios, Contratistas, Visitantes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            NumberDropdown(
                                label = "Func.",
                                selectedNumber = funcionarios,
                                onNumberSelected = { funcionarios = it }
                            )
                            NumberDropdown(
                                label = "Contr.",
                                selectedNumber = contratistas,
                                onNumberSelected = { contratistas = it }
                            )
                            NumberDropdown(
                                label = "Visit.",
                                selectedNumber = visitantes,
                                onNumberSelected = { visitantes = it }
                            )
                        }

                        // Fila 2: Menores de Edad (separada para darle más espacio y visibilidad)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Menores de Edad:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF7B1FA2),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            NumberDropdown(
                                label = "Menores",
                                selectedNumber = menoresEdad,
                                onNumberSelected = { menoresEdad = it }
                            )
                        }

                        // Total
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = FiscaliaYellow.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "TOTAL INGRESOS:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FiscaliaBlue
                                )
                                Text(
                                    (funcionarios + contratistas + visitantes + menoresEdad).toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FiscaliaRed
                                )
                            }
                        }

                        // Botón guardar
                        Button(
                            onClick = {
                                if (selectedSede.isEmpty()) {
                                    Toast.makeText(context, "Seleccione una sede", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.saveStat(
                                    sede = selectedSede,
                                    guarda = guardaName,
                                    funcionarios = funcionarios,
                                    contratistas = contratistas,
                                    visitantes = visitantes,
                                    menoresEdad = menoresEdad
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FiscaliaBlue),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White)
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GUARDAR REGISTRO", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // ── Botón Drive ────────────────────────────────────────────────
            item {
                Button(
                    onClick = {
                        if (selectedSede.isEmpty()) {
                            Toast.makeText(context, "Seleccione una sede primero", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val file = excelExporter.exportToExcel(todayStats, LocalDate.now(), selectedSede)
                        file?.let {
                            googleDriveUploader.shareToDriveFolder(it, selectedSede)
                        } ?: Toast.makeText(context, "Error al exportar", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FiscaliaYellow,
                        contentColor = FiscaliaBlue
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ENVIAR ESTADÍSTICA A DRIVE", fontWeight = FontWeight.Bold)
                }
            }

            // ── Botones Excel / CSV / WhatsApp Tarjeta ─────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    // Fila Excel + CSV
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val file = excelExporter.exportToExcel(todayStats, LocalDate.now(), selectedSede)
                                file?.let {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                        putExtra(
                                            Intent.EXTRA_STREAM,
                                            FileProvider.getUriForFile(
                                                context, "${context.packageName}.fileprovider", it
                                            )
                                        )
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir Excel"))
                                } ?: Toast.makeText(context, "Error al exportar", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FiscaliaBlue)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel")
                        }

                        OutlinedButton(
                            onClick = {
                                val file = excelExporter.exportToCsv(todayStats, LocalDate.now(), selectedSede)
                                file?.let {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(
                                            Intent.EXTRA_STREAM,
                                            FileProvider.getUriForFile(
                                                context, "${context.packageName}.fileprovider", it
                                            )
                                        )
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir CSV"))
                                } ?: Toast.makeText(context, "Error al exportar", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = FiscaliaBlue)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV")
                        }
                    }

                    // ✅ NUEVO: Botón Tarjeta WhatsApp (imagen)
                    Button(
                        onClick = {
                            if (selectedSede.isEmpty()) {
                                Toast.makeText(context, "Seleccione una sede primero", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (todayStats.isEmpty()) {
                                Toast.makeText(context, "No hay registros del día para compartir", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val uri = cardImageGenerator.generarTarjeta(
                                stats = todayStats,
                                date = LocalDate.now(),
                                sede = selectedSede
                            )
                            if (uri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "📊 Estadísticas del día - Fiscalía Seccional Quindío\n📍 Sede: $selectedSede"
                                    )
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    setPackage("com.whatsapp")
                                }
                                val pm = context.packageManager
                                if (shareIntent.resolveActivity(pm) != null) {
                                    context.startActivity(shareIntent)
                                } else {
                                    // WhatsApp no instalado → compartir genérico
                                    shareIntent.setPackage(null)
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "Compartir tarjeta")
                                    )
                                }
                            } else {
                                Toast.makeText(context, "Error al generar la tarjeta", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366), // Verde WhatsApp
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📲", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "COMPARTIR TARJETA POR WHATSAPP",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── Resumen del día ────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FiscaliaGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Resumen del Día",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = FiscaliaBlue
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (todayStats.isEmpty()) {
                            Text(
                                "No hay registros hoy",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FiscaliaText.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            val totalFunc = todayStats.sumOf { it.funcionarios }
                            val totalCont = todayStats.sumOf { it.contratistas }
                            val totalVis = todayStats.sumOf { it.visitantes }
                            val totalMenores = todayStats.sumOf { it.menoresEdad }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                StatSummaryItem("Func.", totalFunc, FiscaliaBlue)
                                StatSummaryItem("Contr.", totalCont, FiscaliaYellow)
                                StatSummaryItem("Visit.", totalVis, FiscaliaRed)
                                StatSummaryItem("Menores", totalMenores, Color(0xFF9C27B0))
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                "Total acumulado: ${totalFunc + totalCont + totalVis + totalMenores}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FiscaliaBlue,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ── Lista de registros ─────────────────────────────────────────
            item {
                Text(
                    "Registros de Hoy (${todayStats.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FiscaliaBlue
                )
            }

            items(todayStats) { stat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stat.sede, fontWeight = FontWeight.Bold, color = FiscaliaBlue)
                            Text("Guarda: ${stat.guarda}", style = MaterialTheme.typography.bodySmall)
                            Text(
                                stat.hora.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = FiscaliaText.copy(alpha = 0.6f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("F: ${stat.funcionarios}", style = MaterialTheme.typography.bodyMedium)
                            Text("C: ${stat.contratistas}", style = MaterialTheme.typography.bodyMedium)
                            Text("V: ${stat.visitantes}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "M: ${stat.menoresEdad}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF9C27B0)
                            )
                            Divider(modifier = Modifier.padding(vertical = 2.dp))
                            Text(
                                "T: ${stat.funcionarios + stat.contratistas + stat.visitantes + stat.menoresEdad}",
                                fontWeight = FontWeight.Bold,
                                color = FiscaliaRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatSummaryItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = FiscaliaText.copy(alpha = 0.7f)
        )
    }
}
