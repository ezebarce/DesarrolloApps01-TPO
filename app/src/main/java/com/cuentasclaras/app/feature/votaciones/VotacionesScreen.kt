package com.cuentasclaras.app.feature.votaciones

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.core.util.FormateadorFecha
import com.cuentasclaras.app.core.util.FormateadorMoneda
import com.cuentasclaras.app.domain.model.EstadoVotacion
import com.cuentasclaras.app.domain.model.TipoAsamblea
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun VotacionesScreen(
    onNavigateToDetalleVotacion: (Long) -> Unit,
    onNavigateToFormularioVotacion: (Long?) -> Unit,
    viewModel: VotacionesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CcTopBarMain(onAvatarClick = { /* TODO: Navegar a perfil */ })
        },
        floatingActionButton = {
            if (uiState is UiState.Contenido) {
                val state = (uiState as UiState.Contenido<VotacionesScreenState>).datos
                if (state.puedeGestionarVotaciones) {
                    FloatingActionButton(
                        onClick = { onNavigateToFormularioVotacion(null) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva")
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
            is UiState.Vacio -> {}
            is UiState.Contenido -> {
                VotacionesContent(
                    state = state.datos,
                    modifier = Modifier.padding(innerPadding),
                    onPestanaChange = viewModel::cambiarPestana,
                    onVotacionClick = onNavigateToDetalleVotacion
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VotacionesContent(
    state: VotacionesScreenState,
    modifier: Modifier = Modifier,
    onPestanaChange: (Int) -> Unit,
    onVotacionClick: (Long) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Gobernanza y Asambleas", style = Typography.headlineMedium)
            state.propietarioActual?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        "UF ${it.unidad} · Coef. ${FormateadorMoneda.formatearPorcentaje(it.coeficiente)}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = Typography.labelMedium
                    )
                }
            }
        }

        // Control Segmentado / Pestañas
        TabRow(
            selectedTabIndex = state.pestanaActiva,
            containerColor = MaterialTheme.colorScheme.surface,
            divider = {}
        ) {
            val tabs = listOf("Activas", "Próximas", "Finalizadas")
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = state.pestanaActiva == index,
                    onClick = { onPestanaChange(index) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(title, style = Typography.labelMedium)
                            if (index == 0 && state.cantidadActivasSinVotar > 0) {
                                Surface(
                                    modifier = Modifier.padding(start = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.error
                                ) {
                                    Text(
                                        text = state.cantidadActivasSinVotar.toString(),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = Typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onError
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
            items(state.votaciones, key = { it.votacion.id }) { resumen ->
                ItemVotacion(resumen, onClick = { onVotacionClick(resumen.votacion.id) })
            }

            if (state.votaciones.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay votaciones en esta categoría.", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (state.pestanaActiva == 0 && state.historialResoluciones.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Historial de Resoluciones", style = Typography.headlineSmall)
                        TextButton(onClick = { onPestanaChange(2) }) {
                            Text("Ver todo")
                        }
                    }
                }
                items(state.historialResoluciones, key = { "hist_${it.votacion.id}" }) { resumen ->
                    ItemVotacion(resumen, onClick = { onVotacionClick(resumen.votacion.id) })
                }
            }
        }
    }
}

@Composable
fun ItemVotacion(resumen: VotacionResumen, onClick: () -> Unit) {
    val v = resumen.votacion
    CcCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        val diasFaltantes = Instant.now().until(v.cierre, ChronoUnit.DAYS)
        val faltanPocosDias = diasFaltantes in 0..3

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            when (v.estado) {
                EstadoVotacion.ACTIVA -> {
                    BadgeAuditoria(
                        texto = "Cierra en $diasFaltantes días · ${FormateadorFecha.formatearFechaHoraCierre(v.cierre)}",
                        tipo = if (faltanPocosDias) BadgeAuditoriaTipo.DESVIO else BadgeAuditoriaTipo.NEUTRO
                    )
                }
                EstadoVotacion.PROXIMA -> {
                    BadgeAuditoria(
                        texto = "Abre el ${FormateadorFecha.formatearCorta(v.apertura.atZone(ZoneId.systemDefault()).toLocalDate())}",
                        tipo = BadgeAuditoriaTipo.NEUTRO
                    )
                }
                EstadoVotacion.FINALIZADA -> {
                    if (resumen.sinQuorum) {
                        BadgeAuditoria("Sin quórum", BadgeAuditoriaTipo.NEUTRO)
                    } else if (resumen.porcentajeAprobacion != null) {
                        val aprobada = resumen.porcentajeAprobacion >= 0.5
                        BadgeAuditoria(
                            texto = if (aprobada) "Aprobada ${FormateadorMoneda.formatearPorcentaje(resumen.porcentajeAprobacion)}" else "Rechazada ${FormateadorMoneda.formatearPorcentaje(resumen.porcentajeAprobacion)}",
                            tipo = if (aprobada) BadgeAuditoriaTipo.VERIFICADO else BadgeAuditoriaTipo.DESVIO
                        )
                    }
                }
            }
        }

        Text(
            text = "Asamblea ${v.tipoAsamblea.name.lowercase().replaceFirstChar { it.uppercase() }} #${v.numero}",
            style = Typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(v.titulo, style = Typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)

        Spacer(modifier = Modifier.height(16.dp))

        if (v.estado == EstadoVotacion.ACTIVA) {
            if (resumen.yaVoto) {
                CcTonalButton(text = "Ya votaste", onClick = onClick, modifier = Modifier.fillMaxWidth())
            } else {
                CcPrimaryButton(text = "Votar", onClick = onClick, modifier = Modifier.fillMaxWidth())
            }
        } else {
            CcTonalButton(text = "Ver detalles", onClick = onClick, modifier = Modifier.fillMaxWidth())
        }
    }
}
