package com.alertastock.ui.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertastock.ui.components.AlertaStockBottomBar
import com.alertastock.ui.components.BottomNavDestino
import com.alertastock.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging

@Composable
fun NotificacionesScreen(
    onAtras: () -> Unit,
    onInicioClick: () -> Unit = {},
    onProductosClick: () -> Unit = {},
    onEscanearClick: () -> Unit = {},
    onAlertasClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    // Estados de los toggles — MI CUENTA
    var notifPush by remember { mutableStateOf(true) }
    var notifCorreo by remember { mutableStateOf(true) }
    var notifWhatsapp by remember { mutableStateOf(false) }

    // Estados de los toggles — MI NEGOCIO
    var notifStockCritico by remember { mutableStateOf(true) }
    var notifPorVencer by remember { mutableStateOf(true) }
    var notifResumenDiario by remember { mutableStateOf(false) }

    // Suscribir/desuscribir de tópicos de Firebase Messaging
    fun manejarSuscripcion(topico: String, activo: Boolean) {
        if (activo) {
            FirebaseMessaging.getInstance().subscribeToTopic(topico)
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topico)
        }
    }

    Scaffold(
        containerColor = BgScreen,
        bottomBar = {
            AlertaStockBottomBar(
                destinoActual = BottomNavDestino.ALERTAS,
                onInicioClick = onInicioClick,
                onProductosClick = onProductosClick,
                onEscanearClick = onEscanearClick,
                onAlertasClick = onAlertasClick
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
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgCard)
                    .padding(start = 8.dp, end = 20.dp, top = 48.dp, bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAtras) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = TextPrimary)
                    }
                    Text("Notificaciones", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección MI CUENTA
            SeccionTitulo("MI CUENTA")

            ItemNotificacion(
                icono = Icons.Default.Notifications,
                iconoColor = Blue,
                titulo = "Notificaciones push",
                subtitulo = "En el dispositivo",
                activo = notifPush,
                onCambiar = {
                    notifPush = it
                    manejarSuscripcion("push_general", it)
                }
            )

            ItemNotificacion(
                icono = Icons.Default.Email,
                iconoColor = Green,
                titulo = "Correo electrónico",
                subtitulo = auth.currentUser?.email ?: "No configurado",
                activo = notifCorreo,
                onCambiar = { notifCorreo = it }
            )

            ItemNotificacion(
                icono = Icons.Default.Chat,
                iconoColor = Color(0xFF25D366),
                titulo = "WhatsApp",
                subtitulo = "No configurado",
                activo = notifWhatsapp,
                onCambiar = { notifWhatsapp = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sección MI NEGOCIO
            SeccionTitulo("MI NEGOCIO")

            ItemNotificacion(
                icono = Icons.Default.Warning,
                iconoColor = Red,
                titulo = "Stock crítico",
                subtitulo = "Bajo el mínimo",
                activo = notifStockCritico,
                onCambiar = {
                    notifStockCritico = it
                    manejarSuscripcion("stock_critico", it)
                }
            )

            ItemNotificacion(
                icono = Icons.Default.DateRange,
                iconoColor = Yellow,
                titulo = "Próximo a vencer",
                subtitulo = "7 días antes",
                activo = notifPorVencer,
                onCambiar = {
                    notifPorVencer = it
                    manejarSuscripcion("por_vencer", it)
                }
            )

            ItemNotificacion(
                icono = Icons.Default.Assessment,
                iconoColor = Color(0xFFFB8C00),
                titulo = "Resumen diario",
                subtitulo = "Cada mañana a las 8 am",
                activo = notifResumenDiario,
                onCambiar = {
                    notifResumenDiario = it
                    manejarSuscripcion("resumen_diario", it)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Item de notificación con toggle ───────────────────────────────────────────
@Composable
fun ItemNotificacion(
    icono: ImageVector,
    iconoColor: Color,
    titulo: String,
    subtitulo: String,
    activo: Boolean,
    onCambiar: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconoColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = iconoColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(subtitulo, fontSize = 12.sp, color = TextSecondary)
            }
            Switch(
                checked = activo,
                onCheckedChange = onCambiar,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Blue,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = BorderMedium
                )
            )
        }
    }
}