package com.alertastock.ui.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertastock.ui.components.AlertaStockBottomBar
import com.alertastock.ui.components.BottomNavDestino
import com.alertastock.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ConfiguracionScreen(
    onAtras: () -> Unit,
    onEditarPerfil: () -> Unit = {},
    onCambiarContrasena: () -> Unit = {},
    onNotificaciones: () -> Unit = {},
    onCategorias: () -> Unit = {},
    onCerrarSesion: () -> Unit = {},
    onInicioClick: () -> Unit = {},
    onProductosClick: () -> Unit = {},
    onEscanearClick: () -> Unit = {},
    onAlertasClick: () -> Unit = {}
) {
    val auth = FirebaseAuth.getInstance()
    val usuario = auth.currentUser
    val nombre = usuario?.displayName ?: usuario?.email?.substringBefore("@") ?: "Usuario"
    val correo = usuario?.email ?: ""

    // Iniciales para el avatar
    val iniciales = nombre.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")
        .ifEmpty { usuario?.email?.first()?.uppercase() ?: "U" }

    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    if (mostrarDialogoCerrarSesion) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCerrarSesion = false },
            containerColor = BgCard,
            icon = {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Red.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Red, modifier = Modifier.size(28.dp))
                }
            },
            title = { Text("¿Cerrar Sesión?", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tendrás que volver a iniciar sesión para acceder a tu inventario.", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(colors = CardDefaults.cardColors(containerColor = BgScreen), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Blue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(iniciales, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(nombre, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text(correo, fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { auth.signOut(); mostrarDialogoCerrarSesion = false; onCerrarSesion() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Sí, cerrar sesión", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCerrarSesion = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = BgScreen,
        bottomBar = {
            AlertaStockBottomBar(
                destinoActual = BottomNavDestino.INICIO,
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
            // Header con avatar clickeable
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Blue)
                    .padding(top = 48.dp, bottom = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // ✅ Avatar clickeable con iniciales — navega a editar perfil
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f))
                            .clickable { onEditarPerfil() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = iniciales,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(nombre, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(correo, fontSize = 13.sp, color = Color.White.copy(alpha = 0.75f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SeccionTitulo("MI CUENTA")

            ItemConfiguracion(icono = Icons.Default.Person, iconoColor = Blue, titulo = "Editar perfil", subtitulo = "Nombre, correo, foto", onClick = onEditarPerfil)
            ItemConfiguracion(icono = Icons.Default.Lock, iconoColor = Color(0xFF9C27B0), titulo = "Cambiar contraseña", subtitulo = "Seguridad de la cuenta", onClick = onCambiarContrasena)
            ItemConfiguracion(icono = Icons.Default.Notifications, iconoColor = Yellow, titulo = "Notificaciones", subtitulo = "Push, correo, WhatsApp", onClick = onNotificaciones)

            Spacer(modifier = Modifier.height(12.dp))

            SeccionTitulo("OTROS")

            ItemConfiguracion(icono = Icons.Default.Category, iconoColor = Green, titulo = "Categorías", subtitulo = "Gestionar categorías", onClick = onCategorias)

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { mostrarDialogoCerrarSesion = true },
                colors = CardDefaults.cardColors(containerColor = BgCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Red.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Red, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text("Cerrar sesión", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Red)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SeccionTitulo(texto: String) {
    Text(
        text = texto,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
    )
}

@Composable
fun ItemConfiguracion(
    icono: ImageVector,
    iconoColor: Color,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconoColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = iconoColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(subtitulo, fontSize = 12.sp, color = TextSecondary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}