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
            object YaVoto : Error
        }
    }

    suspend operator fun invoke(votacionId: Long, opcionId: Long, propietario: Propietario): Result {
        val votacion = repository.observarVotacion(votacionId).first() ?: return Result.Error.VotacionInvalida

        if (votacion.estado != EstadoVotacion.ACTIVA) {
            return Result.Error.VotacionCerrada
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

        repository.registrarVoto(voto)
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
