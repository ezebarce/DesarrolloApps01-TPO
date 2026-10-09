package com.cuentasclaras.app.feature.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cuentasclaras.app.core.designsystem.components.*
import com.cuentasclaras.app.core.designsystem.theme.CuentasClarasTheme
import com.cuentasclaras.app.core.designsystem.theme.Typography
import com.cuentasclaras.app.core.ui.UiState
import com.cuentasclaras.app.core.util.FormateadorFecha
import com.cuentasclaras.app.core.util.FormateadorMoneda
import com.cuentasclaras.app.domain.model.RubroGasto
import java.time.YearMonth

@Composable
fun InicioScreen(
    onNavigateToGastos: () -> Unit,
    onNavigateToVotacion: (Long) -> Unit,
    onNavigateToDetalleGasto: (Long) -> Unit,
    onNavigateToPerfil: () -> Unit,
    viewModel: InicioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CcTopBarMain(
                onAvatarClick = onNavigateToPerfil
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                InicioContent(
                    state = state.datos,
                    modifier = Modifier.padding(innerPadding),
                    onNavigateToGastos = onNavigateToGastos,
                    onNavigateToVotacion = onNavigateToVotacion,
                    onNavigateToDetalleGasto = onNavigateToDetalleGasto,
                    onShowSnackbar = { /* Snackbar para Reclamos */ }
                )
            }
        }
    }
}

@Composable
fun InicioContent(
    state: InicioScreenState,
    modifier: Modifier = Modifier,
    onNavigateToGastos: () -> Unit,
    onNavigateToVotacion: (Long) -> Unit,
    onNavigateToDetalleGasto: (Long) -> Unit,
    onShowSnackbar: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Saludo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Hola, ${state.propietario?.nombre?.split(" ")?.firstOrNull() ?: ""}", style = Typography.headlineLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        state.propietario?.unidad ?: "",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = Typography.labelMedium
                    )
                }
            }
            if (state.expensaActual?.alDia == true) {
                BadgeAuditoria("Al día", BadgeAuditoriaTipo.VERIFICADO)
            } else {
                BadgeAuditoria("Deuda", BadgeAuditoriaTipo.DESVIO)
            }
        }

        // 2. Banner Votación
        if (state.votacionUrgente != null) {
            CcCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HowToVote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Votación abierta", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(state.votacionUrgente.titulo, style = Typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Button(
                        onClick = { onNavigateToVotacion(state.votacionUrgente.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = CuentasClarasTheme.extendedColors.tertiaryFixed, contentColor = CuentasClarasTheme.extendedColors.onTertiaryFixed)
                    ) {
                        Text("Votar", style = Typography.labelMedium)
                    }
                }
            }
        }

        // 3. Tarjeta Expensas
        state.expensaActual?.let { exp ->
            CcCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Expensas ${FormateadorFecha.formatearMesAnio(exp.periodo)}", style = Typography.headlineSmall)
                    BadgeAuditoria(FormateadorFecha.formatearVencimiento(exp.vencimiento), BadgeAuditoriaTipo.NEUTRO)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    MontoHeroText(montoCentavos = exp.ordinariaCentavos + exp.extraordinariaCentavos)
                    Text("PDF", style = Typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Ordinarias", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormateadorMoneda.formatearCentavos(exp.ordinariaCentavos), style = Typography.bodyMedium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Extraordinaria (${exp.cuotaExtraActual}/${exp.cuotaExtraTotal})", style = Typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(FormateadorMoneda.formatearCentavos(exp.extraordinariaCentavos), style = Typography.bodyMedium)
                    }
                }
            }
        }

        // 4. Accesos Rápidos
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            AccesoRapidoItem("Facturas", Icons.Default.ReceiptLong, state.cantidadFacturas, onClick = onNavigateToGastos)
            AccesoRapidoItem("Reclamo", Icons.Default.Build, null, onClick = onShowSnackbar)
            AccesoRapidoItem("Presupuestos", Icons.Default.RequestQuote, null, onClick = onNavigateToGastos)
        }

        // 5. Gasto Común del Mes
        CcCard(modifier = Modifier.fillMaxWidth()) {
            val total = state.gastosDelMes.sumOf { it.montoCentavos }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Gasto Común del Mes", style = Typography.headlineSmall)
                Text(FormateadorMoneda.formatearCompacto(total), style = Typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            // Calculo de porcentajes para barra
            if (total > 0) {
                val sueldos = state.gastosDelMes.filter { it.rubro == RubroGasto.PERSONAL }.sumOf { it.montoCentavos }
                val mant = state.gastosDelMes.filter { it.rubro == RubroGasto.MANTENIMIENTO }.sumOf { it.montoCentavos }
                val serv = state.gastosDelMes.filter { it.rubro == RubroGasto.SERVICIOS || it.rubro == RubroGasto.ELECTRICIDAD }.sumOf { it.montoCentavos }
                
                val pSueldos = sueldos.toDouble() / total
                val pMant = mant.toDouble() / total
                val pServ = serv.toDouble() / total
                
                BarraSegmentada(
                    porcentajeAFavor = pSueldos,
                    porcentajeEnContra = pMant,
                    porcentajeAbstencion = pServ
                )
                // Reutilizamos BarraSegmentada como placeholder de UI, en una app real habria una variante de 4 colores.
            }
        }

        // 6. Último Gasto Auditado
        state.ultimoGasto?.let { gasto ->
            CcCard(modifier = Modifier.fillMaxWidth(), onClick = { onNavigateToDetalleGasto(gasto.id) }) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = CuentasClarasTheme.extendedColors.tertiaryFixed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Último Gasto Auditado", style = Typography.labelMedium, color = CuentasClarasTheme.extendedColors.tertiaryFixed)
                    }
                    Text("Ver todo", style = Typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(gasto.titulo, style = Typography.headlineSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${gasto.proveedor} · ${gasto.tipoComprobante.name}", style = Typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(FormateadorMoneda.formatearCentavos(gasto.montoCentavos), style = Typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun AccesoRapidoItem(
    texto: String,
    icono: ImageVector,
    badgeCount: Int?,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }.padding(8.dp)) {
        Box {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            if (badgeCount != null && badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(badgeCount.toString(), style = Typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(texto, style = Typography.labelSmall)
    }
}
