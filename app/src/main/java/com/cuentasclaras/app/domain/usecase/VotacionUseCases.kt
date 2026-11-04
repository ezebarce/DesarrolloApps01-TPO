package com.cuentasclaras.app.domain.usecase

import com.cuentasclaras.app.domain.model.*
import com.cuentasclaras.app.domain.repository.VotacionRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.util.UUID

class RegistrarVotoUseCase(private val repository: VotacionRepository) {
    sealed interface Result {
        data class Exito(val comprobante: String) : Result
        sealed interface Error : Result {
            object VotacionInvalida : Error
            object VotacionCerrada : Error
            object OpcionInvalida : Error
            object PropietarioNoHabilitado : Error
            object YaVoto : Error
        }
    }

    // Validaciones locales de consistencia de los datos. No reemplazan una
    // autenticación real: la identidad del propietario viene de la sesión simulada.
    suspend operator fun invoke(votacionId: Long, opcionId: Long, propietario: Propietario): Result {
        val votacion = repository.observarVotacion(votacionId).first() ?: return Result.Error.VotacionInvalida

        if (votacion.estado != EstadoVotacion.ACTIVA) {
            return Result.Error.VotacionCerrada
        }

        // La opción elegida tiene que ser una de las opciones de ESTA votación.
        val opciones = repository.observarOpciones(votacionId).first()
        if (opciones.none { it.id == opcionId }) {
            return Result.Error.OpcionInvalida
        }

        // Solo votan los propietarios del consorcio que convocó la votación.
        if (propietario.consorcioId != votacion.consorcioId) {
            return Result.Error.PropietarioNoHabilitado
        }

        val votoExistente = repository.observarVotoPropietario(votacionId, propietario.id).first()
        if (votoExistente != null) {
            return Result.Error.YaVoto
        }

        val comprobante = "ACTA-UF${propietario.unidad.replace(" ", "")}-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
        
        val voto = Voto(
            votacionId = votacionId,
            opcionId = opcionId,
            propietarioId = propietario.id,
            coeficiente = propietario.coeficiente,
            fechaHora = Instant.now(),
            comprobante = comprobante
        )

        // Barrera final: la restricción única de Room (votacionId + propietarioId).
        // Si llega un duplicado (por ejemplo, un doble toque), la base lo rechaza y devuelve -1.
        val votoId = repository.registrarVoto(voto)
        if (votoId == -1L) {
            return Result.Error.YaVoto
        }
        return Result.Exito(comprobante)
    }
}

class ResultadosVotacionUseCase(private val repository: VotacionRepository) {
    data class Resultados(
        val totalUnidades: Int,
        val totalCoeficiente: Double,
        val quorumPorcentaje: Double,
        val hayQuorum: Boolean,
        val aprobada: Boolean,
        val estadoTexto: String,
        val opcionesPonderadas: List<OpcionPonderada>
    )
    
    data class OpcionPonderada(
        val opcion: OpcionVoto,
        val coeficienteTotal: Double,
        val unidadesTotal: Int,
        val porcentajeSobreVotos: Double
    )

    suspend operator fun invoke(votacionId: Long, unidadesConsorcio: Int = 44): Resultados? {
        val votacion = repository.observarVotacion(votacionId).first() ?: return null
        val opciones = repository.observarOpciones(votacionId).first()
        val votos = repository.observarVotos(votacionId).first()

        val quorumAbsoluto = votos.sumOf { it.coeficiente } + opciones.sumOf { it.coeficienteAcumulado }
        val unidadesQueVotaron = votos.size + opciones.sumOf { it.unidadesAcumuladas }
        
        val hayQuorum = quorumAbsoluto >= votacion.quorumRequerido

        val ponderadas = opciones.map { op ->
            val votosDeOpcion = votos.filter { it.opcionId == op.id }
            val coefSum = op.coeficienteAcumulado + votosDeOpcion.sumOf { it.coeficiente }
            val unitSum = op.unidadesAcumuladas + votosDeOpcion.size
            
            OpcionPonderada(
                opcion = op,
                coeficienteTotal = coefSum,
                unidadesTotal = unitSum,
                porcentajeSobreVotos = if (quorumAbsoluto > 0) (coefSum / quorumAbsoluto) else 0.0
            )
        }

        val aFavor = ponderadas.find { it.opcion.tipo == TipoOpcion.A_FAVOR }?.coeficienteTotal ?: 0.0
        val enContra = ponderadas.find { it.opcion.tipo == TipoOpcion.EN_CONTRA }?.coeficienteTotal ?: 0.0
        val aprobada = hayQuorum && (aFavor > enContra)
        
        val estadoTexto = when {
            votacion.estado != EstadoVotacion.FINALIZADA -> "En curso"
            !hayQuorum -> "Sin quórum"
            aprobada -> "Aprobada"
            else -> "Rechazada"
        }

        return Resultados(
            totalUnidades = unidadesQueVotaron,
            totalCoeficiente = quorumAbsoluto,
            quorumPorcentaje = quorumAbsoluto,
            hayQuorum = hayQuorum,
            aprobada = aprobada,
            estadoTexto = estadoTexto,
            opcionesPonderadas = ponderadas
        )
    }
}
