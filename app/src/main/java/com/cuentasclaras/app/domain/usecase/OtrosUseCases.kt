package com.cuentasclaras.app.domain.usecase

import com.cuentasclaras.app.domain.model.*
import com.cuentasclaras.app.domain.repository.GastoRepository
import kotlinx.coroutines.flow.first
import java.time.Instant

class FirmarAdjudicacionUseCase(private val repository: GastoRepository) {
    sealed interface Result {
        object Exito : Result
        object PendienteDeMasFirmas : Result
        sealed interface Error : Result {
            object GastoInvalido : Error
            object SinPresupuestos : Error
        }
    }

    suspend operator fun invoke(gastoId: Long, presupuestoId: Long, propietario: Propietario): Result {
        val gasto = repository.observarGasto(gastoId).first() ?: return Result.Error.GastoInvalido
        val firmas = repository.observarFirmas(gastoId).first()
        val presupuestos = repository.observarPresupuestos(gastoId).first()

        if (presupuestos.isEmpty()) return Result.Error.SinPresupuestos

        // Actualizamos la firma del propietario actual
        val firmaPropietario = firmas.find { it.propietarioId == propietario.id }
        if (firmaPropietario != null) {
            repository.guardarFirma(
                firmaPropietario.copy(
                    presupuestoId = presupuestoId,
                    firmado = true,
                    fechaFirma = Instant.now()
                )
            )
        } else {
            // Si no estaba en la lista pre-armada, la agregamos (edge case en semillas)
            repository.guardarFirma(
                FirmaConsejo(
                    gastoId = gastoId,
                    presupuestoId = presupuestoId,
                    miembroNombre = propietario.nombre,
                    miembroUnidad = propietario.unidad,
                    propietarioId = propietario.id,
                    firmado = true,
                    fechaFirma = Instant.now()
                )
            )
        }

        // Refrescar firmas tras guardar
        val firmasActualizadas = repository.observarFirmas(gastoId).first()
        val totalMiembros = firmasActualizadas.size.coerceAtLeast(1)
        val firmasParaEstePresupuesto = firmasActualizadas.count { it.firmado && it.presupuestoId == presupuestoId }

        // Regla 6.5: Adjudicación con al menos 2/3 de las firmas
        val umbral = (totalMiembros * 2.0 / 3.0).toInt().coerceAtLeast(1)
        
        if (firmasParaEstePresupuesto >= umbral) {
            // Adjudicamos
            val p = presupuestos.find { it.id == presupuestoId }
            if (p != null) {
                repository.actualizarAdjudicacion(presupuestoId, true)
                repository.guardarGasto(
                    gasto.copy(
                        estado = EstadoGasto.APROBADO,
                        montoCentavos = p.montoCentavos
                    )
                )
            }
            return Result.Exito
        }

        return Result.PendienteDeMasFirmas
    }
}
