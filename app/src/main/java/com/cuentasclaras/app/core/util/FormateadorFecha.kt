package com.cuentasclaras.app.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object FormateadorFecha {
    private val localeAr = Locale.Builder().setLanguage("es").setRegion("AR").build()
    private val zoneId = ZoneId.of("America/Argentina/Buenos_Aires")

    private val formatoCorta = DateTimeFormatter.ofPattern("dd/MM/yyyy", localeAr)
    private val formatoHora = DateTimeFormatter.ofPattern("HH:mm", localeAr)

    fun formatearCorta(fecha: LocalDate): String {
        return fecha.format(formatoCorta)
    }

    fun formatearDiaMes(fecha: LocalDate): String {
        return "${fecha.dayOfMonth} ${fecha.month.getDisplayName(TextStyle.SHORT, localeAr).replaceFirstChar { it.uppercase() }}"
    }

    fun formatearMesAnio(periodo: YearMonth): String {
        return "${periodo.month.getDisplayName(TextStyle.FULL, localeAr).replaceFirstChar { it.uppercase() }} ${periodo.year}"
    }

    fun formatearVencimiento(fecha: LocalDate): String {
        return "Vence ${fecha.format(formatoCorta)}"
    }

    fun formatearFechaHoraCierre(instant: Instant): String {
        val dateTime = instant.atZone(zoneId)
        val diaSemana = dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, localeAr).replaceFirstChar { it.uppercase() }
        val fechaStr = dateTime.format(formatoCorta)
        val horaStr = dateTime.format(formatoHora)
        return "$diaSemana $fechaStr - $horaStr hs"
    }
}
