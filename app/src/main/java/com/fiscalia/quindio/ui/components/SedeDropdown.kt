package com.fiscalia.quindio.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.fiscalia.quindio.ui.theme.FiscaliaBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SedeDropdown(
    sedes: List<String>,
    selectedSede: String,
    onSedeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedSede,
            onValueChange = {},
            readOnly = true,
            label = { Text("Seleccionar Sede") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FiscaliaBlue,
                focusedLabelColor = FiscaliaBlue
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sedes.forEach { sede ->
                DropdownMenuItem(
                    text = { Text(sede) },
                    onClick = {
                        onSedeSelected(sede)
                        expanded = false
                    }
                )
            }
        }
    }
}