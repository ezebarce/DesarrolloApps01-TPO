package com.cuentasclaras.app.feature.gastos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentasclaras.app.core.designsystem.components.*
import com.cuentasclaras.app.core.designsystem.theme.CuentasClarasTheme
import com.cuentasclaras.app.core.designsystem.theme.Typography
import com.cuentasclaras.app.core.designsystem.theme.monetaryTable
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.core.util.FormateadorFecha
import com.cuentasclaras.app.core.util.FormateadorMoneda
import com.cuentasclaras.app.domain.model.ConceptoAdministrativo
import com.cuentasclaras.app.domain.model.EstadoConcepto
import com.cuentasclaras.app.domain.model.Gasto
import java.time.YearMonth

@Composable
fun GastosScreen(
    onNavigateToDetalleGasto: (Long) -> Unit,
    onNavigateToFormularioConcepto: (Long?) -> Unit,
    onNavigateToFormularioGasto: () -> Unit,
    viewModel: GastosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CcTopBarMain(
                onAvatarClick = { /* TODO: Navegar a perfil */ }
            )
        },
        floatingActionButton = {
            if (uiState is UiState.Contenido) {
                val state = (uiState as UiState.Contenido<GastosScreenState>).datos
                if (state.puedeGestionarGastos) {
                    FloatingActionButton(
                        onClick = { 
                            if (state.pestanaActiva == 3) {
                                onNavigateToFormularioConcepto(null)
                            } else {
                                onNavigateToFormularioGasto()
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nuevo")
                    }
                }
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Cargando -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(state.mensaje)
                }
            }
            is UiState.Vacio -> { /* No aplica por la arquitectura del StateFlow en este caso */ }
            is UiState.Contenido -> {
                GastosContent(
                    state = state.datos,
                    modifier = Modifier.padding(innerPadding),
                    onPestanaChange = viewModel::cambiarPestana,
                    onPeriodoChange = viewModel::cambiarPeriodo,
                    onBusquedaChange = viewModel::buscar,
                    onRubroChange = viewModel::cambiarRubro,
                    onGastoClick = onNavigateToDetalleGasto,
                    onConceptoClick = onNavigateToFormularioConcepto
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GastosContent(
    state: GastosScreenState,
    modifier: Modifier = Modifier,
    onPestanaChange: (Int) -> Unit,
    onPeriodoChange: (YearMonth) -> Unit,
    onBusquedaChange: (String) -> Unit,
    onRubroChange: (String) -> Unit,
    onGastoClick: (Long) -> Unit,
    onConceptoClick: (Long) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Pestañas
        ScrollableTabRow(
            selectedTabIndex = state.pestanaActiva,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            divider = {}
        ) {
            val tabs = listOf("Ordinarias", "Extraordinarias", "Fondo Reserva", "Impuestos")
            tabs.forEachIndexed { index, title ->
                val count = if (index == 3) state.conceptos.size else if (state.pestanaActiva == index) state.gastos.size else 0
                Tab(
                    selected = state.pestanaActiva == index,
                    onClick = { onPestanaChange(index) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, style = Typography.labelMedium)
                            if (count > 0 && state.pestanaActiva == index) {
                                Surface(
                                    modifier = Modifier.padding(start = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = count.toString(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = Typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TarjetaResumenPeriodo(
                    periodo = state.periodoActual,
                    totalGastos = state.gastos.sumOf { it.montoCentavos },
                    topeCentavos = state.topeMensualCentavos,
                    onPrevMonth = { onPeriodoChange(state.periodoActual.minusMonths(1)) },
                    onNextMonth = { onPeriodoChange(state.periodoActual.plusMonths(1)) }
                )
            }

            item {
                OutlinedTextField(
                    value = state.busqueda,
                    onValueChange = onBusquedaChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar por proveedor o concepto…", style = Typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            if (state.pestanaActiva != 3) {
                item {
                    val rubros = listOf("Todos", "MANTENIMIENTO", "PERSONAL", "SERVICIOS", "ELECTRICIDAD", "HIGIENE", "OTROS")
                    FilaChipsFiltro(
                        opciones = rubros,
                        opcionSeleccionada = state.filtroRubro,
                        onOpcionSeleccionada = onRubroChange,
                        modifier = Modifier.padding(horizontal = 0.dp) // Anular padding del componente general
                    )
                }

                items(state.gastos, key = { it.id }) { gasto ->
                    ItemGasto(gasto, onClick = { onGastoClick(gasto.id) })
                }
            } else {
                items(state.conceptos, key = { it.id }) { concepto ->
                    ItemConcepto(concepto, onClick = { onConceptoClick(concepto.id) })
                }
            }

            if ((state.pestanaActiva != 3 && state.gastos.isEmpty()) || (state.pestanaActiva == 3 && state.conceptos.isEmpty())) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron registros.", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun TarjetaResumenPeriodo(
    periodo: YearMonth,
    totalGastos: Long,
    topeCentavos: Long,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    CcCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
            }
            Text(
                text = FormateadorFecha.formatearMesAnio(periodo),
                style = Typography.titleMedium
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("TOTAL EJECUTADO AUDITADO", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        MontoHeroText(montoCentavos = totalGastos)

        if (topeCentavos > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            val porcentaje = if (topeCentavos > 0) (totalGastos.toDouble() / topeCentavos.toDouble()) else 0.0
            val colorDesvio = if (porcentaje > 1.0) MaterialTheme.colorScheme.errorContainer else CuentasClarasTheme.extendedColors.tertiaryFixed.copy(alpha = 0.15f)
            val textColorDesvio = if (porcentaje > 1.0) MaterialTheme.colorScheme.onErrorContainer else CuentasClarasTheme.extendedColors.onTertiaryFixed

            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(8.dp)).padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Desvío vs. presupuesto", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Tope asignado: ${FormateadorMoneda.formatearCentavosSinDecimales(topeCentavos)}", style = Typography.bodySmall)
                }
                Box(modifier = Modifier.background(colorDesvio, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(FormateadorMoneda.formatearPorcentaje(porcentaje), style = Typography.labelMedium, color = textColorDesvio)
                }
            }
        }
    }
}

@Composable
fun ItemGasto(gasto: Gasto, onClick: () -> Unit) {
    CcCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Text(gasto.rubro.name, style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = gasto.titulo,
                style = Typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Text(
                text = FormateadorMoneda.formatearCentavos(gasto.montoCentavos),
                style = monetaryTable
            )
        }
        Text("${FormateadorFecha.formatearCorta(gasto.fecha)} · ${gasto.proveedor}", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Evaluamos desvio o estado
            val desvioInfo = if (gasto.referenciaMercadoCentavos != null) {
                val diff = (gasto.montoCentavos - gasto.referenciaMercadoCentavos).toDouble() / gasto.referenciaMercadoCentavos
                when {
                    diff > 0.10 -> Pair("+" + FormateadorMoneda.formatearPorcentaje(diff), BadgeAuditoriaTipo.DESVIO)
                    diff < -0.10 -> Pair("-" + FormateadorMoneda.formatearPorcentaje(kotlin.math.abs(diff)), BadgeAuditoriaTipo.VERIFICADO)
                    else -> Pair("En precio de mercado", BadgeAuditoriaTipo.VERIFICADO)
                }
            } else if (gasto.requiereLicitacion) {
                Pair("Licitación", BadgeAuditoriaTipo.NEUTRO)
            } else {
                Pair("Comprobante adjunto", BadgeAuditoriaTipo.NEUTRO)
            }

            BadgeAuditoria(texto = desvioInfo.first, tipo = desvioInfo.second)
            
            Text(
                text = if (desvioInfo.second == BadgeAuditoriaTipo.DESVIO) "Ver justificación" else "Comprobante",
                style = Typography.labelMedium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.clickable { onClick() }
            )
        }
    }
}

@Composable
fun ItemConcepto(concepto: ConceptoAdministrativo, onClick: () -> Unit) {
    CcCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Text(concepto.categoria.name, style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = concepto.nombre,
                style = Typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            Text(
                text = FormateadorMoneda.formatearCentavos(concepto.montoCentavos),
                style = monetaryTable
            )
        }
        Text(concepto.organismo, style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(FormateadorFecha.formatearVencimiento(concepto.vencimiento), style = Typography.bodySmall)
            
            val (badgeText, badgeType) = when (concepto.estado) {
                EstadoConcepto.PAGADO -> Pair("Pagado", BadgeAuditoriaTipo.VERIFICADO)
                EstadoConcepto.PENDIENTE -> Pair("Pendiente", BadgeAuditoriaTipo.NEUTRO)
                EstadoConcepto.VENCIDO -> Pair("Vencido", BadgeAuditoriaTipo.DESVIO)
            }
            BadgeAuditoria(texto = badgeText, tipo = badgeType)
        }
    }
}
