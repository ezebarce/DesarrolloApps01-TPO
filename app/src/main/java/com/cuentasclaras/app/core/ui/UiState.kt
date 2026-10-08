package com.cuentasclaras.app.core.ui

sealed interface UiState<out T> {
    object Cargando : UiState<Nothing>
    data class Contenido<T>(val datos: T) : UiState<T>
    data class Vacio(val mensaje: String) : UiState<Nothing>
    data class Error(val mensaje: String) : UiState<Nothing>
}
