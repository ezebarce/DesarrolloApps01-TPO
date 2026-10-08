package com.cuentasclaras.app.domain.repository

import com.cuentasclaras.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface GastoRepository {
    fun observarGastos(consorcioId: Long): Flow<List<Gasto>>
    fun observarGastosPorPeriodo(consorcioId: Long, periodo: YearMonth): Flow<List<Gasto>>
    fun observarGasto(gastoId: Long): Flow<Gasto?>
    suspend fun guardarGasto(gasto: Gasto): Long
    suspend fun eliminarGasto(gastoId: Long)
    
    fun observarPresupuestos(gastoId: Long): Flow<List<Presupuesto>>
    suspend fun guardarPresupuesto(presupuesto: Presupuesto): Long
    suspend fun eliminarPresupuesto(presupuestoId: Long)
    suspend fun actualizarAdjudicacion(presupuestoId: Long, adjudicado: Boolean)
    
    fun observarFirmas(gastoId: Long): Flow<List<FirmaConsejo>>
    suspend fun guardarFirma(firma: FirmaConsejo): Long
}

interface ConceptoRepository {
    fun observarConceptos(consorcioId: Long): Flow<List<ConceptoAdministrativo>>
    fun observarConceptosPorPeriodo(consorcioId: Long, periodo: YearMonth): Flow<List<ConceptoAdministrativo>>
    fun observarConcepto(conceptoId: Long): Flow<ConceptoAdministrativo?>
    suspend fun guardarConcepto(concepto: ConceptoAdministrativo): Long
    suspend fun eliminarConcepto(conceptoId: Long)
}

interface VotacionRepository {
    fun observarVotaciones(consorcioId: Long): Flow<List<Votacion>>
    fun observarVotacion(votacionId: Long): Flow<Votacion?>
    suspend fun guardarVotacion(votacion: Votacion): Long
    suspend fun eliminarVotacion(votacionId: Long)
    
    fun observarOpciones(votacionId: Long): Flow<List<OpcionVoto>>
    suspend fun guardarOpcion(opcion: OpcionVoto): Long
    suspend fun eliminarOpcionesDeVotacion(votacionId: Long)
    
    fun observarVotoPropietario(votacionId: Long, propietarioId: Long): Flow<Voto?>
    fun observarVotos(votacionId: Long): Flow<List<Voto>>
    suspend fun registrarVoto(voto: Voto): Long
}

interface AlertaRepository {
    fun observarAlertas(consorcioId: Long): Flow<List<Alerta>>
    fun observarAlerta(alertaId: Long): Flow<Alerta?>
    suspend fun guardarAlerta(alerta: Alerta): Long
    suspend fun eliminarAlerta(alertaId: Long)
    suspend fun marcarComoLeida(alertaId: Long)
    suspend fun marcarTodasComoLeidas(consorcioId: Long)
}

interface InfoConsorcioRepository {
    fun observarConsorcio(consorcioId: Long): Flow<Consorcio?>
    fun observarPropietario(propietarioId: Long): Flow<Propietario?>
    fun observarExpensas(propietarioId: Long): Flow<List<Expensa>>
    fun observarTopeMensual(consorcioId: Long, periodo: YearMonth): Flow<TopeMensual?>
}
