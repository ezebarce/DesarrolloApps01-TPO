package com.cuentasclaras.app.feature.alertas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentasclaras.app.core.designsystem.components.*
import com.cuentasclaras.app.core.designsystem.theme.CuentasClarasTheme
import com.cuentasclaras.app.core.designsystem.theme.Typography
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.domain.model.Alerta
import com.cuentasclaras.app.domain.model.PrioridadAlerta
import com.cuentasclaras.app.domain.model.TipoAlerta
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun AlertasScreen(
    onNavigateToFormulario: (Long?) -> Unit,
    onNavigateToGasto: (Long) -> Unit,
    onNavigateToVotacion: (Long) -> Unit,
    viewModel: AlertasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CcTopBarMain(onAvatarClick = { /* TODO: Perfil */ })
        },
        floatingActionButton = {
            if (uiState is UiState.Contenido) {
                val state = (uiState as UiState.Contenido<AlertasScreenState>).datos
                if (state.puedeGestionarAlertas) {
                    FloatingActionButton(
                        onClick = { onNavigateToFormulario(null) },
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva Alerta")
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
                AlertasContent(
                    state = state.datos,
                    modifier = Modifier.padding(innerPadding),
                    onFiltroChange = viewModel::cambiarFiltro,
                    onMarcarTodasLeidas = viewModel::marcarTodasComoLeidas,
                    onMarcarLeida = viewModel::marcarComoLeida,
                    onDelete = viewModel::eliminarAlerta,
                    onEdit = onNavigateToFormulario,
                    onNavigateToGasto = onNavigateToGasto,
                    onNavigateToVotacion = onNavigateToVotacion
                )
            }
        }
    }
}

@Composable
fun AlertasContent(
    state: AlertasScreenState,
    modifier: Modifier = Modifier,
    onFiltroChange: (String) -> Unit,
    onMarcarTodasLeidas: () -> Unit,
    onMarcarLeida: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (Long) -> Unit,
    onNavigateToGasto: (Long) -> Unit,
    onNavigateToVotacion: (Long) -> Unit
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Alertas", style = Typography.headlineMedium)
            if (state.cantidadNoLeidas > 0) {
                Text(
                    text = "Marcar todas como leídas",
                    style = Typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onMarcarTodasLeidas() }
                )
            }
        }

        val chips = listOf("Todas", "No leídas", "Gastos", "Votaciones", "Mantenimiento", "Avisos")
        FilaChipsFiltro(
            opciones = chips,
            opcionSeleccionada = state.filtroActual,
            onOpcionSeleccionada = onFiltroChange
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.alertas, key = { it.id }) { alerta ->
                ItemAlerta(
                    alerta = alerta,
                    puedeGestionar = state.puedeGestionarAlertas,
                    onClick = { 
                        if (!alerta.leida) onMarcarLeida(alerta.id)
                        if (alerta.gastoId != null) onNavigateToGasto(alerta.gastoId)
                        if (alerta.votacionId != null) onNavigateToVotacion(alerta.votacionId)
                    },
                    onEdit = { onEdit(alerta.id) },
                    onDelete = { onDelete(alerta.id) }
                )
            }

            if (state.alertas.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay alertas.", style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ItemAlerta(
    alerta: Alerta,
    puedeGestionar: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    CcCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            // Icono
            val icon = when (alerta.tipo) {
                TipoAlerta.GASTO -> Icons.Default.ReceiptLong
                TipoAlerta.VOTACION -> Icons.Default.HowToVote
                TipoAlerta.MANTENIMIENTO -> Icons.Default.Build
                TipoAlerta.AVISO_GENERAL -> Icons.Default.Info
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(alerta.titulo, style = Typography.titleMedium, modifier = Modifier.weight(1f))
                    if (!alerta.leida) {
                        Box(modifier = Modifier.padding(start = 8.dp, top = 4.dp).size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(alerta.mensaje, style = Typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val horas = ChronoUnit.HOURS.between(alerta.fechaHora, Instant.now())
                    val tiempoStr = if (horas < 24) "Hace $horas h" else "Hace ${horas / 24} días"
                    
                    Text(tiempoStr, style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    if (alerta.prioridad == PrioridadAlerta.ALTA) {
                        Spacer(modifier = Modifier.width(8.dp))
                        BadgeAuditoria("Urgente", BadgeAuditoriaTipo.DESVIO)
                    }
                }
            }

            // Menu
            if (puedeGestionar) {
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp).padding(start = 4.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = null)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Editar") }, onClick = { showMenu = false; onEdit() })
                        DropdownMenuItem(text = { Text("Eliminar") }, onClick = { showMenu = false; onDelete() })
                    }
                }
            }
        }
    }
}
