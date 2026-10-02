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
}
