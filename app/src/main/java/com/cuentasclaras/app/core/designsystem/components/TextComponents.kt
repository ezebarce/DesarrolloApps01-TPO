package com.cuentasclaras.app.core.designsystem.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.cuentasclaras.app.core.designsystem.theme.Typography
import com.cuentasclaras.app.core.designsystem.theme.monetaryHero
import com.cuentasclaras.app.core.util.FormateadorMoneda

@Composable
fun MontoHeroText(
    montoCentavos: Long,
    modifier: Modifier = Modifier
) {
    val montoFormat = FormateadorMoneda.formatearCentavos(montoCentavos)
    
    // Asumiendo formato "$ 1.234,56"
    val parts = montoFormat.split(",")
    val mainPart = parts[0]
    val decimalPart = if (parts.size > 1) ",${parts[1]}" else ""

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(fontSize = Typography.headlineMedium.fontSize)) {
            append("$ ")
        }
        append(mainPart.removePrefix("$ "))
        withStyle(style = SpanStyle(fontSize = Typography.headlineSmall.fontSize)) {
            append(decimalPart)
        }
    }

    Text(
        text = annotatedString,
        style = monetaryHero,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}
