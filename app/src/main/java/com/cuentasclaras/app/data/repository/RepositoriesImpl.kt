package com.cuentasclaras.app.data.repository

import com.cuentasclaras.app.data.local.*
import com.cuentasclaras.app.data.mapper.*
import com.cuentasclaras.app.domain.model.*
import com.cuentasclaras.app.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.YearMonth

class GastoRepositoryImpl(private val dao: GastoDao) : GastoRepository {
    override fun observarGastos(consorcioId: Long): Flow<List<Gasto>> = 
        dao.observarGastos(consorcioId).map { it.map { e -> e.toDomain() } }

    override fun observarGastosPorPeriodo(consorcioId: Long, periodo: YearMonth): Flow<List<Gasto>> =
        dao.observarGastosPorPeriodo(consorcioId, periodo.toString()).map { it.map { e -> e.toDomain() } }

    override fun observarGasto(gastoId: Long): Flow<Gasto?> =
        dao.observarGasto(gastoId).map { it?.toDomain() }

    // id == 0: gasto nuevo (INSERT). Si ya existe: UPDATE, que conserva presupuestos y firmas.
    override suspend fun guardarGasto(gasto: Gasto): Long =
        if (gasto.id == 0L) {
            dao.insertarGasto(gasto.toEntity())
        } else {
            dao.actualizarGasto(gasto.toEntity())
            gasto.id
        }

    override suspend fun eliminarGasto(gastoId: Long) =
        dao.eliminarGasto(gastoId)

    override fun observarPresupuestos(gastoId: Long): Flow<List<Presupuesto>> =
        dao.observarPresupuestos(gastoId).map { it.map { e -> e.toDomain() } }

    override suspend fun guardarPresupuesto(presupuesto: Presupuesto): Long =
        dao.insertarPresupuesto(presupuesto.toEntity())

    override suspend fun eliminarPresupuesto(presupuestoId: Long) =
        dao.eliminarPresupuesto(presupuestoId)

    override suspend fun actualizarAdjudicacion(presupuestoId: Long, adjudicado: Boolean) =
        dao.actualizarAdjudicacion(presupuestoId, adjudicado)

    override fun observarFirmas(gastoId: Long): Flow<List<FirmaConsejo>> =
        dao.observarFirmas(gastoId).map { it.map { e -> e.toDomain() } }

    override suspend fun guardarFirma(firma: FirmaConsejo): Long =
        dao.insertarFirma(firma.toEntity())
}

class ConceptoRepositoryImpl(private val dao: ConceptoDao) : ConceptoRepository {
    override fun observarConceptos(consorcioId: Long): Flow<List<ConceptoAdministrativo>> =
        dao.observarConceptos(consorcioId).map { it.map { e -> e.toDomain() } }

    override fun observarConceptosPorPeriodo(consorcioId: Long, periodo: YearMonth): Flow<List<ConceptoAdministrativo>> =
        dao.observarConceptosPorPeriodo(consorcioId, periodo.toString()).map { it.map { e -> e.toDomain() } }

    override fun observarConcepto(conceptoId: Long): Flow<ConceptoAdministrativo?> =
        dao.observarConcepto(conceptoId).map { it?.toDomain() }

    override suspend fun guardarConcepto(concepto: ConceptoAdministrativo): Long =
        dao.insertarConcepto(concepto.toEntity())

    override suspend fun eliminarConcepto(conceptoId: Long) =
        dao.eliminarConcepto(conceptoId)
}

class VotacionRepositoryImpl(private val dao: VotacionDao) : VotacionRepository {
    override fun observarVotaciones(consorcioId: Long): Flow<List<Votacion>> =
        dao.observarVotaciones(consorcioId).map { it.map { e -> e.toDomain() } }

    override fun observarVotacion(votacionId: Long): Flow<Votacion?> =
        dao.observarVotacion(votacionId).map { it?.toDomain() }

    override suspend fun guardarVotacion(votacion: Votacion): Long =
        if (votacion.id == 0L) {
            dao.insertarVotacion(votacion.toEntity())
        } else {
            dao.actualizarVotacion(votacion.toEntity())
            votacion.id
        }

    override suspend fun eliminarVotacion(votacionId: Long) =
        dao.eliminarVotacion(votacionId)

    override fun observarOpciones(votacionId: Long): Flow<List<OpcionVoto>> =
        dao.observarOpciones(votacionId).map { it.map { e -> e.toDomain() } }

    override suspend fun guardarOpcion(opcion: OpcionVoto): Long =
        dao.insertarOpcion(opcion.toEntity())

    override suspend fun eliminarOpcionesDeVotacion(votacionId: Long) =
        dao.eliminarOpcionesDeVotacion(votacionId)

    override suspend fun guardarVotacionConOpciones(votacion: Votacion, opciones: List<OpcionVoto>): Long? =
        dao.guardarVotacionConOpciones(votacion.toEntity(), opciones.map { it.toEntity() })

    override fun observarVotoPropietario(votacionId: Long, propietarioId: Long): Flow<Voto?> =
        dao.observarVotoPropietario(votacionId, propietarioId).map { it?.toDomain() }

    override fun observarVotos(votacionId: Long): Flow<List<Voto>> =
        dao.observarVotos(votacionId).map { it.map { e -> e.toDomain() } }

    override suspend fun registrarVoto(voto: Voto): Long =
        dao.insertarVoto(voto.toEntity())
}

class AlertaRepositoryImpl(private val dao: AlertaDao) : AlertaRepository {
    override fun observarAlertas(consorcioId: Long): Flow<List<Alerta>> =
        dao.observarAlertas(consorcioId).map { it.map { e -> e.toDomain() } }

    override fun observarAlerta(alertaId: Long): Flow<Alerta?> =
        dao.observarAlerta(alertaId).map { it?.toDomain() }

    override suspend fun guardarAlerta(alerta: Alerta): Long =
        dao.insertarAlerta(alerta.toEntity())

    override suspend fun eliminarAlerta(alertaId: Long) =
        dao.eliminarAlerta(alertaId)

    override suspend fun marcarComoLeida(alertaId: Long) =
        dao.marcarComoLeida(alertaId)

    override suspend fun marcarTodasComoLeidas(consorcioId: Long) =
        dao.marcarTodasComoLeidas(consorcioId)
}

class InfoConsorcioRepositoryImpl(private val dao: InfoConsorcioDao) : InfoConsorcioRepository {
    override fun observarConsorcio(consorcioId: Long): Flow<Consorcio?> =
        dao.observarConsorcio(consorcioId).map { it?.toDomain() }

    override fun observarPropietario(propietarioId: Long): Flow<Propietario?> =
        dao.observarPropietario(propietarioId).map { it?.toDomain() }

    override fun observarExpensas(propietarioId: Long): Flow<List<Expensa>> =
        dao.observarExpensas(propietarioId).map { it.map { e -> e.toDomain() } }

    override fun observarTopeMensual(consorcioId: Long, periodo: YearMonth): Flow<TopeMensual?> =
        dao.observarTopeMensual(consorcioId, periodo.toString()).map { it?.toDomain() }
}
