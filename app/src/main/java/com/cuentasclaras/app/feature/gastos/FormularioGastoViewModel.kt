package com.cuentasclaras.app.feature.gastos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cuentasclaras.app.core.navigation.Screen
import com.cuentasclaras.app.domain.model.*
import com.cuentasclaras.app.domain.repository.GastoRepository
import com.cuentasclaras.app.domain.repository.InfoConsorcioRepository
import com.cuentasclaras.app.domain.usecase.GuardarGastoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class FormularioGastoState(
    val id: Long? = null,
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: TipoGasto = TipoGasto.ORDINARIA,
    val rubro: RubroGasto = RubroGasto.MANTENIMIENTO,
    val montoText: String = "",
    val fecha: LocalDate = LocalDate.now(),
    val proveedor: String = "",
    val tipoComprobante: TipoComprobante = TipoComprobante.FACTURA_A,
    val numeroComprobante: String = "",
    val requiereLicitacion: Boolean = false,
    val urgencia: Urgencia = Urgencia.BAJA,
    val isLoading: Boolean = false,
    val error: String? = null,
    val guardadoExitoso: Boolean = false
)

@HiltViewModel
class FormularioGastoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: GastoRepository,
    private val infoConsorcioRepository: InfoConsorcioRepository,
    private val guardarGastoUseCase: GuardarGastoUseCase
) : ViewModel() {

    private val gastoId = savedStateHandle.toRoute<Screen.FormularioGasto>().gastoId

    private val _uiState = MutableStateFlow(FormularioGastoState(id = gastoId))
    val uiState: StateFlow<FormularioGastoState> = _uiState.asStateFlow()

    private var consorcioIdActual: Long = 1L // Por ahora hardcodeado como en la fase 1

    init {
        if (gastoId != null) {
            viewModelScope.launch {
                val gasto = repository.observarGasto(gastoId).first()
                if (gasto != null) {
                    _uiState.update {
                        it.copy(
                            titulo = gasto.titulo,
                            descripcion = gasto.descripcion,
                            tipo = gasto.tipo,
                            rubro = gasto.rubro,
                            montoText = (gasto.montoCentavos / 100.0).toString(),
                            fecha = gasto.fecha,
                            proveedor = gasto.proveedor,
                            tipoComprobante = gasto.tipoComprobante,
                            numeroComprobante = gasto.numeroComprobante,
                            requiereLicitacion = gasto.requiereLicitacion,
                            urgencia = gasto.urgencia
                        )
                    }
                }
            }
        }
    }

    fun onTituloChange(v: String) = _uiState.update { it.copy(titulo = v, error = null) }
    fun onDescripcionChange(v: String) = _uiState.update { it.copy(descripcion = v) }
    fun onTipoChange(v: TipoGasto) = _uiState.update { it.copy(tipo = v) }
    fun onRubroChange(v: RubroGasto) = _uiState.update { it.copy(rubro = v) }
    fun onMontoChange(v: String) = _uiState.update { it.copy(montoText = v, error = null) }
    fun onFechaChange(v: LocalDate) = _uiState.update { it.copy(fecha = v) }
    fun onProveedorChange(v: String) = _uiState.update { it.copy(proveedor = v) }
    fun onTipoComprobanteChange(v: TipoComprobante) = _uiState.update { it.copy(tipoComprobante = v) }
    fun onNumeroComprobanteChange(v: String) = _uiState.update { it.copy(numeroComprobante = v, error = null) }
    fun onRequiereLicitacionChange(v: Boolean) = _uiState.update { it.copy(requiereLicitacion = v) }
    fun onUrgenciaChange(v: Urgencia) = _uiState.update { it.copy(urgencia = v) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    fun guardar() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        val montoCentavos = (state.montoText.toDoubleOrNull() ?: 0.0).let { it * 100 }.toLong()

        val gasto = Gasto(
            id = state.id ?: 0L,
            consorcioId = consorcioIdActual,
            titulo = state.titulo,
            descripcion = state.descripcion,
            tipo = state.tipo,
            rubro = state.rubro,
            montoCentavos = montoCentavos,
            fecha = state.fecha,
            proveedor = state.proveedor,
            tipoComprobante = state.tipoComprobante,
            numeroComprobante = state.numeroComprobante,
            requiereLicitacion = state.requiereLicitacion,
            urgencia = state.urgencia,
            estado = if (state.requiereLicitacion) EstadoGasto.PRESUPUESTADO else EstadoGasto.COMPROBANTE_FISCAL
        )

        viewModelScope.launch {
            when (val res = guardarGastoUseCase(gasto)) {
                is GuardarGastoUseCase.Result.Exito -> {
                    _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
                }
                GuardarGastoUseCase.Result.Error.MontoInvalido -> {
                    _uiState.update { it.copy(isLoading = false, error = "El monto debe ser mayor a 0") }
                }
                GuardarGastoUseCase.Result.Error.SinComprobante -> {
                    _uiState.update { it.copy(isLoading = false, error = "El número de comprobante es obligatorio") }
                }
                GuardarGastoUseCase.Result.Error.SinTitulo -> {
                    _uiState.update { it.copy(isLoading = false, error = "El título es obligatorio") }
                }
            }
        }
    }
}
