package com.cuentasclaras.app.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    object Inicio : Screen

    @Serializable
    object Gastos : Screen

    @Serializable
    object Votaciones : Screen

    @Serializable
    object Alertas : Screen

    @Serializable
    data class DetalleGasto(val gastoId: Long) : Screen

    @Serializable
    data class FormularioGasto(val gastoId: Long? = null) : Screen

    @Serializable
    data class FormularioPresupuesto(val gastoId: Long, val presupuestoId: Long? = null) : Screen

    @Serializable
    data class FormularioConcepto(val conceptoId: Long? = null) : Screen

    @Serializable
    data class DetalleVotacion(val votacionId: Long) : Screen

    @Serializable
    data class FormularioVotacion(val votacionId: Long? = null) : Screen

    @Serializable
    data class DetalleAlerta(val alertaId: Long) : Screen

    @Serializable
    data class FormularioAlerta(val alertaId: Long? = null) : Screen

    @Serializable
    object Perfil : Screen
}

fun Screen.getRouteName(): String {
    return this::class.simpleName ?: ""
}
