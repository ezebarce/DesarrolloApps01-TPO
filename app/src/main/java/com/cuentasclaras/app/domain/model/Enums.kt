package com.cuentasclaras.app.domain.model

enum class TipoGasto { ORDINARIA, EXTRAORDINARIA, FONDO_RESERVA }
enum class RubroGasto { MANTENIMIENTO, PERSONAL, SERVICIOS, ELECTRICIDAD, HIGIENE, OTROS }
enum class TipoComprobante { FACTURA_A, FACTURA_B, FACTURA_C, TICKET_FISCAL, RECIBO, TRANSFERENCIA }
enum class EstadoGasto { SOLICITADO, PRESUPUESTADO, APROBADO, PAGADO, COMPROBANTE_FISCAL }
enum class Urgencia { BAJA, MEDIA, ALTA }
enum class EstadoArt { AL_DIA, EN_TRAMITE, SIN_ART }
enum class CategoriaConcepto { IMPUESTO, TASA, SERVICIO_PUBLICO, SEGURO, CARGAS_SOCIALES, OTRO }
enum class EstadoConcepto { PENDIENTE, PAGADO, VENCIDO }
enum class TipoAsamblea { ORDINARIA, EXTRAORDINARIA }
enum class EstadoVotacion { PROXIMA, ACTIVA, FINALIZADA }
enum class TipoOpcion { A_FAVOR, EN_CONTRA, ABSTENCION }
enum class TipoAlerta { GASTO, VOTACION, MANTENIMIENTO, AVISO_GENERAL }
enum class PrioridadAlerta { ALTA, MEDIA, BAJA }
