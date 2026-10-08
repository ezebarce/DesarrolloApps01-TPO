package com.cuentasclaras.app.domain.usecase

import com.cuentasclaras.app.domain.model.*
import com.cuentasclaras.app.domain.repository.GastoRepository

class GuardarGastoUseCase(private val gastoRepository: GastoRepository) {
    sealed interface Result {
        data class Exito(val id: Long) : Result
        sealed interface Error : Result {
            object SinTitulo : Error
            object MontoInvalido : Error
            object SinComprobante : Error
        }
    }

    suspend operator fun invoke(gasto: Gasto): Result {
        if (gasto.titulo.isBlank()) return Result.Error.SinTitulo
        if (gasto.montoCentavos <= 0) return Result.Error.MontoInvalido
        if (gasto.numeroComprobante.isBlank()) return Result.Error.SinComprobante

        // Regla 6.2: Licitacion (Umbral = $ 300.000,00)
        val umbralLicitacion = 300_000_00L // en centavos
        var requiereLicitacion = gasto.requiereLicitacion
        if (gasto.tipo == TipoGasto.EXTRAORDINARIA && gasto.montoCentavos > umbralLicitacion) {
            requiereLicitacion = true
        }

        val gastoFinal = gasto.copy(requiereLicitacion = requiereLicitacion)
        val id = gastoRepository.guardarGasto(gastoFinal)
        return Result.Exito(id)
    }
}

class CalcularDesvioDeMercadoUseCase {
    sealed interface Desvio {
        object SinReferencia : Desvio
        data class EnPrecio(val porcentaje: Double) : Desvio
        data class BajoReferencia(val porcentaje: Double) : Desvio
        data class SobreReferencia(val porcentaje: Double) : Desvio
    }

    operator fun invoke(montoCentavos: Long, referenciaCentavos: Long?): Desvio {
        if (referenciaCentavos == null || referenciaCentavos == 0L) return Desvio.SinReferencia
        
        val diff = montoCentavos - referenciaCentavos
        val porcentaje = diff.toDouble() / referenciaCentavos.toDouble()

        return when {
            porcentaje > 0.10 -> Desvio.SobreReferencia(porcentaje)
            porcentaje < -0.10 -> Desvio.BajoReferencia(porcentaje)
            else -> Desvio.EnPrecio(porcentaje)
        }
    }
}

class PonderarPresupuestosUseCase {
    data class ResultadoPonderacion(
        val presupuesto: Presupuesto,
        val puntaje: Double,
        val esMejorOpcion: Boolean,
        val esMasEconomica: Boolean
    )

    operator fun invoke(presupuestos: List<Presupuesto>): List<ResultadoPonderacion> {
        if (presupuestos.isEmpty()) return emptyList()

        val precios = presupuestos.map { it.montoCentavos }
        val precioMin = precios.minOrNull() ?: 1L
        val precioMax = precios.maxOrNull() ?: 1L
        val rangoPrecio = if (precioMax > precioMin) (precioMax - precioMin).toDouble() else 1.0

        // Encontramos el más económico absoluto
        val presupuestoMasEconomicoId = presupuestos.minByOrNull { it.montoCentavos }?.id

        val puntuados = presupuestos.map { p ->
            // Normalizar precio: 1.0 es el más barato, 0.0 es el más caro
            val precioNormalizado = 1.0 - ((p.montoCentavos - precioMin) / rangoPrecio)
            
            val valorArt = when (p.estadoArt) {
                EstadoArt.AL_DIA -> 1.0
                EstadoArt.EN_TRAMITE -> 0.5
                EstadoArt.SIN_ART -> 0.0
            }
            
            val valorGarantia = (p.garantiaMeses / 12.0).coerceAtMost(1.0)
            
            val puntaje = (0.4 * precioNormalizado) + 
                          (0.3 * (p.calificacion / 5.0)) + 
                          (0.2 * valorArt) + 
                          (0.1 * valorGarantia)
                          
            Triple(p, puntaje, p.id == presupuestoMasEconomicoId)
        }

        val maxPuntaje = puntuados.maxOfOrNull { it.second } ?: 0.0

        return puntuados.map { (p, puntaje, esEconomica) ->
            ResultadoPonderacion(
                presupuesto = p,
                puntaje = puntaje,
                esMejorOpcion = puntaje == maxPuntaje,
                esMasEconomica = esEconomica
            )
        }
    }
}
