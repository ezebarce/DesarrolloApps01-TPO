package com.cuentasclaras.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

data class Consorcio(
    val id: Long = 0,
    val nombre: String,
    val direccion: String,
    val unidadesTotales: Int
)

data class Propietario(
    val id: Long = 0,
    val consorcioId: Long,
    val nombre: String,
    val unidad: String,
    val piso: String,
    val coeficiente: Double,
    val esMiembroConsejo: Boolean
)

data class Expensa(
    val id: Long = 0,
    val propietarioId: Long,
    val periodo: YearMonth,
    val ordinariaCentavos: Long,
    val extraordinariaCentavos: Long,
    val cuotaExtraActual: Int,
    val cuotaExtraTotal: Int,
    val vencimiento: LocalDate,
    val alDia: Boolean
)

data class TopeMensual(
    val id: Long = 0,
    val consorcioId: Long,
    val periodo: YearMonth,
    val topeCentavos: Long,
    val conciliadoConBanco: Boolean
)

data class Gasto(
    val id: Long = 0,
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
    val referenciaMercadoCentavos: Long? = null,
    val rangoMercadoMinCentavos: Long? = null,
    val rangoMercadoMaxCentavos: Long? = null,
    val notaAuditoria: String? = null,
    val justificacion: String? = null,
    val estado: EstadoGasto = EstadoGasto.SOLICITADO,
    val requiereLicitacion: Boolean = false,
    val urgencia: Urgencia = Urgencia.BAJA,
    val ubicacion: String? = null,
    val solicitadoPor: String? = null
)

data class Presupuesto(
    val id: Long = 0,
    val gastoId: Long,
    val proveedor: String,
    val montoCentavos: Long,
    val plazoObra: String,
    val garantiaMeses: Int,
    val estadoArt: EstadoArt,
    val calificacion: Double,
    val consorciosAuditados: Int,
    val etiqueta: String? = null,
    val notas: String,
    val adjudicado: Boolean = false
)

data class FirmaConsejo(
    val id: Long = 0,
    val gastoId: Long,
    val presupuestoId: Long?,
    val miembroNombre: String,
    val miembroUnidad: String,
    val propietarioId: Long?,
    val firmado: Boolean,
    val fechaFirma: Instant? = null
)

data class ConceptoAdministrativo(
    val id: Long = 0,
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

data class Votacion(
    val id: Long = 0,
    val consorcioId: Long,
    val numero: Int,
    val titulo: String,
    val descripcion: String,
    val tipoAsamblea: TipoAsamblea,
    val apertura: Instant,
    val cierre: Instant,
    val quorumRequerido: Double = 50.0,
    val proveedorRecomendado: String? = null,
    val montoCentavos: Long? = null,
    val rangoMercadoMinCentavos: Long? = null,
    val rangoMercadoMaxCentavos: Long? = null,
    val gastoId: Long? = null
) {
    val estado: EstadoVotacion
        get() {
            val now = Instant.now()
            return when {
                now.isBefore(apertura) -> EstadoVotacion.PROXIMA
                now.isAfter(cierre) -> EstadoVotacion.FINALIZADA
                else -> EstadoVotacion.ACTIVA
            }
        }
}

data class OpcionVoto(
    val id: Long = 0,
    val votacionId: Long,
    val titulo: String,
    val descripcion: String,
    val tipo: TipoOpcion,
    val recomendada: Boolean,
    val orden: Int,
    val coeficienteAcumulado: Double = 0.0,
    val unidadesAcumuladas: Int = 0
)

data class Voto(
    val id: Long = 0,
    val votacionId: Long,
    val opcionId: Long,
    val propietarioId: Long,
    val coeficiente: Double,
    val fechaHora: Instant,
    val comprobante: String
)

data class Alerta(
    val id: Long = 0,
    val consorcioId: Long,
    val titulo: String,
    val mensaje: String,
    val tipo: TipoAlerta,
    val prioridad: PrioridadAlerta,
    val fechaHora: Instant,
    val leida: Boolean = false,
    val gastoId: Long? = null,
    val votacionId: Long? = null
)
