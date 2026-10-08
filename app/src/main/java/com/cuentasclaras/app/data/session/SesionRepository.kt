package com.cuentasclaras.app.data.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Capacidades(
    val puedeGestionarGastos: Boolean,
    val puedeGestionarConceptos: Boolean,
    val puedeGestionarVotaciones: Boolean,
    val puedeGestionarAlertas: Boolean,
    val puedeVotar: Boolean,
    val puedeAprobarPresupuestos: Boolean,
)

interface SesionRepository {
    val propietarioIdActual: Flow<Long>
    val consorcioIdActual: Flow<Long>
    val capacidadesActuales: Flow<Capacidades>
}

// Implementación temporal para la Fase 1: siempre es Martín D. con ID 1 y Consorcio ID 1
// Como pide el prompt: "el ABM queda habilitado temporalmente para el Propietario"
// Estas capacidades permitirán que la UI actúe como admin/owner combinados.
class SesionRepositoryImpl : SesionRepository {
    override val propietarioIdActual: Flow<Long> = kotlinx.coroutines.flow.flowOf(1L)
    override val consorcioIdActual: Flow<Long> = kotlinx.coroutines.flow.flowOf(1L)
    
    // esMiembroConsejo es true en los datos semilla para Martín D., por lo que puede aprobar presupuestos.
    override val capacidadesActuales: Flow<Capacidades> = kotlinx.coroutines.flow.flowOf(
        Capacidades(
            puedeGestionarGastos = true,
            puedeGestionarConceptos = true,
            puedeGestionarVotaciones = true,
            puedeGestionarAlertas = true,
            puedeVotar = true,
            puedeAprobarPresupuestos = true
        )
    )
}
