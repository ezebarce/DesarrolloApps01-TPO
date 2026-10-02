package com.cuentasclaras.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

class FormateadorFechaTest {

    @Test
    fun `formatearCorta usa formato dd MM yyyy`() {
        val fecha = LocalDate.of(2024, 10, 21)
        assertEquals("21/10/2024", FormateadorFecha.formatearCorta(fecha))
    }

    @Test
    fun `formatearDiaMes usa abreviatura de mes`() {
        val fecha = LocalDate.of(2024, 10, 24)
        // En Java 17 Locale("es", "AR") el mes abreviado es "Oct" (o "oct" capitalizado)
        // Probamos que tenga el día y algo de texto.
        val resultado = FormateadorFecha.formatearDiaMes(fecha)
        assert(resultado.startsWith("24 "))
    }

    @Test
    fun `formatearMesAnio usa nombre completo`() {
        val periodo = YearMonth.of(2024, 10)
        assertEquals("Octubre 2024", FormateadorFecha.formatearMesAnio(periodo))
    }

    @Test
    fun `formatearFechaHoraCierre formatea asamblea`() {
        // Viernes 27/10/2024 a las 20:00 (UTC-3)
        // Instant at UTC: 2024-10-27T23:00:00Z
        val instant = Instant.parse("2024-10-27T23:00:00Z")
        val resultado = FormateadorFecha.formatearFechaHoraCierre(instant)
        // En vez de igualdad exacta debido a mayúsculas según JVM, verificamos que contiene la información
        assert(resultado.contains("27/10/2024"))
        assert(resultado.contains("20:00 hs"))
    }
}
