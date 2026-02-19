package com.fiscalia.quindio.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fiscalia.quindio.ui.theme.FiscaliaBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberDropdown(
    label: String,
    selectedNumber: Int,
    onNumberSelected: (Int) -> Unit,
    range: IntRange = 0..500,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.width(120.dp)
    ) {
        OutlinedTextField(
            value = selectedNumber.toString(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = FiscaliaBlue,
                focusedLabelColor = FiscaliaBlue
            ),
            modifier = Modifier.menuAnchor(),
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Mostrar opciones comunes primero, luego scroll para el resto
            val commonValues = listOf(0, 1, 5, 10, 20, 50, 100)
            val displayValues = (commonValues + range.toList()).distinct().sorted()

            displayValues.forEach { number ->
                DropdownMenuItem(
                    text = { Text(number.toString()) },
                    onClick = {
                        onNumberSelected(number)
                        expanded = false
                    }
                )
            }
        }
    }
}