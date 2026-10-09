package com.cuentasclaras.app.feature.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.data.local.SembradorDeDatos
import com.cuentasclaras.app.data.session.SesionRepository
import com.cuentasclaras.app.domain.model.*
import com.cuentasclaras.app.domain.repository.GastoRepository
import com.cuentasclaras.app.domain.repository.InfoConsorcioRepository
import com.cuentasclaras.app.domain.repository.VotacionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class InicioScreenState(
    val propietario: Propietario?,
    val expensaActual: Expensa?,
    val gastosDelMes: List<Gasto>,
    val ultimoGasto: Gasto?,
    val cantidadFacturas: Int,
    val votacionUrgente: Votacion?,
    val confirmandoReset: Boolean = false
)

@HiltViewModel
class InicioViewModel @Inject constructor(
    private val sesionRepository: SesionRepository,
    private val infoConsorcioRepository: InfoConsorcioRepository,
    private val gastoRepository: GastoRepository,
    private val votacionRepository: VotacionRepository,
    private val sembradorDeDatos: SembradorDeDatos
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<InicioScreenState>>(UiState.Cargando)
    val uiState: StateFlow<UiState<InicioScreenState>> = _uiState.asStateFlow()

    private val _confirmandoReset = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            // Check de sembrado inicial
            sembradorDeDatos.inicializarSiEsNecesario()

            val consorcioId = sesionRepository.consorcioIdActual.first()
            val propietarioId = sesionRepository.propietarioIdActual.first()
            val mesActual = YearMonth.now()

            combine(
                infoConsorcioRepository.observarPropietario(propietarioId),
                infoConsorcioRepository.observarExpensas(propietarioId),
                gastoRepository.observarGastos(consorcioId),
                votacionRepository.observarVotaciones(consorcioId),
                _confirmandoReset
            ) { propietario, expensas, gastos, votaciones, confirmando ->

                val expensa = expensas.firstOrNull() // La más reciente
                val gastosMes = gastos.filter { YearMonth.from(it.fecha) == mesActual }
                val ultimoGasto = gastos.filter { it.estado == EstadoGasto.PAGADO || it.estado == EstadoGasto.COMPROBANTE_FISCAL }.maxByOrNull { it.fecha }
                
                // Votación activa sin voto que cierra primero
                var votacionUrgente: Votacion? = null
                for (v in votaciones.filter { it.estado == EstadoVotacion.ACTIVA }.sortedBy { it.cierre }) {
                    val voto = votacionRepository.observarVotoPropietario(v.id, propietarioId).first()
                    if (voto == null) {
                        votacionUrgente = v
                        break
                    }
                }

                val state = InicioScreenState(
                    propietario = propietario,
                    expensaActual = expensa,
                    gastosDelMes = gastosMes,
                    ultimoGasto = ultimoGasto,
                    cantidadFacturas = gastosMes.count { it.tipoComprobante != TipoComprobante.TRANSFERENCIA && it.tipoComprobante != TipoComprobante.RECIBO },
                    votacionUrgente = votacionUrgente,
                    confirmandoReset = confirmando
                )
                UiState.Contenido(state)
            }
            .catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Error desconocido en Inicio")
            }
            .collect { state ->
                _uiState.value = state
            }
        }
    }

    fun solicitarResetDatos() {
        _confirmandoReset.value = true
    }

    fun cancelarResetDatos() {
        _confirmandoReset.value = false
    }

    fun confirmarResetDatos() {
        viewModelScope.launch {
            _confirmandoReset.value = false
            _uiState.value = UiState.Cargando
            sembradorDeDatos.restablecerDatosDeEjemplo()
            // El combine del init volverá a emitir cuando los DAOs notifiquen
        }
    }
}
