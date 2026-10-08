package com.cuentasclaras.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cuentasclaras.app.domain.model.*
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

@Entity(tableName = "consorcio")
data class ConsorcioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val direccion: String,
    val unidadesTotales: Int
)

@Entity(
    tableName = "propietario",
    foreignKeys = [ForeignKey(entity = ConsorcioEntity::class, parentColumns = ["id"], childColumns = ["consorcioId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("consorcioId")]
)
data class PropietarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val consorcioId: Long,
    val nombre: String,
    val unidad: String,
    val piso: String,
    val coeficiente: Double,
    val esMiembroConsejo: Boolean
)

@Entity(
    tableName = "expensa",
    foreignKeys = [ForeignKey(entity = PropietarioEntity::class, parentColumns = ["id"], childColumns = ["propietarioId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("propietarioId")]
)
data class ExpensaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propietarioId: Long,
    val periodo: YearMonth,
    val ordinariaCentavos: Long,
    val extraordinariaCentavos: Long,
    val cuotaExtraActual: Int,
    val cuotaExtraTotal: Int,
    val vencimiento: LocalDate,
    val alDia: Boolean
)

@Entity(
    tableName = "tope_mensual",
    foreignKeys = [ForeignKey(entity = ConsorcioEntity::class, parentColumns = ["id"], childColumns = ["consorcioId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("consorcioId")]
)
data class TopeMensualEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val consorcioId: Long,
    val periodo: YearMonth,
    val topeCentavos: Long,
    val conciliadoConBanco: Boolean
)

@Entity(
    tableName = "gasto",
    foreignKeys = [ForeignKey(entity = ConsorcioEntity::class, parentColumns = ["id"], childColumns = ["consorcioId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("consorcioId"), Index("fecha")]
)
data class GastoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val consorcioId: Long,
    val titulo: String,
    val descripcion: String,
    val tipo: TipoGasto,
    val rubro: RubroGasto,
    val montoCentavos: Long,
    val fecha: LocalDate,
    val proveedor: String,
    val tipoComprobante: TipoComprobante,
    val numeroComprobante: String,
    val referenciaMercadoCentavos: Long?,
    val rangoMercadoMinCentavos: Long?,
    val rangoMercadoMaxCentavos: Long?,
    val notaAuditoria: String?,
    val justificacion: String?,
    val estado: EstadoGasto,
    val requiereLicitacion: Boolean,
    val urgencia: Urgencia,
    val ubicacion: String?,
    val solicitadoPor: String?
)

@Entity(
    tableName = "presupuesto",
    foreignKeys = [ForeignKey(entity = GastoEntity::class, parentColumns = ["id"], childColumns = ["gastoId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("gastoId")]
)
data class PresupuestoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gastoId: Long,
    val proveedor: String,
    val montoCentavos: Long,
    val plazoObra: String,
    val garantiaMeses: Int,
    val estadoArt: EstadoArt,
    val calificacion: Double,
    val consorciosAuditados: Int,
    val etiqueta: String?,
    val notas: String,
    val adjudicado: Boolean
)

@Entity(
    tableName = "firma_consejo",
    foreignKeys = [
        ForeignKey(entity = GastoEntity::class, parentColumns = ["id"], childColumns = ["gastoId"], onDelete = ForeignKey.CASCADE),
        // No strict foreign key to Presupuesto since it can be null until selected, we manage it logically.
    ],
    indices = [Index("gastoId")]
)
data class FirmaConsejoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gastoId: Long,
    val presupuestoId: Long?,
    val miembroNombre: String,
    val miembroUnidad: String,
    val propietarioId: Long?,
    val firmado: Boolean,
    val fechaFirma: Instant?
)

@Entity(
    tableName = "concepto_administrativo",
    foreignKeys = [ForeignKey(entity = ConsorcioEntity::class, parentColumns = ["id"], childColumns = ["consorcioId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("consorcioId")]
)
data class ConceptoAdministrativoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val consorcioId: Long,
    val nombre: String,
    val categoria: CategoriaConcepto,
    val organismo: String,
    val montoCentavos: Long,
    val periodo: YearMonth,
    val vencimiento: LocalDate,
    val estado: EstadoConcepto,
    val descripcion: String,
    val numeroComprobante: String
)

@Entity(
    tableName = "votacion",
    foreignKeys = [ForeignKey(entity = ConsorcioEntity::class, parentColumns = ["id"], childColumns = ["consorcioId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("consorcioId")]
)
data class VotacionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val consorcioId: Long,
    val numero: Int,
    val titulo: String,
    val descripcion: String,
    val tipoAsamblea: TipoAsamblea,
    val apertura: Instant,
    val cierre: Instant,
    val quorumRequerido: Double,
    val proveedorRecomendado: String?,
    val montoCentavos: Long?,
    val rangoMercadoMinCentavos: Long?,
    val rangoMercadoMaxCentavos: Long?,
    val gastoId: Long?
)

@Entity(
    tableName = "opcion_voto",
    foreignKeys = [ForeignKey(entity = VotacionEntity::class, parentColumns = ["id"], childColumns = ["votacionId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("votacionId")]
)
data class OpcionVotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val votacionId: Long,
    val titulo: String,
    val descripcion: String,
    val tipo: TipoOpcion,
    val recomendada: Boolean,
    val orden: Int,
    val coeficienteAcumulado: Double,
    val unidadesAcumuladas: Int
)

@Entity(
    tableName = "voto",
    foreignKeys = [
        ForeignKey(entity = VotacionEntity::class, parentColumns = ["id"], childColumns = ["votacionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = OpcionVotoEntity::class, parentColumns = ["id"], childColumns = ["opcionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PropietarioEntity::class, parentColumns = ["id"], childColumns = ["propietarioId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("votacionId", "propietarioId", unique = true), Index("opcionId"), Index("propietarioId")]
)
data class VotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val votacionId: Long,
    val opcionId: Long,
    val propietarioId: Long,
    val coeficiente: Double,
    val fechaHora: Instant,
    val comprobante: String
)

@Entity(
    tableName = "alerta",
    foreignKeys = [ForeignKey(entity = ConsorcioEntity::class, parentColumns = ["id"], childColumns = ["consorcioId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("consorcioId")]
)
data class AlertaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val consorcioId: Long,
    val titulo: String,
    val mensaje: String,
    val tipo: TipoAlerta,
    val prioridad: PrioridadAlerta,
    val fechaHora: Instant,
    val leida: Boolean,
    val gastoId: Long?,
    val votacionId: Long?
)
