package com.cuentasclaras.app.feature.gastos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cuentasclaras.app.core.navigation.Screen
import com.cuentasclaras.app.domain.model.*
import com.cuentasclaras.app.domain.repository.ConceptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class FormularioConceptoState(
    val id: Long? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: CategoriaConcepto = CategoriaConcepto.OTRO,
    val organismo: String = "",
    val montoText: String = "",
    val fechaVencimiento: LocalDate = LocalDate.now(),
    val periodo: YearMonth = YearMonth.now(),
    val estado: EstadoConcepto = EstadoConcepto.PENDIENTE,
    val numeroComprobante: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val guardadoExitoso: Boolean = false
)

@HiltViewModel
class FormularioConceptoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: ConceptoRepository
) : ViewModel() {

    private val conceptoId = savedStateHandle.toRoute<Screen.FormularioConcepto>().conceptoId

    private val _uiState = MutableStateFlow(FormularioConceptoState(id = conceptoId))
    val uiState: StateFlow<FormularioConceptoState> = _uiState.asStateFlow()

    private var consorcioIdActual: Long = 1L

    init {
        if (conceptoId != null) {
            viewModelScope.launch {
                val c = repository.observarConcepto(conceptoId).first()
                if (c != null) {
                    _uiState.update {
                        it.copy(
                            nombre = c.nombre,
                            descripcion = c.descripcion,
                            categoria = c.categoria,
                            organismo = c.organismo,
                            montoText = (c.montoCentavos / 100.0).toString(),
                            fechaVencimiento = c.vencimiento,
                            periodo = c.periodo,
                            estado = c.estado,
                            numeroComprobante = c.numeroComprobante
                        )
                    }
                }
            }
        }
    }

    fun onNombreChange(v: String) = _uiState.update { it.copy(nombre = v, error = null) }
    fun onCategoriaChange(v: CategoriaConcepto) = _uiState.update { it.copy(categoria = v) }
    fun onOrganismoChange(v: String) = _uiState.update { it.copy(organismo = v) }
    fun onMontoChange(v: String) = _uiState.update { it.copy(montoText = v, error = null) }
    fun onEstadoChange(v: EstadoConcepto) = _uiState.update { it.copy(estado = v) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    fun guardar() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        if (state.nombre.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "El nombre es obligatorio") }
            return
        }

        val montoCentavos = (state.montoText.toDoubleOrNull() ?: 0.0).let { it * 100 }.toLong()
        if (montoCentavos <= 0) {
            _uiState.update { it.copy(isLoading = false, error = "El monto debe ser mayor a 0") }
            return
        }

        val concepto = ConceptoAdministrativo(
            id = state.id ?: 0L,
            consorcioId = consorcioIdActual,
            nombre = state.nombre,
            categoria = state.categoria,
            organismo = state.organismo,
            montoCentavos = montoCentavos,
            periodo = state.periodo,
            vencimiento = state.fechaVencimiento,
            estado = state.estado,
            descripcion = state.descripcion,
            numeroComprobante = state.numeroComprobante
        )

        viewModelScope.launch {
            repository.guardarConcepto(concepto)
            _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
        }
    }

    fun eliminar(onEliminado: () -> Unit) {
        if (conceptoId != null) {
            viewModelScope.launch {
                repository.eliminarConcepto(conceptoId)
                onEliminado()
            }
        }
    }
}
