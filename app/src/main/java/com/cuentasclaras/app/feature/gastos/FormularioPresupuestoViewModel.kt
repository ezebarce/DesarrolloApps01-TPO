package com.cuentasclaras.app.feature.gastos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cuentasclaras.app.core.navigation.Screen
import com.cuentasclaras.app.domain.model.EstadoArt
import com.cuentasclaras.app.domain.model.Presupuesto
import com.cuentasclaras.app.domain.repository.GastoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FormularioPresupuestoState(
    val gastoId: Long = 0,
    val presupuestoId: Long? = null,
    val proveedor: String = "",
    val montoText: String = "",
    val plazoObra: String = "",
    val garantiaMesesText: String = "",
    val estadoArt: EstadoArt = EstadoArt.SIN_ART,
    val calificacionText: String = "",
    val consorciosAuditadosText: String = "0",
    val etiqueta: String = "",
    val notas: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val guardadoExitoso: Boolean = false
)

@HiltViewModel
class FormularioPresupuestoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GastoRepository
) : ViewModel() {

    private val args = savedStateHandle.toRoute<Screen.FormularioPresupuesto>()
    private val gastoId = args.gastoId
    private val presupuestoId = args.presupuestoId

    private val _uiState = MutableStateFlow(FormularioPresupuestoState(gastoId = gastoId, presupuestoId = presupuestoId))
    val uiState: StateFlow<FormularioPresupuestoState> = _uiState.asStateFlow()

    init {
        if (presupuestoId != null) {
            viewModelScope.launch {
                val presupuestos = repository.observarPresupuestos(gastoId).first()
                val p = presupuestos.find { it.id == presupuestoId }
                if (p != null) {
                    _uiState.update {
                        it.copy(
                            proveedor = p.proveedor,
                            montoText = (p.montoCentavos / 100.0).toString(),
                            plazoObra = p.plazoObra,
                            garantiaMesesText = p.garantiaMeses.toString(),
                            estadoArt = p.estadoArt,
                            calificacionText = p.calificacion.toString(),
                            consorciosAuditadosText = p.consorciosAuditados.toString(),
                            etiqueta = p.etiqueta ?: "",
                            notas = p.notas
                        )
                    }
                }
            }
        }
    }

    fun onProveedorChange(v: String) = _uiState.update { it.copy(proveedor = v, error = null) }
    fun onMontoChange(v: String) = _uiState.update { it.copy(montoText = v, error = null) }
    fun onPlazoObraChange(v: String) = _uiState.update { it.copy(plazoObra = v) }
    fun onGarantiaChange(v: String) = _uiState.update { it.copy(garantiaMesesText = v) }
    fun onEstadoArtChange(v: EstadoArt) = _uiState.update { it.copy(estadoArt = v) }
    fun onCalificacionChange(v: String) = _uiState.update { it.copy(calificacionText = v) }
    fun onConsorciosAuditadosChange(v: String) = _uiState.update { it.copy(consorciosAuditadosText = v) }
    fun onEtiquetaChange(v: String) = _uiState.update { it.copy(etiqueta = v) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    fun guardar() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        if (state.proveedor.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "El proveedor es obligatorio") }
            return
        }

        val montoCentavos = (state.montoText.toDoubleOrNull() ?: 0.0).let { it * 100 }.toLong()
        if (montoCentavos <= 0) {
            _uiState.update { it.copy(isLoading = false, error = "El monto debe ser mayor a 0") }
            return
        }

        val presupuesto = Presupuesto(
            id = state.presupuestoId ?: 0L,
            gastoId = state.gastoId,
            proveedor = state.proveedor,
            montoCentavos = montoCentavos,
            plazoObra = state.plazoObra,
            garantiaMeses = state.garantiaMesesText.toIntOrNull() ?: 0,
            estadoArt = state.estadoArt,
            calificacion = state.calificacionText.toDoubleOrNull() ?: 0.0,
            consorciosAuditados = state.consorciosAuditadosText.toIntOrNull() ?: 0,
            etiqueta = state.etiqueta.takeIf { it.isNotBlank() },
            notas = state.notas,
            adjudicado = false // No se puede adjudicar creando/editando un presupuesto
        )

        viewModelScope.launch {
            repository.guardarPresupuesto(presupuesto)
            _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
        }
    }
}
