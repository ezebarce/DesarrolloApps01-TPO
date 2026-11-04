package com.cuentasclaras.app.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FormateadorMonedaTest {

    @Test
    fun `formatearCentavos usa coma para decimales y punto para miles`() {
        assertEquals("$ 1.234.567,89", FormateadorMoneda.formatearCentavos(123456789))
        assertEquals("$ 150,00", FormateadorMoneda.formatearCentavos(15000))
    }

    @Test
    fun `formatearCentavosSinDecimales ignora decimales`() {
        assertEquals("$ 1.234.568", FormateadorMoneda.formatearCentavosSinDecimales(123456789))
    }

    @Test
    fun `formatearCompacto abrevia millones`() {
        assertEquals("$ 4,82M", FormateadorMoneda.formatearCompacto(482000000))
        assertEquals("$ 1M", FormateadorMoneda.formatearCompacto(100000000))
    }

    @Test
    fun `formatearCompacto abrevia miles`() {
        assertEquals("$ 350k", FormateadorMoneda.formatearCompacto(35000000))
        assertEquals("$ 42,25k", FormateadorMoneda.formatearCompacto(4225000))
    }

    @Test
    fun `formatearPorcentaje usa coma`() {
        assertEquals("68,40 %", FormateadorMoneda.formatearPorcentaje(68.4))
        assertEquals("2,45 %", FormateadorMoneda.formatearPorcentaje(2.45))
    }

    // Convención: formatearPorcentaje recibe puntos porcentuales (0-100).
    // Las pruebas replican el cálculo que hace cada pantalla antes de formatear.

    @Test
    fun `Gastos - desvio contra referencia de mercado (fraccion por 100)`() {
        // Luminarias del sembrador: monto 215.400 vs referencia 182.500
        val diff = (21540000L - 18250000L).toDouble() / 18250000L
        assertEquals("18,03 %", FormateadorMoneda.formatearPorcentaje(diff * 100))
    }

    @Test
    fun `Gastos - consumo del tope mensual (fraccion por 100)`() {
        val porcentaje = 2_865_850_00L.toDouble() / 3_370_000_00L.toDouble()
        assertEquals("85,04 %", FormateadorMoneda.formatearPorcentaje(porcentaje * 100))
    }

    @Test
    fun `Votaciones - quorum y coeficientes en puntos porcentuales`() {
        // Quórum requerido 50.0 y alcanzado 50.6 + 12.3 + 5.5 = 68.4
        assertEquals("50,00 %", FormateadorMoneda.formatearPorcentaje(50.0))
        assertEquals("68,40 %", FormateadorMoneda.formatearPorcentaje(50.6 + 12.3 + 5.5))
    }

    @Test
    fun `Votaciones - porcentaje sobre votos y aprobacion (fraccion por 100)`() {
        val aFavor = 50.6 / 68.4
        assertEquals("73,98 %", FormateadorMoneda.formatearPorcentaje(aFavor * 100))
        assertEquals("100,00 %", FormateadorMoneda.formatearPorcentaje(1.0 * 100))
    }

    @Test
    fun `Perfil - coeficiente del propietario`() {
        assertEquals("2,45 %", FormateadorMoneda.formatearPorcentaje(2.45))
    }

    @Test
    fun `formatearPorcentaje agrupa miles y admite cero`() {
        assertEquals("0,00 %", FormateadorMoneda.formatearPorcentaje(0.0))
        assertEquals("1.234,50 %", FormateadorMoneda.formatearPorcentaje(1234.5))
    }
}
