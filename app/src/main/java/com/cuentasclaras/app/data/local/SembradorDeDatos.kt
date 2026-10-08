package com.cuentasclaras.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.cuentasclaras.app.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class SembradorDeDatos(
    private val db: AppDatabase,
    private val dataStore: DataStore<Preferences>
) {
    private val DATOS_SEMBRADOS = booleanPreferencesKey("datos_sembrados")

    suspend fun inicializarSiEsNecesario() {
        val yaSembrado = dataStore.data.map { it[DATOS_SEMBRADOS] ?: false }.first()
        if (!yaSembrado) {
            sembrar()
            dataStore.edit { it[DATOS_SEMBRADOS] = true }
        }
    }

    suspend fun restablecerDatosDeEjemplo() {
        db.infoConsorcioDao().limpiarTodo()
        sembrar()
        dataStore.edit { it[DATOS_SEMBRADOS] = true }
    }

    private suspend fun sembrar() {
        val dao = db.infoConsorcioDao()
        val gastoDao = db.gastoDao()
        val votacionDao = db.votacionDao()
        val conceptoDao = db.conceptoDao()
        val alertaDao = db.alertaDao()

        val hoy = LocalDate.now()
        val mesActual = YearMonth.now()
        val mesPasado = mesActual.minusMonths(1)
        val ahora = Instant.now()

        // 1. Consorcio y Propietario
        val consorcioId = dao.insertarConsorcio(
            ConsorcioEntity(nombre = "Torre Las Heras 2450", direccion = "Las Heras 2450, CABA", unidadesTotales = 44)
        )

        val propId = dao.insertarPropietario(
            PropietarioEntity(consorcioId = consorcioId, nombre = "Martín D.", unidad = "8B", piso = "8", coeficiente = 2.45, esMiembroConsejo = true)
        )

        // 2. Expensa y Tope
        dao.insertarExpensa(
            ExpensaEntity(propietarioId = propId, periodo = mesActual, ordinariaCentavos = 14200000, extraordinariaCentavos = 4225000, cuotaExtraActual = 2, cuotaExtraTotal = 6, vencimiento = hoy.plusDays(10), alDia = true)
        )
        dao.insertarTopeMensual(
            TopeMensualEntity(consorcioId = consorcioId, periodo = mesActual, topeCentavos = 337000000, conciliadoConBanco = true)
        )

        // 3. Gastos
        val g1Id = gastoDao.insertarGasto(GastoEntity(consorcioId = consorcioId, titulo = "Abono Ascensores", descripcion = "Servicio mensual", tipo = TipoGasto.ORDINARIA, rubro = RubroGasto.MANTENIMIENTO, montoCentavos = 48000000, fecha = hoy.minusDays(2), proveedor = "Ascensores ElevAR", tipoComprobante = TipoComprobante.FACTURA_A, numeroComprobante = "0004-1829", referenciaMercadoCentavos = 48500000, rangoMercadoMinCentavos = 46000000, rangoMercadoMaxCentavos = 51000000, notaAuditoria = null, justificacion = null, estado = EstadoGasto.COMPROBANTE_FISCAL, requiereLicitacion = false, urgencia = Urgencia.BAJA, ubicacion = null, solicitadoPor = null))
        
        val g2Id = gastoDao.insertarGasto(GastoEntity(consorcioId = consorcioId, titulo = "Reemplazo luminarias LED palier", descripcion = "Pisos 1 al 10", tipo = TipoGasto.ORDINARIA, rubro = RubroGasto.ELECTRICIDAD, montoCentavos = 21540000, fecha = hoy.minusDays(5), proveedor = "Ferretería La Luz", tipoComprobante = TipoComprobante.TICKET_FISCAL, numeroComprobante = "TK-9283", referenciaMercadoCentavos = 18250000, rangoMercadoMinCentavos = 17500000, rangoMercadoMaxCentavos = 19500000, notaAuditoria = null, justificacion = "Compra urgente fuera de horario para evitar pasillos oscuros.", estado = EstadoGasto.PAGADO, requiereLicitacion = false, urgencia = Urgencia.MEDIA, ubicacion = "Palieres", solicitadoPor = "Encargado"))
        
        val g3Id = gastoDao.insertarGasto(GastoEntity(consorcioId = consorcioId, titulo = "Limpieza y desinfección de tanques", descripcion = "Semestral", tipo = TipoGasto.ORDINARIA, rubro = RubroGasto.HIGIENE, montoCentavos = 19500000, fecha = hoy.minusDays(10), proveedor = "Limpito SA", tipoComprobante = TipoComprobante.FACTURA_A, numeroComprobante = "0012-9844", referenciaMercadoCentavos = null, rangoMercadoMinCentavos = null, rangoMercadoMaxCentavos = null, notaAuditoria = "3 presupuestos presentados", justificacion = null, estado = EstadoGasto.PAGADO, requiereLicitacion = false, urgencia = Urgencia.MEDIA, ubicacion = "Azotea", solicitadoPor = "Admin"))

        val g4Id = gastoDao.insertarGasto(GastoEntity(consorcioId = consorcioId, titulo = "Sueldo Encargado + Aportes", descripcion = "Haberes del mes", tipo = TipoGasto.ORDINARIA, rubro = RubroGasto.PERSONAL, montoCentavos = 152045000, fecha = hoy.minusDays(8), proveedor = "F. López", tipoComprobante = TipoComprobante.TRANSFERENCIA, numeroComprobante = "TR-23948", referenciaMercadoCentavos = null, rangoMercadoMinCentavos = null, rangoMercadoMaxCentavos = null, notaAuditoria = "Convenio SUTERH verificado", justificacion = null, estado = EstadoGasto.PAGADO, requiereLicitacion = false, urgencia = Urgencia.BAJA, ubicacion = null, solicitadoPor = null))

        // Gasto extraordinario con Licitación
        val gLicId = gastoDao.insertarGasto(GastoEntity(consorcioId = consorcioId, titulo = "Reparación estructural de caño troncal", descripcion = "Pérdida en subsuelo", tipo = TipoGasto.EXTRAORDINARIA, rubro = RubroGasto.MANTENIMIENTO, montoCentavos = 46500000, fecha = hoy, proveedor = "A definir", tipoComprobante = TipoComprobante.FACTURA_A, numeroComprobante = "PENDIENTE", referenciaMercadoCentavos = 46500000, rangoMercadoMinCentavos = 42000000, rangoMercadoMaxCentavos = 51000000, notaAuditoria = null, justificacion = null, estado = EstadoGasto.PRESUPUESTADO, requiereLicitacion = true, urgencia = Urgencia.MEDIA, ubicacion = "Sala de Máquinas", solicitadoPor = "Portería / Admin"))

        val p1 = gastoDao.insertarPresupuesto(PresupuestoEntity(gastoId = gLicId, proveedor = "HidroInstalaciones Belgrano", montoCentavos = 44000000, plazoObra = "48 hs hábiles", garantiaMeses = 12, estadoArt = EstadoArt.AL_DIA, calificacion = 4.9, consorciosAuditados = 18, etiqueta = null, notas = "Incluye materiales", adjudicado = false))
        val p2 = gastoDao.insertarPresupuesto(PresupuestoEntity(gastoId = gLicId, proveedor = "Plomería Integral San Martín", montoCentavos = 39000000, plazoObra = "5 días hábiles", garantiaMeses = 3, estadoArt = EstadoArt.EN_TRAMITE, calificacion = 3.8, consorciosAuditados = 0, etiqueta = null, notas = "Pago contado", adjudicado = false))
        val p3 = gastoDao.insertarPresupuesto(PresupuestoEntity(gastoId = gLicId, proveedor = "Servicios Hidráulicos Express", montoCentavos = 59000000, plazoObra = "24 hs corridas", garantiaMeses = 6, estadoArt = EstadoArt.AL_DIA, calificacion = 4.5, consorciosAuditados = 6, etiqueta = "Servicio 24hs express", notas = "Urgencias", adjudicado = false))

        gastoDao.insertarFirma(FirmaConsejoEntity(gastoId = gLicId, presupuestoId = p1, miembroNombre = "Sofía Martínez", miembroUnidad = "4A", propietarioId = null, firmado = true, fechaFirma = ahora.minusSeconds(3600)))
        gastoDao.insertarFirma(FirmaConsejoEntity(gastoId = gLicId, presupuestoId = p1, miembroNombre = "Carlos Rossi", miembroUnidad = "7C", propietarioId = null, firmado = true, fechaFirma = ahora.minusSeconds(1800)))
        gastoDao.insertarFirma(FirmaConsejoEntity(gastoId = gLicId, presupuestoId = null, miembroNombre = "Martín D.", miembroUnidad = "8B", propietarioId = propId, firmado = false, fechaFirma = null))

        // 4. Conceptos Administrativos
        conceptoDao.insertarConcepto(ConceptoAdministrativoEntity(consorcioId = consorcioId, nombre = "ABL", categoria = CategoriaConcepto.IMPUESTO, organismo = "AGIP", montoCentavos = 3540000, periodo = mesActual, vencimiento = hoy.plusDays(5), estado = EstadoConcepto.PENDIENTE, descripcion = "Cuota mensual", numeroComprobante = "0123-ABL"))
        conceptoDao.insertarConcepto(ConceptoAdministrativoEntity(consorcioId = consorcioId, nombre = "AySA", categoria = CategoriaConcepto.SERVICIO_PUBLICO, organismo = "AySA", montoCentavos = 9850000, periodo = mesActual, vencimiento = hoy.minusDays(2), estado = EstadoConcepto.PAGADO, descripcion = "Agua y saneamiento", numeroComprobante = "AYSA-8821"))

        // 5. Votaciones
        val v1Id = votacionDao.insertarVotacion(VotacionEntity(consorcioId = consorcioId, numero = 42, titulo = "Aprobación de presupuesto para renovación de impermeabilización en terraza", descripcion = "Renovación total de membranas", tipoAsamblea = TipoAsamblea.EXTRAORDINARIA, apertura = ahora.minus(2, ChronoUnit.DAYS), cierre = ahora.plus(2, ChronoUnit.DAYS), quorumRequerido = 50.0, proveedorRecomendado = "TechosPro SRL", montoCentavos = 285000000, rangoMercadoMinCentavos = 240000000, rangoMercadoMaxCentavos = 340000000, gastoId = null))
        votacionDao.insertarOpcion(OpcionVotoEntity(votacionId = v1Id, titulo = "Aprobar impermeabilización", descripcion = "Adjudica a TechosPro SRL e inicia cronograma de 3 cuotas.", tipo = TipoOpcion.A_FAVOR, recomendada = true, orden = 0, coeficienteAcumulado = 50.6, unidadesAcumuladas = 23))
        votacionDao.insertarOpcion(OpcionVotoEntity(votacionId = v1Id, titulo = "Rechazar propuesta", descripcion = "Solicita nueva compulsa de precios para la próxima asamblea.", tipo = TipoOpcion.EN_CONTRA, recomendada = false, orden = 1, coeficienteAcumulado = 12.3, unidadesAcumuladas = 6))
        votacionDao.insertarOpcion(OpcionVotoEntity(votacionId = v1Id, titulo = "Abstención", descripcion = "Suma al quórum pero no incide en el cómputo final.", tipo = TipoOpcion.ABSTENCION, recomendada = false, orden = 2, coeficienteAcumulado = 5.5, unidadesAcumuladas = 2))
        
        val v2Id = votacionDao.insertarVotacion(VotacionEntity(consorcioId = consorcioId, numero = 41, titulo = "Rendición de Cuentas y Balance", descripcion = "Aprobación ejercicio anterior", tipoAsamblea = TipoAsamblea.ORDINARIA, apertura = ahora.minus(30, ChronoUnit.DAYS), cierre = ahora.minus(20, ChronoUnit.DAYS), quorumRequerido = 50.0, proveedorRecomendado = null, montoCentavos = null, rangoMercadoMinCentavos = null, rangoMercadoMaxCentavos = null, gastoId = null))
        votacionDao.insertarOpcion(OpcionVotoEntity(votacionId = v2Id, titulo = "Aprobar", descripcion = "", tipo = TipoOpcion.A_FAVOR, recomendada = false, orden = 0, coeficienteAcumulado = 60.0, unidadesAcumuladas = 28))
        votacionDao.insertarOpcion(OpcionVotoEntity(votacionId = v2Id, titulo = "Rechazar", descripcion = "", tipo = TipoOpcion.EN_CONTRA, recomendada = false, orden = 1, coeficienteAcumulado = 10.0, unidadesAcumuladas = 4))

        // 6. Alertas
        alertaDao.insertarAlerta(AlertaEntity(consorcioId = consorcioId, titulo = "Nueva votación abierta", mensaje = "Aprobación de presupuesto para impermeabilización.", tipo = TipoAlerta.VOTACION, prioridad = PrioridadAlerta.ALTA, fechaHora = ahora.minus(1, ChronoUnit.HOURS), leida = false, gastoId = null, votacionId = v1Id))
        alertaDao.insertarAlerta(AlertaEntity(consorcioId = consorcioId, titulo = "Desvío sobre referencia", mensaje = "El gasto de luminarias LED superó la referencia de mercado.", tipo = TipoAlerta.GASTO, prioridad = PrioridadAlerta.MEDIA, fechaHora = ahora.minus(5, ChronoUnit.DAYS), leida = true, gastoId = g2Id, votacionId = null))
    }
}
