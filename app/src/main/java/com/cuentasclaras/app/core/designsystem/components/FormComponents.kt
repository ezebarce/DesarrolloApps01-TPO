package com.cuentasclaras.app.core.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.cuentasclaras.app.core.designsystem.theme.Shapes

@Composable
fun CampoTexto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage) }
        } else null,
        singleLine = singleLine,
        shape = Shapes.small
    )
}

@Composable
fun CampoMonto(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { 
            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                onValueChange(it)
            }
        },
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        leadingIcon = { Text("$ ") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(errorMessage) }
        } else null,
        singleLine = true,
        shape = Shapes.small
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoSelector(
    opciones: List<String>,
    seleccionado: String,
    onSeleccionChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = seleccionado,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = Shapes.small
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        onSeleccionChange(opcion)
                        expanded = false
                    }
                )
            }
        }
    }
}
