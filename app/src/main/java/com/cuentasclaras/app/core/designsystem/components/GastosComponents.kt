package com.cuentasclaras.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cuentasclaras.app.core.designsystem.theme.CuentasClarasTheme
import com.cuentasclaras.app.core.designsystem.theme.Typography
import com.cuentasclaras.app.core.util.FormateadorMoneda
import com.cuentasclaras.app.domain.model.EstadoGasto

@Composable
fun LineaTiempoTrazabilidad(
    estadoActual: EstadoGasto,
    modifier: Modifier = Modifier
) {
    val estados = EstadoGasto.values()
    val currentIndex = estados.indexOf(estadoActual)

    Column(modifier = modifier.fillMaxWidth()) {
        estados.forEachIndexed { index, estado ->
            val isCompleted = index <= currentIndex
            val isLast = index == estados.size - 1

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(if (isCompleted) CuentasClarasTheme.extendedColors.tertiaryFixed else MaterialTheme.colorScheme.surfaceVariant)
                    )
                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(24.dp)
                                .background(if (index < currentIndex) CuentasClarasTheme.extendedColors.tertiaryFixed else MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = estado.name.replace("_", " "),
                    style = Typography.bodyMedium,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 0.dp)
                )
            }
        }
    }
}

@Composable
fun BarraRangoMercado(
    min: Long,
    max: Long,
    media: Long,
    actual: Long?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(FormateadorMoneda.formatearCompacto(min), style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Media: ${FormateadorMoneda.formatearCompacto(media)}", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(FormateadorMoneda.formatearCompacto(max), style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            // Segmento de mercado (del 10% al 90% para ilustrar la zona)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.8f) // placeholder
                    .align(Alignment.Center)
                    .background(CuentasClarasTheme.extendedColors.tertiaryFixed.copy(alpha = 0.3f))
            )
            // Marca de la media
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .align(Alignment.Center)
                    .background(CuentasClarasTheme.extendedColors.tertiaryFixed)
            )
            // Marca del gasto actual si existe
            if (actual != null) {
                // Cálculo simple de posición (0f a 1f) truncado
                val range = (max - min).coerceAtLeast(1)
                val position = ((actual - min).toFloat() / range).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .offset(x = position.dp) // Simplified offset, should use BoxWithConstraints for real pixels
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }
    }
}
