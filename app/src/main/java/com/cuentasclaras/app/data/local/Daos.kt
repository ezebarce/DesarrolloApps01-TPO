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

    // Solo para gastos nuevos. No usamos REPLACE: en SQLite REPLACE borra la fila
    // existente y la vuelve a insertar, y el borrado dispara el CASCADE de
    // presupuestos y firmas.
    @Insert
    suspend fun insertarGasto(gasto: GastoEntity): Long

    // Para editar: UPDATE modifica la fila en su lugar y no toca los registros hijos.
    @Update
    suspend fun actualizarGasto(gasto: GastoEntity)

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

    // Solo para votaciones nuevas (REPLACE borraría opciones y votos por CASCADE).
    @Insert
    suspend fun insertarVotacion(votacion: VotacionEntity): Long

    @Update
    suspend fun actualizarVotacion(votacion: VotacionEntity)

    @Query("DELETE FROM votacion WHERE id = :id")
    suspend fun eliminarVotacion(id: Long)

    @Query("SELECT * FROM opcion_voto WHERE votacionId = :votacionId ORDER BY orden ASC")
    fun observarOpciones(votacionId: Long): Flow<List<OpcionVotoEntity>>

    @Insert
    suspend fun insertarOpcion(opcion: OpcionVotoEntity): Long

    @Query("DELETE FROM opcion_voto WHERE votacionId = :votacionId")
    suspend fun eliminarOpcionesDeVotacion(votacionId: Long)

    // Votos registrados como filas de la tabla voto.
    @Query("SELECT COUNT(*) FROM voto WHERE votacionId = :votacionId")
    suspend fun contarVotos(votacionId: Long): Int

    // Votos de demostración que el sembrador guarda como totales dentro de cada opción.
    @Query("SELECT COALESCE(SUM(unidadesAcumuladas), 0) FROM opcion_voto WHERE votacionId = :votacionId")
    suspend fun contarVotosAcumulados(votacionId: Long): Int

    /**
     * Crea o edita una votación junto con sus opciones en una única transacción
     * (si algo falla, no queda nada guardado a medias).
     *
     * Regla del MVP: una votación que ya recibió votos no se puede editar,
     * porque cambiar sus opciones o condiciones alteraría lo que se votó.
     * En ese caso no se modifica nada y se devuelve null.
     */
    @Transaction
    suspend fun guardarVotacionConOpciones(
        votacion: VotacionEntity,
        opciones: List<OpcionVotoEntity>
    ): Long? {
        if (votacion.id == 0L) {
            val nuevoId = insertarVotacion(votacion)
            opciones.forEach { insertarOpcion(it.copy(votacionId = nuevoId)) }
            return nuevoId
        }

        if (contarVotos(votacion.id) > 0 || contarVotosAcumulados(votacion.id) > 0) {
            return null
        }

        // Sin votos: reemplazar las opciones no puede borrar ningún voto.
        actualizarVotacion(votacion)
        eliminarOpcionesDeVotacion(votacion.id)
        opciones.forEach { insertarOpcion(it.copy(votacionId = votacion.id)) }
        return votacion.id
    }

    @Query("SELECT * FROM voto WHERE votacionId = :votacionId AND propietarioId = :propietarioId")
    fun observarVotoPropietario(votacionId: Long, propietarioId: Long): Flow<VotoEntity?>

    @Query("SELECT * FROM voto WHERE votacionId = :votacionId")
    fun observarVotos(votacionId: Long): Flow<List<VotoEntity>>

    // IGNORE: si ya existe un voto de ese propietario en esa votación (índice único
    // votacionId + propietarioId), no se inserta nada y Room devuelve -1.
    // El voto original nunca se reemplaza. La comprobación y la inserción son una
    // sola sentencia SQL, por lo que es atómica.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
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
