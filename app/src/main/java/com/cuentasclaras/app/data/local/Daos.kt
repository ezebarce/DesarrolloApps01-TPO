package com.cuentasclaras.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {
    @Query("SELECT * FROM gasto WHERE consorcioId = :consorcioId ORDER BY fecha DESC")
    fun observarGastos(consorcioId: Long): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gasto WHERE consorcioId = :consorcioId AND substr(fecha, 1, 7) = :periodoStr ORDER BY fecha DESC")
    fun observarGastosPorPeriodo(consorcioId: Long, periodoStr: String): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gasto WHERE id = :id")
    fun observarGasto(id: Long): Flow<GastoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarGasto(gasto: GastoEntity): Long

    @Query("DELETE FROM gasto WHERE id = :id")
    suspend fun eliminarGasto(id: Long)

    @Query("SELECT * FROM presupuesto WHERE gastoId = :gastoId")
    fun observarPresupuestos(gastoId: Long): Flow<List<PresupuestoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPresupuesto(presupuesto: PresupuestoEntity): Long

    @Query("DELETE FROM presupuesto WHERE id = :id")
    suspend fun eliminarPresupuesto(id: Long)

    @Query("UPDATE presupuesto SET adjudicado = :adjudicado WHERE id = :id")
    suspend fun actualizarAdjudicacion(id: Long, adjudicado: Boolean)

    @Query("SELECT * FROM firma_consejo WHERE gastoId = :gastoId")
    fun observarFirmas(gastoId: Long): Flow<List<FirmaConsejoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarFirma(firma: FirmaConsejoEntity): Long
}

@Dao
interface ConceptoDao {
    @Query("SELECT * FROM concepto_administrativo WHERE consorcioId = :consorcioId ORDER BY vencimiento DESC")
    fun observarConceptos(consorcioId: Long): Flow<List<ConceptoAdministrativoEntity>>

    @Query("SELECT * FROM concepto_administrativo WHERE consorcioId = :consorcioId AND periodo = :periodoStr ORDER BY vencimiento DESC")
    fun observarConceptosPorPeriodo(consorcioId: Long, periodoStr: String): Flow<List<ConceptoAdministrativoEntity>>

    @Query("SELECT * FROM concepto_administrativo WHERE id = :id")
    fun observarConcepto(id: Long): Flow<ConceptoAdministrativoEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarConcepto(concepto: ConceptoAdministrativoEntity): Long

    @Query("DELETE FROM concepto_administrativo WHERE id = :id")
    suspend fun eliminarConcepto(id: Long)
}

@Dao
interface VotacionDao {
    @Query("SELECT * FROM votacion WHERE consorcioId = :consorcioId ORDER BY cierre DESC")
    fun observarVotaciones(consorcioId: Long): Flow<List<VotacionEntity>>

    @Query("SELECT * FROM votacion WHERE id = :id")
    fun observarVotacion(id: Long): Flow<VotacionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVotacion(votacion: VotacionEntity): Long

    @Query("DELETE FROM votacion WHERE id = :id")
    suspend fun eliminarVotacion(id: Long)

    @Query("SELECT * FROM opcion_voto WHERE votacionId = :votacionId ORDER BY orden ASC")
    fun observarOpciones(votacionId: Long): Flow<List<OpcionVotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOpcion(opcion: OpcionVotoEntity): Long

    @Query("DELETE FROM opcion_voto WHERE votacionId = :votacionId")
    suspend fun eliminarOpcionesDeVotacion(votacionId: Long)

    @Query("SELECT * FROM voto WHERE votacionId = :votacionId AND propietarioId = :propietarioId")
    fun observarVotoPropietario(votacionId: Long, propietarioId: Long): Flow<VotoEntity?>

    @Query("SELECT * FROM voto WHERE votacionId = :votacionId")
    fun observarVotos(votacionId: Long): Flow<List<VotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVoto(voto: VotoEntity): Long
}

@Dao
interface AlertaDao {
    @Query("SELECT * FROM alerta WHERE consorcioId = :consorcioId ORDER BY fechaHora DESC")
    fun observarAlertas(consorcioId: Long): Flow<List<AlertaEntity>>

    @Query("SELECT * FROM alerta WHERE id = :id")
    fun observarAlerta(id: Long): Flow<AlertaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlerta(alerta: AlertaEntity): Long

    @Query("DELETE FROM alerta WHERE id = :id")
    suspend fun eliminarAlerta(id: Long)

    @Query("UPDATE alerta SET leida = 1 WHERE id = :id")
    suspend fun marcarComoLeida(id: Long)

    @Query("UPDATE alerta SET leida = 1 WHERE consorcioId = :consorcioId AND leida = 0")
    suspend fun marcarTodasComoLeidas(consorcioId: Long)
}

@Dao
interface InfoConsorcioDao {
    @Query("SELECT * FROM consorcio WHERE id = :id")
    fun observarConsorcio(id: Long): Flow<ConsorcioEntity?>

    @Query("SELECT * FROM propietario WHERE id = :id")
    fun observarPropietario(id: Long): Flow<PropietarioEntity?>

    @Query("SELECT * FROM expensa WHERE propietarioId = :propietarioId ORDER BY periodo DESC")
    fun observarExpensas(propietarioId: Long): Flow<List<ExpensaEntity>>

    @Query("SELECT * FROM tope_mensual WHERE consorcioId = :consorcioId AND periodo = :periodoStr")
    fun observarTopeMensual(consorcioId: Long, periodoStr: String): Flow<TopeMensualEntity?>
    
    // Métodos para el sembrador de datos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarConsorcio(consorcio: ConsorcioEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPropietario(propietario: PropietarioEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarExpensa(expensa: ExpensaEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTopeMensual(tope: TopeMensualEntity): Long

    @Query("DELETE FROM consorcio")
    suspend fun limpiarTodo()
}
