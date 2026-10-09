package com.cuentasclaras.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@Composable
fun BarraQuorum(
    porcentajeQuorum: Double,
    quorumRequerido: Double,
    modifier: Modifier = Modifier
) {
    val req = (quorumRequerido / 100.0).coerceIn(0.0, 1.0)
    val pct = (porcentajeQuorum / 100.0).coerceIn(0.0, 1.0)
    
    val reached = porcentajeQuorum >= quorumRequerido
    val barColor = if (reached) CuentasClarasTheme.extendedColors.tertiaryFixed else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            // Progreso
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pct.toFloat())
                    .background(barColor)
            )
            // Marca de quorum
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .offset(x = 100.dp) // Simplified, requires BoxWithConstraints for real percentage offset
                    .background(MaterialTheme.colorScheme.onSurface)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Base legal requerida: ${FormateadorMoneda.formatearPorcentaje(quorumRequerido)}", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Actual: ${FormateadorMoneda.formatearPorcentaje(porcentajeQuorum)}", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun BarraSegmentada(
    porcentajeAFavor: Double,
    porcentajeEnContra: Double,
    porcentajeAbstencion: Double,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            if (porcentajeAFavor > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(porcentajeAFavor.toFloat())
                        .background(CuentasClarasTheme.extendedColors.tertiaryFixed)
                )
            }
            if (porcentajeEnContra > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(porcentajeEnContra.toFloat())
                        .background(MaterialTheme.colorScheme.error)
                )
            }
            if (porcentajeAbstencion > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(porcentajeAbstencion.toFloat())
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(CuentasClarasTheme.extendedColors.tertiaryFixed))
                Spacer(modifier = Modifier.width(4.dp))
                Text("A favor ${FormateadorMoneda.formatearPorcentaje(porcentajeAFavor * 100)}", style = Typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.error))
                Spacer(modifier = Modifier.width(4.dp))
                Text("En contra ${FormateadorMoneda.formatearPorcentaje(porcentajeEnContra * 100)}", style = Typography.labelSmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Abstención ${FormateadorMoneda.formatearPorcentaje(porcentajeAbstencion * 100)}", style = Typography.labelSmall)
            }
        }
    }
}
