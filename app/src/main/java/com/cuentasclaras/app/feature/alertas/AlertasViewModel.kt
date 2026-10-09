package com.cuentasclaras.app.feature.alertas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.data.session.SesionRepository
import com.cuentasclaras.app.domain.model.Alerta
import com.cuentasclaras.app.domain.repository.AlertaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlertasScreenState(
    val filtroActual: String = "Todas",
    val alertas: List<Alerta> = emptyList(),
    val cantidadNoLeidas: Int = 0,
    val puedeGestionarAlertas: Boolean = false
)

@HiltViewModel
class AlertasViewModel @Inject constructor(
    private val alertaRepository: AlertaRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<AlertasScreenState>>(UiState.Cargando)
    val uiState: StateFlow<UiState<AlertasScreenState>> = _uiState.asStateFlow()

    private val filtroActual = MutableStateFlow("Todas")

    init {
        viewModelScope.launch {
            val consorcioId = sesionRepository.consorcioIdActual.first()
            val capacidades = sesionRepository.capacidadesActuales.first()

            combine(
                filtroActual,
                alertaRepository.observarAlertas(consorcioId)
            ) { filtro, alertasTodas ->
                
                val alertasFiltradas = when (filtro) {
                    "Todas" -> alertasTodas
                    "No leídas" -> alertasTodas.filter { !it.leida }
                    "Gastos" -> alertasTodas.filter { it.tipo == com.cuentasclaras.app.domain.model.TipoAlerta.GASTO }
                    "Votaciones" -> alertasTodas.filter { it.tipo == com.cuentasclaras.app.domain.model.TipoAlerta.VOTACION }
                    "Mantenimiento" -> alertasTodas.filter { it.tipo == com.cuentasclaras.app.domain.model.TipoAlerta.MANTENIMIENTO }
                    "Avisos" -> alertasTodas.filter { it.tipo == com.cuentasclaras.app.domain.model.TipoAlerta.AVISO_GENERAL }
                    else -> alertasTodas
                }

                val noLeidas = alertasTodas.count { !it.leida }

                AlertasScreenState(
                    filtroActual = filtro,
                    alertas = alertasFiltradas,
                    cantidadNoLeidas = noLeidas,
                    puedeGestionarAlertas = capacidades.puedeGestionarAlertas
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

    fun cambiarFiltro(nuevoFiltro: String) {
        filtroActual.value = nuevoFiltro
    }

    fun marcarComoLeida(alertaId: Long) {
        viewModelScope.launch {
            alertaRepository.marcarComoLeida(alertaId)
        }
    }

    fun marcarTodasComoLeidas() {
        viewModelScope.launch {
            val consorcioId = sesionRepository.consorcioIdActual.first()
            alertaRepository.marcarTodasComoLeidas(consorcioId)
        }
    }

    fun eliminarAlerta(alertaId: Long) {
        viewModelScope.launch {
            alertaRepository.eliminarAlerta(alertaId)
        }
    }
}
