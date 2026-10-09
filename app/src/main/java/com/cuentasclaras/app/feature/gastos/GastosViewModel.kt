package com.cuentasclaras.app.feature.gastos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.data.session.SesionRepository
import com.cuentasclaras.app.domain.model.ConceptoAdministrativo
import com.cuentasclaras.app.domain.model.Gasto
import com.cuentasclaras.app.domain.repository.ConceptoRepository
import com.cuentasclaras.app.domain.repository.GastoRepository
import com.cuentasclaras.app.domain.repository.InfoConsorcioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class GastosScreenState(
    val periodoActual: YearMonth = YearMonth.now(),
    val pestanaActiva: Int = 0, // 0: Ordinarias, 1: Extraordinarias, 2: Fondo Reserva, 3: Impuestos
    val gastos: List<Gasto> = emptyList(),
    val conceptos: List<ConceptoAdministrativo> = emptyList(),
    val topeMensualCentavos: Long = 0L,
    val filtroRubro: String = "Todos",
    val busqueda: String = "",
    val puedeGestionarGastos: Boolean = false
)

@HiltViewModel
class GastosViewModel @Inject constructor(
    private val gastoRepository: GastoRepository,
    private val conceptoRepository: ConceptoRepository,
    private val infoConsorcioRepository: InfoConsorcioRepository,
    private val sesionRepository: SesionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<GastosScreenState>>(UiState.Cargando)
    val uiState: StateFlow<UiState<GastosScreenState>> = _uiState.asStateFlow()

    private val periodoActual = MutableStateFlow(YearMonth.now())
    private val pestanaActiva = MutableStateFlow(0)
    private val filtroRubro = MutableStateFlow("Todos")
    private val busqueda = MutableStateFlow("")

    init {
        viewModelScope.launch {
            val consorcioId = sesionRepository.consorcioIdActual.first()
            val capacidades = sesionRepository.capacidadesActuales.first()

            combine(
                periodoActual,
                pestanaActiva,
                filtroRubro,
                busqueda,
                gastoRepository.observarGastos(consorcioId),
                conceptoRepository.observarConceptos(consorcioId),
                infoConsorcioRepository.observarTopeMensual(consorcioId, YearMonth.now()) // simplificado para fase 4
            ) { args ->
                val periodo = args[0] as YearMonth
                val pestana = args[1] as Int
                val rubro = args[2] as String
                val q = args[3] as String
                @Suppress("UNCHECKED_CAST")
                val gastosTodos = args[4] as List<Gasto>
                @Suppress("UNCHECKED_CAST")
                val conceptosTodos = args[5] as List<ConceptoAdministrativo>
                val tope = args[6] as com.cuentasclaras.app.domain.model.TopeMensual?
                
                val gastosDelPeriodo = gastosTodos.filter { YearMonth.from(it.fecha) == periodo }
                val conceptosDelPeriodo = conceptosTodos.filter { it.periodo == periodo }

                // Filtrar según pestaña
                val gastosFiltradosPestana = when (pestana) {
                    0 -> gastosDelPeriodo.filter { it.tipo == com.cuentasclaras.app.domain.model.TipoGasto.ORDINARIA }
                    1 -> gastosDelPeriodo.filter { it.tipo == com.cuentasclaras.app.domain.model.TipoGasto.EXTRAORDINARIA }
                    2 -> gastosDelPeriodo.filter { it.tipo == com.cuentasclaras.app.domain.model.TipoGasto.FONDO_RESERVA }
                    else -> emptyList()
                }

                // Filtrar según Rubro (solo aplica a gastos)
                val gastosRubro = if (rubro == "Todos") {
                    gastosFiltradosPestana
                } else {
                    gastosFiltradosPestana.filter { it.rubro.name.equals(rubro, ignoreCase = true) }
                }

                // Filtrar búsqueda
                val gastosBusqueda = if (q.isBlank()) gastosRubro else gastosRubro.filter {
                    it.titulo.contains(q, ignoreCase = true) || it.proveedor.contains(q, ignoreCase = true)
                }

                val conceptosBusqueda = if (q.isBlank()) conceptosDelPeriodo else conceptosDelPeriodo.filter {
                    it.nombre.contains(q, ignoreCase = true) || it.organismo.contains(q, ignoreCase = true)
                }

                GastosScreenState(
                    periodoActual = periodo,
                    pestanaActiva = pestana,
                    gastos = gastosBusqueda,
                    conceptos = conceptosBusqueda,
                    topeMensualCentavos = tope?.topeCentavos ?: 0L,
                    filtroRubro = rubro,
                    busqueda = q,
                    puedeGestionarGastos = capacidades.puedeGestionarGastos
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
        filtroRubro.value = "Todos" // Reset rubro al cambiar pestaña
    }

    fun cambiarPeriodo(nuevoPeriodo: YearMonth) {
        periodoActual.value = nuevoPeriodo
    }

    fun buscar(query: String) {
        busqueda.value = query
    }

    fun cambiarRubro(rubro: String) {
        filtroRubro.value = rubro
    }
}
