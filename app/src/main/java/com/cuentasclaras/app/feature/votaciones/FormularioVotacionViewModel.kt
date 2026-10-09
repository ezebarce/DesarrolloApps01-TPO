package com.cuentasclaras.app.feature.votaciones

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.cuentasclaras.app.core.navigation.Screen
import com.cuentasclaras.app.domain.model.OpcionVoto
import com.cuentasclaras.app.domain.model.TipoAsamblea
import com.cuentasclaras.app.domain.model.TipoOpcion
import com.cuentasclaras.app.domain.model.Votacion
import com.cuentasclaras.app.domain.repository.VotacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class FormularioVotacionState(
    val id: Long? = null,
    val numeroTexto: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val tipoAsamblea: TipoAsamblea = TipoAsamblea.ORDINARIA,
    val apertura: LocalDate = LocalDate.now(),
    val cierre: LocalDate = LocalDate.now().plusDays(15),
    val quorumRequeridoTexto: String = "50.0",
    val proveedorRecomendado: String = "",
    val montoText: String = "",
    val opciones: List<OpcionVotoState> = listOf(
        OpcionVotoState(titulo = "Aprobar", tipo = TipoOpcion.A_FAVOR, recomendada = true),
        OpcionVotoState(titulo = "Rechazar", tipo = TipoOpcion.EN_CONTRA, recomendada = false),
        OpcionVotoState(titulo = "Abstención", tipo = TipoOpcion.ABSTENCION, recomendada = false)
    ),
    val isLoading: Boolean = false,
    val error: String? = null,
    val guardadoExitoso: Boolean = false
)

data class OpcionVotoState(
    val id: Long = 0,
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: TipoOpcion = TipoOpcion.A_FAVOR,
    val recomendada: Boolean = false
)

@HiltViewModel
class FormularioVotacionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VotacionRepository
) : ViewModel() {

    private val votacionId = savedStateHandle.toRoute<Screen.FormularioVotacion>().votacionId

    private val _uiState = MutableStateFlow(FormularioVotacionState(id = votacionId))
    val uiState: StateFlow<FormularioVotacionState> = _uiState.asStateFlow()

    private var consorcioIdActual: Long = 1L

    init {
        if (votacionId != null) {
            viewModelScope.launch {
                val v = repository.observarVotacion(votacionId).first()
                if (v != null) {
                    val ops = repository.observarOpciones(votacionId).first()
                    _uiState.update {
                        it.copy(
                            numeroTexto = v.numero.toString(),
                            titulo = v.titulo,
                            descripcion = v.descripcion,
                            tipoAsamblea = v.tipoAsamblea,
                            apertura = v.apertura.atZone(ZoneId.systemDefault()).toLocalDate(),
                            cierre = v.cierre.atZone(ZoneId.systemDefault()).toLocalDate(),
                            quorumRequeridoTexto = v.quorumRequerido.toString(),
                            proveedorRecomendado = v.proveedorRecomendado ?: "",
                            montoText = v.montoCentavos?.let { m -> (m / 100.0).toString() } ?: "",
                            opciones = ops.map { op -> 
                                OpcionVotoState(
                                    id = op.id, 
                                    titulo = op.titulo, 
                                    descripcion = op.descripcion, 
                                    tipo = op.tipo, 
                                    recomendada = op.recomendada
                                ) 
                            }
                        )
                    }
                }
            }
        }
    }

    // Simplificamos los updates para no alargar el código. 
    // En una app real cada campo tendría su mutator.
    fun updateState(transform: (FormularioVotacionState) -> FormularioVotacionState) {
        _uiState.update(transform)
    }

    fun guardar() {
        val state = _uiState.value
        _uiState.update { it.copy(isLoading = true, error = null) }

        if (state.titulo.isBlank() || state.numeroTexto.isBlank()) {
            _uiState.update { it.copy(isLoading = false, error = "Complete los campos obligatorios") }
            return
        }

        if (state.opciones.size < 2) {
            _uiState.update { it.copy(isLoading = false, error = "Debe haber al menos 2 opciones") }
            return
        }

        val montoCentavos = state.montoText.toDoubleOrNull()?.let { (it * 100).toLong() }
        val aperturaInst = state.apertura.atStartOfDay(ZoneId.systemDefault()).toInstant()
        // Cierre al final del día
        val cierreInst = state.cierre.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()

        if (cierreInst.isBefore(aperturaInst)) {
            _uiState.update { it.copy(isLoading = false, error = "La fecha de cierre debe ser posterior a la de apertura") }
            return
        }

        val votacion = Votacion(
            id = state.id ?: 0L,
            consorcioId = consorcioIdActual,
            numero = state.numeroTexto.toIntOrNull() ?: 1,
            titulo = state.titulo,
            descripcion = state.descripcion,
            tipoAsamblea = state.tipoAsamblea,
            apertura = aperturaInst,
            cierre = cierreInst,
            quorumRequerido = state.quorumRequeridoTexto.toDoubleOrNull() ?: 50.0,
            proveedorRecomendado = state.proveedorRecomendado.takeIf { it.isNotBlank() },
            montoCentavos = montoCentavos
        )

        viewModelScope.launch {
            val savedId = repository.guardarVotacion(votacion)
            
            // Re-guardamos opciones. Por simplicidad borramos y creamos (ya que en Formulario no se guardan ids intermedios en la UI simplificada)
            if (state.id != null) {
                repository.eliminarOpcionesDeVotacion(savedId)
            }
            
            state.opciones.forEachIndexed { index, op ->
                repository.guardarOpcion(
                    OpcionVoto(
                        votacionId = savedId,
                        titulo = op.titulo,
                        descripcion = op.descripcion,
                        tipo = op.tipo,
                        recomendada = op.recomendada,
                        orden = index
                    )
                )
            }

            _uiState.update { it.copy(isLoading = false, guardadoExitoso = true) }
        }
    }
}
