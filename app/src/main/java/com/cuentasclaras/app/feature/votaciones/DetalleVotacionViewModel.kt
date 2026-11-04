package com.cuentasclaras.app.feature.votaciones

import android.database.SQLException
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cuentasclaras.app.core.navigation.Screen
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.data.session.Capacidades
import com.cuentasclaras.app.data.session.SesionRepository
import com.cuentasclaras.app.domain.model.Propietario
import com.cuentasclaras.app.domain.model.Votacion
import com.cuentasclaras.app.domain.model.Voto
import com.cuentasclaras.app.domain.repository.InfoConsorcioRepository
import com.cuentasclaras.app.domain.repository.VotacionRepository
import com.cuentasclaras.app.domain.usecase.RegistrarVotoUseCase
import com.cuentasclaras.app.domain.usecase.ResultadosVotacionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetalleVotacionState(
    val votacion: Votacion,
    val capacidades: Capacidades,
    val propietarioActual: Propietario?,
    val miVoto: Voto?,
    val resultados: ResultadosVotacionUseCase.Resultados?,
    val opcionElegidaId: Long? = null,
    val confirmandoVoto: Boolean = false,
    val mostrarMensaje: String? = null
)

@HiltViewModel
class DetalleVotacionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VotacionRepository,
    private val infoConsorcioRepository: InfoConsorcioRepository,
    private val sesionRepository: SesionRepository,
    private val registrarVotoUseCase: RegistrarVotoUseCase,
    private val resultadosVotacionUseCase: ResultadosVotacionUseCase
) : ViewModel() {

    private val votacionId = savedStateHandle.toRoute<Screen.DetalleVotacion>().votacionId

    private val _uiState = MutableStateFlow<UiState<DetalleVotacionState>>(UiState.Cargando)
    val uiState: StateFlow<UiState<DetalleVotacionState>> = _uiState.asStateFlow()

    private val _opcionElegidaId = MutableStateFlow<Long?>(null)
    private val _confirmandoVoto = MutableStateFlow(false)
    private val _mensaje = MutableStateFlow<String?>(null)
    private val _refrescar = MutableStateFlow(0) // Trigger manual

    init {
        viewModelScope.launch {
            val propietarioId = sesionRepository.propietarioIdActual.first()
            val capacidades = sesionRepository.capacidadesActuales.first()
            val propietario = infoConsorcioRepository.observarPropietario(propietarioId).first()

            combine(
                repository.observarVotacion(votacionId),
                repository.observarVotoPropietario(votacionId, propietarioId),
                _opcionElegidaId,
                _confirmandoVoto,
                _mensaje,
                _refrescar
            ) { args ->
                val votacion = args[0] as Votacion?
                val miVoto = args[1] as Voto?
                val opcionId = args[2] as Long?
                val confirmando = args[3] as Boolean
                val msj = args[4] as String?

                if (votacion == null) {
                    UiState.Vacio("Votación no encontrada")
                } else {
                    val resultados = resultadosVotacionUseCase(votacionId)
                    UiState.Contenido(
                        DetalleVotacionState(
                            votacion = votacion,
                            capacidades = capacidades,
                            propietarioActual = propietario,
                            miVoto = miVoto,
                            resultados = resultados,
                            opcionElegidaId = opcionId,
                            confirmandoVoto = confirmando,
                            mostrarMensaje = msj
                        )
                    )
                }
            }.catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Error al cargar detalle de votación")
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun elegirOpcion(opcionId: Long) {
        _opcionElegidaId.value = opcionId
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    fun registrarVoto() {
        val opcionId = _opcionElegidaId.value ?: return
        
        viewModelScope.launch {
            _confirmandoVoto.value = true
            try {
                delay(1200) // Simular comprobación biométrica de identidad

                val propietarioId = sesionRepository.propietarioIdActual.first()
                val propietario = infoConsorcioRepository.observarPropietario(propietarioId).first()

                if (propietario == null) {
                    _mensaje.value = "No se encontró el propietario de la sesión."
                } else {
                    _mensaje.value = when (val result = registrarVotoUseCase(votacionId, opcionId, propietario)) {
                        is RegistrarVotoUseCase.Result.Exito -> {
                            _opcionElegidaId.value = null // Resetea seleccion
                            _refrescar.value += 1 // Forzamos recálculo de resultados en UI Flow
                            "¡Voto registrado con éxito! Comprobante: ${result.comprobante}"
                        }
                        RegistrarVotoUseCase.Result.Error.VotacionInvalida -> "La votación no existe."
                        RegistrarVotoUseCase.Result.Error.VotacionCerrada -> "La votación no está abierta."
                        RegistrarVotoUseCase.Result.Error.OpcionInvalida -> "La opción elegida no pertenece a esta votación."
                        RegistrarVotoUseCase.Result.Error.PropietarioNoHabilitado -> "Tu unidad no pertenece al consorcio de esta votación."
                        RegistrarVotoUseCase.Result.Error.YaVoto -> "Ya registraste tu voto en esta votación. Se conserva tu voto original."
                    }
                }
            } catch (e: SQLException) {
                // Errores de la base de datos (Room usa el SQLite de Android, que lanza
                // subclases de android.database.SQLException). No capturamos Exception en
                // general: así no ocultamos errores de programación ni la cancelación
                // de la corrutina (CancellationException no es una SQLException).
                _mensaje.value = "No se pudo registrar el voto. Intentá nuevamente."
            } finally {
                // Se ejecuta siempre (éxito, error o cancelación): la pantalla nunca queda bloqueada.
                _confirmandoVoto.value = false
            }
        }
    }

    fun eliminarVotacion(onEliminado: () -> Unit) {
        viewModelScope.launch {
            repository.eliminarVotacion(votacionId)
            onEliminado()
        }
    }
}
