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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertastock.ui.theme.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CambiarContrasenaScreen(
    onAtras: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val usuario = auth.currentUser

    var contrasenaActual by remember { mutableStateOf("") }
    var nuevaContrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }

    var verActual by remember { mutableStateOf(false) }
    var verNueva by remember { mutableStateOf(false) }
    var verConfirmar by remember { mutableStateOf(false) }

    var cargando by remember { mutableStateOf(false) }
    var mensajeExito by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    // Validaciones en tiempo real
    val contrasenasCoindicen = nuevaContrasena == confirmarContrasena && confirmarContrasena.isNotEmpty()
    val contrasenaValida = nuevaContrasena.length >= 6

    fun cambiarContrasena() {
        if (contrasenaActual.isBlank()) {
            mensajeError = "Ingresa tu contraseña actual"
            return
        }
        if (!contrasenaValida) {
            mensajeError = "La nueva contraseña debe tener al menos 6 caracteres"
            return
        }
        if (!contrasenasCoindicen) {
            mensajeError = "Las contraseñas no coinciden"
            return
        }

        cargando = true
        mensajeError = null

        // Re-autenticar al usuario antes de cambiar la contraseña
        val credencial = EmailAuthProvider.getCredential(
            usuario?.email ?: "", contrasenaActual
        )
        usuario?.reauthenticate(credencial)
            ?.addOnSuccessListener {
                // Re-autenticación exitosa — cambiar contraseña
                usuario.updatePassword(nuevaContrasena)
                    .addOnSuccessListener {
                        cargando = false
                        mensajeExito = true
                        contrasenaActual = ""
                        nuevaContrasena = ""
                        confirmarContrasena = ""
                    }
                    .addOnFailureListener { e ->
                        cargando = false
                        mensajeError = "Error al cambiar: ${e.message}"
                    }
            }
            ?.addOnFailureListener {
                cargando = false
                mensajeError = "Contraseña actual incorrecta"
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgScreen)
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
                Text("Cambiar Contraseña", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Banner éxito
            if (mensajeExito) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Green.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cambio de contraseña exitoso", color = Green, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Info de seguridad
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Blue.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Blue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Usa mínimo 6 caracteres con letras y números para mayor seguridad.",
                        color = Blue,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Contraseña actual
            FormLabel("CONTRASEÑA ACTUAL")
            OutlinedTextField(
                value = contrasenaActual,
                onValueChange = { contrasenaActual = it; mensajeError = null },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Digite su contraseña actual", color = TextHint) },
                singleLine = true,
                visualTransformation = if (verActual) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { verActual = !verActual }) {
                        Icon(
                            if (verActual) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = TextHint
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = campoColores()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nueva contraseña
            FormLabel("NUEVA CONTRASEÑA")
            OutlinedTextField(
                value = nuevaContrasena,
                onValueChange = { nuevaContrasena = it; mensajeError = null },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Nueva Contraseña", color = TextHint) },
                singleLine = true,
                visualTransformation = if (verNueva) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { verNueva = !verNueva }) {
                        Icon(
                            if (verNueva) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = TextHint
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = campoColores()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirmar nueva contraseña
            FormLabel("CONFIRMAR NUEVA CONTRASEÑA")
            OutlinedTextField(
                value = confirmarContrasena,
                onValueChange = { confirmarContrasena = it; mensajeError = null },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Confirmar contraseña", color = TextHint) },
                singleLine = true,
                visualTransformation = if (verConfirmar) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { verConfirmar = !verConfirmar }) {
                        Icon(
                            if (verConfirmar) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null, tint = TextHint
                        )
                    }
                },
                isError = confirmarContrasena.isNotEmpty() && !contrasenasCoindicen,
                shape = RoundedCornerShape(12.dp),
                colors = campoColores()
            )

            // Indicador contraseñas coinciden
            if (confirmarContrasena.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (contrasenasCoindicen) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (contrasenasCoindicen) Green else Red,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (contrasenasCoindicen) "Las contraseñas coinciden" else "Las contraseñas no coinciden",
                        fontSize = 11.sp,
                        color = if (contrasenasCoindicen) Green else Red
                    )
                }
            }

            // Error
            mensajeError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Botón actualizar
            Button(
                onClick = { cambiarContrasena() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Actualizar contraseña", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}