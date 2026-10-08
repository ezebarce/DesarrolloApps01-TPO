package com.cuentasclaras.app.data.mapper

import com.cuentasclaras.app.data.local.*
import com.cuentasclaras.app.domain.model.*

fun ConsorcioEntity.toDomain() = Consorcio(id, nombre, direccion, unidadesTotales)
fun Consorcio.toEntity() = ConsorcioEntity(id, nombre, direccion, unidadesTotales)

fun PropietarioEntity.toDomain() = Propietario(id, consorcioId, nombre, unidad, piso, coeficiente, esMiembroConsejo)
fun Propietario.toEntity() = PropietarioEntity(id, consorcioId, nombre, unidad, piso, coeficiente, esMiembroConsejo)

fun ExpensaEntity.toDomain() = Expensa(id, propietarioId, periodo, ordinariaCentavos, extraordinariaCentavos, cuotaExtraActual, cuotaExtraTotal, vencimiento, alDia)
fun Expensa.toEntity() = ExpensaEntity(id, propietarioId, periodo, ordinariaCentavos, extraordinariaCentavos, cuotaExtraActual, cuotaExtraTotal, vencimiento, alDia)

fun TopeMensualEntity.toDomain() = TopeMensual(id, consorcioId, periodo, topeCentavos, conciliadoConBanco)
fun TopeMensual.toEntity() = TopeMensualEntity(id, consorcioId, periodo, topeCentavos, conciliadoConBanco)

fun GastoEntity.toDomain() = Gasto(id, consorcioId, titulo, descripcion, tipo, rubro, montoCentavos, fecha, proveedor, tipoComprobante, numeroComprobante, referenciaMercadoCentavos, rangoMercadoMinCentavos, rangoMercadoMaxCentavos, notaAuditoria, justificacion, estado, requiereLicitacion, urgencia, ubicacion, solicitadoPor)
fun Gasto.toEntity() = GastoEntity(id, consorcioId, titulo, descripcion, tipo, rubro, montoCentavos, fecha, proveedor, tipoComprobante, numeroComprobante, referenciaMercadoCentavos, rangoMercadoMinCentavos, rangoMercadoMaxCentavos, notaAuditoria, justificacion, estado, requiereLicitacion, urgencia, ubicacion, solicitadoPor)

fun PresupuestoEntity.toDomain() = Presupuesto(id, gastoId, proveedor, montoCentavos, plazoObra, garantiaMeses, estadoArt, calificacion, consorciosAuditados, etiqueta, notas, adjudicado)
fun Presupuesto.toEntity() = PresupuestoEntity(id, gastoId, proveedor, montoCentavos, plazoObra, garantiaMeses, estadoArt, calificacion, consorciosAuditados, etiqueta, notas, adjudicado)

fun FirmaConsejoEntity.toDomain() = FirmaConsejo(id, gastoId, presupuestoId, miembroNombre, miembroUnidad, propietarioId, firmado, fechaFirma)
fun FirmaConsejo.toEntity() = FirmaConsejoEntity(id, gastoId, presupuestoId, miembroNombre, miembroUnidad, propietarioId, firmado, fechaFirma)

fun ConceptoAdministrativoEntity.toDomain() = ConceptoAdministrativo(id, consorcioId, nombre, categoria, organismo, montoCentavos, periodo, vencimiento, estado, descripcion, numeroComprobante)
fun ConceptoAdministrativo.toEntity() = ConceptoAdministrativoEntity(id, consorcioId, nombre, categoria, organismo, montoCentavos, periodo, vencimiento, estado, descripcion, numeroComprobante)

fun VotacionEntity.toDomain() = Votacion(id, consorcioId, numero, titulo, descripcion, tipoAsamblea, apertura, cierre, quorumRequerido, proveedorRecomendado, montoCentavos, rangoMercadoMinCentavos, rangoMercadoMaxCentavos, gastoId)
fun Votacion.toEntity() = VotacionEntity(id, consorcioId, numero, titulo, descripcion, tipoAsamblea, apertura, cierre, quorumRequerido, proveedorRecomendado, montoCentavos, rangoMercadoMinCentavos, rangoMercadoMaxCentavos, gastoId)

fun OpcionVotoEntity.toDomain() = OpcionVoto(id, votacionId, titulo, descripcion, tipo, recomendada, orden, coeficienteAcumulado, unidadesAcumuladas)
fun OpcionVoto.toEntity() = OpcionVotoEntity(id, votacionId, titulo, descripcion, tipo, recomendada, orden, coeficienteAcumulado, unidadesAcumuladas)

fun VotoEntity.toDomain() = Voto(id, votacionId, opcionId, propietarioId, coeficiente, fechaHora, comprobante)
fun Voto.toEntity() = VotoEntity(id, votacionId, opcionId, propietarioId, coeficiente, fechaHora, comprobante)

fun AlertaEntity.toDomain() = Alerta(id, consorcioId, titulo, mensaje, tipo, prioridad, fechaHora, leida, gastoId, votacionId)
fun Alerta.toEntity() = AlertaEntity(id, consorcioId, titulo, mensaje, tipo, prioridad, fechaHora, leida, gastoId, votacionId)
