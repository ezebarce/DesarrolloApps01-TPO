package com.cuentasclaras.app.feature.votaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.data.session.SesionRepository
import com.cuentasclaras.app.domain.model.EstadoVotacion
import com.cuentasclaras.app.domain.model.Propietario
import com.cuentasclaras.app.domain.model.Votacion
import com.cuentasclaras.app.domain.repository.InfoConsorcioRepository
import com.cuentasclaras.app.domain.repository.VotacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VotacionesScreenState(
    val pestanaActiva: Int = 0, // 0: Activas, 1: Próximas, 2: Finalizadas
    val votaciones: List<VotacionResumen> = emptyList(),
    val historialResoluciones: List<VotacionResumen> = emptyList(),
    val puedeGestionarVotaciones: Boolean = false,
    val propietarioActual: Propietario? = null,
    val cantidadActivasSinVotar: Int = 0
)

data class VotacionResumen(
    val votacion: Votacion,
    val yaVoto: Boolean,
    val porcentajeAprobacion: Double? = null,
    val sinQuorum: Boolean = false
)

@HiltViewModel
class VotacionesViewModel @Inject constructor(
    private val votacionRepository: VotacionRepository,
    private val infoConsorcioRepository: InfoConsorcioRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<VotacionesScreenState>>(UiState.Cargando)
    val uiState: StateFlow<UiState<VotacionesScreenState>> = _uiState.asStateFlow()

    private val pestanaActiva = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            val consorcioId = sesionRepository.consorcioIdActual.first()
            val propietarioId = sesionRepository.propietarioIdActual.first()
            val capacidades = sesionRepository.capacidadesActuales.first()
            
            val propietario = infoConsorcioRepository.observarPropietario(propietarioId).first()

            combine(
                pestanaActiva,
                votacionRepository.observarVotaciones(consorcioId)
            ) { pestana, votacionesTodas ->
                val resumenes = votacionesTodas.map { v ->
                    val voto = votacionRepository.observarVotoPropietario(v.id, propietarioId).first()
                    
                    var pctAprobacion: Double? = null
                    var sinQuorum = false
                    
                    if (v.estado == EstadoVotacion.FINALIZADA) {
                        val votos = votacionRepository.observarVotos(v.id).first()
                        val opciones = votacionRepository.observarOpciones(v.id).first()
                        
                        val quorumAbsoluto = votos.sumOf { it.coeficiente } + opciones.sumOf { it.coeficienteAcumulado }
                        sinQuorum = quorumAbsoluto < v.quorumRequerido
                        
                        if (!sinQuorum && quorumAbsoluto > 0) {
                            val opAprob = opciones.find { it.tipo == com.cuentasclaras.app.domain.model.TipoOpcion.A_FAVOR }
                            if (opAprob != null) {
                                val votosAFavor = votos.filter { it.opcionId == opAprob.id }.sumOf { it.coeficiente }
                                val totalAprob = opAprob.coeficienteAcumulado + votosAFavor
                                pctAprobacion = totalAprob / quorumAbsoluto
                            }
                        }
                    }

                    VotacionResumen(
                        votacion = v,
                        yaVoto = voto != null,
                        porcentajeAprobacion = pctAprobacion,
                        sinQuorum = sinQuorum
                    )
                }

                val activas = resumenes.filter { it.votacion.estado == EstadoVotacion.ACTIVA }
                val proximas = resumenes.filter { it.votacion.estado == EstadoVotacion.PROXIMA }
                val finalizadas = resumenes.filter { it.votacion.estado == EstadoVotacion.FINALIZADA }

                val listaMostrada = when (pestana) {
                    0 -> activas
                    1 -> proximas
                    2 -> finalizadas
                    else -> emptyList()
                }

                VotacionesScreenState(
                    pestanaActiva = pestana,
                    votaciones = listaMostrada.sortedBy { it.votacion.cierre },
                    historialResoluciones = finalizadas.sortedByDescending { it.votacion.cierre }.take(2),
                    puedeGestionarVotaciones = capacidades.puedeGestionarVotaciones,
                    propietarioActual = propietario,
                    cantidadActivasSinVotar = activas.count { !it.yaVoto }
                )
            }
            .catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
            .collect { state ->
                _uiState.value = UiState.Contenido(state)
            }
        }
    }

    fun cambiarPestana(index: Int) {
        pestanaActiva.value = index
    }
}
