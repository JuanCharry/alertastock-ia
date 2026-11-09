package com.alertastock.ui.dashboard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.R
import com.alertastock.ui.components.AlertaStockBottomBar
import com.alertastock.ui.components.BottomNavDestino
import com.alertastock.ui.product.ProductoViewModel
import com.alertastock.ui.ia.IaViewModel
import com.alertastock.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onCerrarSesion: () -> Unit,
    onProductos: () -> Unit = {},
    onProductosCriticos: () -> Unit = {},
    onProductosBajos: () -> Unit = {},
    onProductosPorVencer: () -> Unit = {},
    onProductosBuenEstado: () -> Unit = {},
    onEscanear: () -> Unit = {},
    onAlertas: () -> Unit = {},
    onConfigurar: () -> Unit = {},
    viewModel: ProductoViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val usuario = auth.currentUser
    val nombre = usuario?.displayName?.split(" ")?.firstOrNull()
        ?: usuario?.email?.substringBefore("@")
        ?: "Usuario"

    val todosLosProductos by viewModel.todosLosProductos.observeAsState(emptyList())
    val iaViewModel: IaViewModel = viewModel()
    val estadosIa by iaViewModel.estados.collectAsState()

    val totalProductos = todosLosProductos.size

    // El resumen del inicio usa el mismo resultado de IA que Inventario.
    // Mientras un producto aun no tenga prediccion en esta sesion, se conserva
    // temporalmente la regla manual anterior como respaldo visual.
    val riesgoAlto = todosLosProductos.count { producto ->
        val riesgo = estadosIa[producto.id]?.resultado?.riesgo
        riesgo == "ALTO" || (riesgo == null && producto.stockActual <= producto.stockMinimo)
    }
    val riesgoMedio = todosLosProductos.count { producto ->
        val riesgo = estadosIa[producto.id]?.resultado?.riesgo
        riesgo == "MEDIO" || (
            riesgo == null &&
                producto.stockActual > producto.stockMinimo &&
                producto.stockActual <= producto.stockMinimo * 2
        )
    }
    val riesgoBajo = todosLosProductos.count { producto ->
        val riesgo = estadosIa[producto.id]?.resultado?.riesgo
        riesgo == "BAJO" || (riesgo == null && producto.stockActual > producto.stockMinimo * 2)
    }

    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

    val saludoManana = stringResource(R.string.saludo_manana)
    val saludoTarde = stringResource(R.string.saludo_tarde)
    val saludoNoche = stringResource(R.string.saludo_noche)
    val saludo = when {
        hora < 12 -> saludoManana
        hora < 18 -> saludoTarde
        else      -> saludoNoche
    }

    val formato = SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale("es", "CO"))
    val fecha = formato.format(Date()).replaceFirstChar { it.uppercase() }

    LaunchedEffect(Unit) {
        viewModel.sincronizar()
    }

    // Analiza automaticamente todos los productos visibles en Inicio para que
    // las tarjetas del resumen se actualicen con riesgo ALTO / MEDIO / BAJO.
    LaunchedEffect(todosLosProductos) {
        todosLosProductos.forEach { producto ->
            if (estadosIa[producto.id] == null) {
                iaViewModel.analizar(producto)
            }
        }
    }

    Scaffold(
        containerColor = BgScreen,
        bottomBar = {
            AlertaStockBottomBar(
                destinoActual = BottomNavDestino.INICIO,
                onInicioClick = {},
                onProductosClick = onProductos,
                onEscanearClick = onEscanear,
                onAlertasClick = onAlertas
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgScreen)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(colors = listOf(BlueDark, Blue)))
                    .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$saludo $nombre",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = fecha,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { auth.signOut(); onCerrarSesion() }) {
                                Icon(
                                    Icons.Default.ExitToApp,
                                    contentDescription = stringResource(R.string.cerrar_sesion),
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            // ✅ Avatar clickeable — navega a configuración
                            Card(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(22.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f)),
                                onClick = onConfigurar
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = nombre.first().uppercase(),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // RESUMEN GENERAL
            Text(
                text = stringResource(R.string.dashboard_resumen),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaEstadistica(
                    emoji = "📦",
                    valor = totalProductos.toString(),
                    etiqueta = stringResource(R.string.tarjeta_total),
                    color = Blue,
                    modifier = Modifier.weight(1f),
                    onClick = onProductos
                )
                TarjetaEstadistica(
                    emoji = "⚠️",
                    valor = riesgoAlto.toString(),
                    etiqueta = "Riesgo alto",
                    color = Red,
                    modifier = Modifier.weight(1f),
                    onClick = onProductosCriticos
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TarjetaEstadistica(
                    emoji = "⚠️",
                    valor = riesgoMedio.toString(),
                    etiqueta = "Riesgo medio",
                    color = Color(0xFFFB8C00),
                    modifier = Modifier.weight(1f),
                    onClick = onProductosBajos
                )
                TarjetaEstadistica(
                    emoji = "✅",
                    valor = riesgoBajo.toString(),
                    etiqueta = "Riesgo bajo",
                    color = Green,
                    modifier = Modifier.weight(1f),
                    onClick = onProductosBuenEstado
                )
            }

            // ACCESO RÁPIDO
            Text(
                text = stringResource(R.string.dashboard_acceso),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccesoRapido(emoji = "📦", label = stringResource(R.string.acceso_productos), color = Blue, modifier = Modifier.weight(1f), onClick = onProductos)
                AccesoRapido(emoji = "📷", label = stringResource(R.string.acceso_escanear), color = Green, modifier = Modifier.weight(1f), onClick = onEscanear)
                AccesoRapido(emoji = "🔔", label = stringResource(R.string.acceso_alertas), color = Red, modifier = Modifier.weight(1f), onClick = onAlertas)
                AccesoRapido(emoji = "⚙️", label = stringResource(R.string.acceso_configurar), color = Yellow, modifier = Modifier.weight(1f), onClick = onConfigurar)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TarjetaEstadistica(
    emoji: String,
    valor: String,
    etiqueta: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = valor, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Text(text = etiqueta, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
fun AccesoRapido(
    emoji: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = emoji, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}