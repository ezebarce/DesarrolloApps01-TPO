package com.cuentasclaras.app.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

object FormateadorMoneda {
    private val localeAr = Locale.Builder().setLanguage("es").setRegion("AR").build()
    private val symbols = DecimalFormatSymbols(localeAr).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }

    private val formatoEstandar = DecimalFormat("$ #,##0.00", symbols)
    private val formatoSinDecimales = DecimalFormat("$ #,##0", symbols)

    fun formatearCentavos(centavos: Long): String {
        val monto = centavos / 100.0
        return formatoEstandar.format(monto)
    }

    fun formatearCentavosSinDecimales(centavos: Long): String {
        val monto = centavos / 100.0
        return formatoSinDecimales.format(monto)
    }

    fun formatearCompacto(centavos: Long): String {
        val monto = centavos / 100.0
        val absoluto = abs(monto)
        return when {
            absoluto >= 1_000_000 -> {
                val millones = monto / 1_000_000.0
                val format = DecimalFormat("$ #,##0.##M", symbols)
                format.format(millones)
            }
            absoluto >= 1_000 -> {
                val miles = monto / 1_000.0
                val format = DecimalFormat("$ #,##0.##k", symbols)
                format.format(miles)
            }
            else -> formatoSinDecimales.format(monto)
        }
    }

    /**
     * Recibe el valor en puntos porcentuales: 68.4 -> "68,40 %".
     * Si se tiene una fracción (0.684), multiplicarla por 100 antes de llamar.
     * (El patrón "%" de DecimalFormat multiplicaba por 100 y por eso no se usa).
     */
    fun formatearPorcentaje(valor: Double): String {
        val formatoPorcentaje = DecimalFormat("#,##0.00", symbols)
        return "${formatoPorcentaje.format(valor)} %"
    }
}
