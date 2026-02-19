package com.fiscalia.quindio.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fiscalia.quindio.ui.components.NumberDropdown
import com.fiscalia.quindio.ui.components.SedeDropdown
import com.fiscalia.quindio.ui.theme.FiscaliaBlue
import com.fiscalia.quindio.ui.theme.FiscaliaGray
import com.fiscalia.quindio.ui.theme.FiscaliaRed
import com.fiscalia.quindio.ui.theme.FiscaliaText
import com.fiscalia.quindio.ui.theme.FiscaliaYellow
import com.fiscalia.quindio.util.ExcelExporter
import com.fiscalia.quindio.util.WhatsAppShare
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

    val excelExporter = remember { ExcelExporter(context) }
    val whatsAppShare = remember { WhatsAppShare(context) }

    // Efectos para mensajes
    LaunchedEffect(Unit) {
        viewModel.saveSuccess.collectLatest { success ->
            if (success) {
                Toast.makeText(context, "Estadística guardada exitosamente", Toast.LENGTH_SHORT).show()
                // Limpiar campos excepto sede y guarda
                funcionarios = 0
                contratistas = 0
                visitantes = 0
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
                    IconButton(onClick = {
                        // Mostrar diálogo de ayuda
                    }) {
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
            // Fecha y hora actual
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = FiscaliaBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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

            // Formulario
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

                        // Selector de sede
                        SedeDropdown(
                            sedes = viewModel.sedes,
                            selectedSede = selectedSede,
                            onSedeSelected = { selectedSede = it }
                        )

                        // Nombre del guarda
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

                        // Contadores
                        Text(
                            text = "Cantidad de Personas",
                            style = MaterialTheme.typography.titleMedium,
                            color = FiscaliaText
                        )

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

                        // Total
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = FiscaliaYellow.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
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
                                    (funcionarios + contratistas + visitantes).toString(),
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
                                    visitantes = visitantes
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FiscaliaBlue
                            ),
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

            // Botones de exportación
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val file = excelExporter.exportToExcel(todayStats, LocalDate.now())
                            file?.let {
                                whatsAppShare.shareToMultipleApps(it)
                            } ?: Toast.makeText(context, "Error al exportar", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FiscaliaBlue
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Excel")
                    }

                    OutlinedButton(
                        onClick = {
                            val file = excelExporter.exportToCsv(todayStats, LocalDate.now())
                            file?.let {
                                whatsAppShare.shareToMultipleApps(it)
                            } ?: Toast.makeText(context, "Error al exportar", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FiscaliaBlue
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV")
                    }
                }
            }

            // Resumen del día
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FiscaliaGray
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
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

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                StatSummaryItem("Funcionarios", totalFunc, FiscaliaBlue)
                                StatSummaryItem("Contratistas", totalCont, FiscaliaYellow)
                                StatSummaryItem("Visitantes", totalVis, FiscaliaRed)
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                "Total acumulado: ${totalFunc + totalCont + totalVis}",
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

            // Lista de registros del día
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stat.sede,
                                fontWeight = FontWeight.Bold,
                                color = FiscaliaBlue
                            )
                            Text(
                                "Guarda: ${stat.guarda}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                stat.hora.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = FiscaliaText.copy(alpha = 0.6f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "F: ${stat.funcionarios}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "C: ${stat.contratistas}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "V: ${stat.visitantes}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Divider(modifier = Modifier.padding(vertical = 2.dp))
                            Text(
                                "T: ${stat.funcionarios + stat.contratistas + stat.visitantes}",
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