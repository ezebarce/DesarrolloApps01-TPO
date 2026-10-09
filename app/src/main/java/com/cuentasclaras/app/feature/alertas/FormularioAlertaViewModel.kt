package com.cuentasclaras.app.feature.alertas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cuentasclaras.app.core.navigation.Screen
import com.cuentasclaras.app.domain.model.Alerta
import com.cuentasclaras.app.domain.model.PrioridadAlerta
import com.cuentasclaras.app.domain.model.TipoAlerta
import com.cuentasclaras.app.domain.repository.AlertaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

data class FormularioAlertaState(
    val id: Long? = null,
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: TipoAlerta = TipoAlerta.AVISO_GENERAL,
    val prioridad: PrioridadAlerta = PrioridadAlerta.MEDIA,
    val vinculacionIdTexto: String = "", // Simplificado en vez de un selector complejo
    val isLoading: Boolean = false,
    val error: String? = null,
    val guardadoExitoso: Boolean = false
)

@HiltViewModel
class FormularioAlertaViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: AlertaRepository
) : ViewModel() {

    private val alertaId = savedStateHandle.toRoute<Screen.FormularioAlerta>().alertaId

    private val _uiState = MutableStateFlow(FormularioAlertaState(id = alertaId))
    val uiState: StateFlow<FormularioAlertaState> = _uiState.asStateFlow()

    private var consorcioIdActual: Long = 1L

    init {
        if (alertaId != null) {
            viewModelScope.launch {
                val a = repository.observarAlerta(alertaId).first()
                if (a != null) {
                    _uiState.update {
                        it.copy(
                            titulo = a.titulo,
                            mensaje = a.mensaje,
                            tipo = a.tipo,
                            prioridad = a.prioridad,
                            vinculacionIdTexto = (a.gastoId ?: a.votacionId ?: "").toString()
                        )
                    }
                }
            }
        }
    }

    fun onTituloChange(v: String) = _uiState.update { it.copy(titulo = v, error = null) }
    fun onMensajeChange(v: String) = _uiState.update { it.copy(mensaje = v) }
    fun onTipoChange(v: TipoAlerta) = _uiState.update { it.copy(tipo = v) }
    fun onPrioridadChange(v: PrioridadAlerta) = _uiState.update { it.copy(prioridad = v) }
    fun onVinculacionIdChange(v: String) = _uiState.update { it.copy(vinculacionIdTexto = v) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    fun guardar() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        if (state.titulo.isBlank() || state.mensaje.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Complete título y mensaje") }
            return
        }

        val vinculacionId = state.vinculacionIdTexto.toLongOrNull()
        val gastoId = if (state.tipo == TipoAlerta.GASTO) vinculacionId else null
        val votacionId = if (state.tipo == TipoAlerta.VOTACION) vinculacionId else null

        val alerta = Alerta(
            id = state.id ?: 0L,
            consorcioId = consorcioIdActual,
            titulo = state.titulo,
            mensaje = state.mensaje,
            tipo = state.tipo,
            prioridad = state.prioridad,
            fechaHora = Instant.now(), // Al crear o editar, simplificamos actualizando fecha
            leida = false,
            gastoId = gastoId,
            votacionId = votacionId
        )

        viewModelScope.launch {
            repository.guardarAlerta(alerta)
            _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
        }
    }
}
