package com.cuentasclaras.app.feature.gastos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cuentasclaras.app.core.navigation.Screen
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.data.session.Capacidades
import com.cuentasclaras.app.data.session.SesionRepository
import com.cuentasclaras.app.domain.model.FirmaConsejo
import com.cuentasclaras.app.domain.model.Gasto
import com.cuentasclaras.app.domain.model.Propietario
import com.cuentasclaras.app.domain.repository.GastoRepository
import com.cuentasclaras.app.domain.repository.InfoConsorcioRepository
import com.cuentasclaras.app.domain.usecase.FirmarAdjudicacionUseCase
import com.cuentasclaras.app.domain.usecase.PonderarPresupuestosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetalleGastoState(
    val gasto: Gasto,
    val presupuestos: List<PonderarPresupuestosUseCase.ResultadoPonderacion>,
    val firmas: List<FirmaConsejo>,
    val capacidades: Capacidades,
    val propietarioActual: Propietario?,
    val confirmandoFirma: Boolean = false,
    val mostrarMensaje: String? = null
)

@HiltViewModel
class DetalleGastoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gastoRepository: GastoRepository,
    private val infoConsorcioRepository: InfoConsorcioRepository,
    private val sesionRepository: SesionRepository,
    private val ponderarPresupuestos: PonderarPresupuestosUseCase,
    private val firmarAdjudicacion: FirmarAdjudicacionUseCase
) : ViewModel() {

    private val gastoId = savedStateHandle.toRoute<Screen.DetalleGasto>().gastoId

    private val _uiState = MutableStateFlow<UiState<DetalleGastoState>>(UiState.Cargando)
    val uiState: StateFlow<UiState<DetalleGastoState>> = _uiState.asStateFlow()

    private val _confirmandoFirma = MutableStateFlow(false)
    private val _mensaje = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            val propietarioId = sesionRepository.propietarioIdActual.first()
            val capacidades = sesionRepository.capacidadesActuales.first()
            val propietario = infoConsorcioRepository.observarPropietario(propietarioId).first()

            combine(
                gastoRepository.observarGasto(gastoId),
                gastoRepository.observarPresupuestos(gastoId),
                gastoRepository.observarFirmas(gastoId),
                _confirmandoFirma,
                _mensaje
            ) { gasto, presupuestos, firmas, confirmando, mensaje ->
                if (gasto == null) {
                    UiState.Vacio("Gasto no encontrado")
                } else {
                    val ponderados = ponderarPresupuestos(presupuestos)
                    UiState.Contenido(
                        DetalleGastoState(
                            gasto = gasto,
                            presupuestos = ponderados,
                            firmas = firmas,
                            capacidades = capacidades,
                            propietarioActual = propietario,
                            confirmandoFirma = confirmando,
                            mostrarMensaje = mensaje
                        )
                    )
                }
            }.catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Error al cargar detalle")
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun firmarPresupuesto(presupuestoId: Long) {
        viewModelScope.launch {
            _confirmandoFirma.value = true
            // Simulador de ConfirmadorDeIdentidad (~1.2s de espera como indica el prompt)
            delay(1200)

            val propietarioId = sesionRepository.propietarioIdActual.first()
            val propietario = infoConsorcioRepository.observarPropietario(propietarioId).first()

            if (propietario != null) {
                when (val result = firmarAdjudicacion(gastoId, presupuestoId, propietario)) {
                    is FirmarAdjudicacionUseCase.Result.Exito -> {
                        _mensaje.value = "Adjudicación completada"
                    }
                    is FirmarAdjudicacionUseCase.Result.PendienteDeMasFirmas -> {
                        _mensaje.value = "Firma registrada, faltan miembros"
                    }
                    is FirmarAdjudicacionUseCase.Result.Error -> {
                        _mensaje.value = "Error al firmar"
                    }
                }
            }
            _confirmandoFirma.value = false
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun eliminarGasto(onEliminado: () -> Unit) {
        viewModelScope.launch {
            gastoRepository.eliminarGasto(gastoId)
            onEliminado()
        }
    }

    fun eliminarPresupuesto(presupuestoId: Long) {
        viewModelScope.launch {
            gastoRepository.eliminarPresupuesto(presupuestoId)
            _mensaje.value = "Presupuesto eliminado"
        }
    }
}
