package com.cuentasclaras.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cuentasclaras.app.core.designsystem.theme.CuentasClarasTheme
import com.cuentasclaras.app.core.designsystem.theme.Typography

enum class BadgeAuditoriaTipo {
    VERIFICADO, NEUTRO, ADVERTENCIA, DESVIO
}

@Composable
fun BadgeAuditoria(
    texto: String,
    tipo: BadgeAuditoriaTipo,
    modifier: Modifier = Modifier
) {
    val (containerColor, contentColor) = when (tipo) {
        BadgeAuditoriaTipo.VERIFICADO -> 
            Pair(CuentasClarasTheme.extendedColors.tertiaryFixed.copy(alpha = 0.15f), CuentasClarasTheme.extendedColors.onTertiaryFixed)
        BadgeAuditoriaTipo.NEUTRO -> 
            Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
        BadgeAuditoriaTipo.ADVERTENCIA -> 
            Pair(CuentasClarasTheme.extendedColors.warningContainer, CuentasClarasTheme.extendedColors.onWarningContainer)
        BadgeAuditoriaTipo.DESVIO -> 
            Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = texto,
            style = Typography.labelSmall,
            color = contentColor
        )
    }
}
