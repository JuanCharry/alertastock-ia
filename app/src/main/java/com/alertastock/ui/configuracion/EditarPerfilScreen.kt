package com.alertastock.ui.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertastock.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@Composable
fun EditarPerfilScreen(
    onAtras: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val usuario = auth.currentUser

    var nombre by remember { mutableStateOf(usuario?.displayName ?: "") }
    var telefono by remember { mutableStateOf("") }
    var negocio by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var mensajeExito by remember { mutableStateOf(false) }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    // Iniciales para el avatar
    val iniciales = nombre.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")
        .ifEmpty { usuario?.email?.first()?.uppercase() ?: "U" }

    fun guardar() {
        if (nombre.isBlank()) {
            mensajeError = "El nombre no puede estar vacío"
            return
        }
        cargando = true
        val request = UserProfileChangeRequest.Builder()
            .setDisplayName(nombre.trim())
            .build()
        usuario?.updateProfile(request)
            ?.addOnSuccessListener {
                cargando = false
                mensajeExito = true
                mensajeError = null
            }
            ?.addOnFailureListener { e ->
                cargando = false
                mensajeError = "Error al guardar: ${e.message}"
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
                Text("Editar perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onAtras) {
                    Text("Cancelar", color = Red)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Banner éxito
            if (mensajeExito) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Green.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Perfil actualizado correctamente", color = Green, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Avatar con iniciales
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(Blue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Nombre
            FormLabel("NOMBRE COMPLETO")
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it; mensajeError = null },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tu nombre completo", color = TextHint) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = campoColores()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Correo (solo lectura)
            FormLabel("CORREO ELECTRÓNICO")
            OutlinedTextField(
                value = usuario?.email ?: "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = campoColores()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Teléfono
            FormLabel("TELÉFONO")
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("+57 300 000 0000", color = TextHint) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                colors = campoColores()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre del negocio
            FormLabel("NOMBRE DEL NEGOCIO")
            OutlinedTextField(
                value = negocio,
                onValueChange = { negocio = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tienda La Economía", color = TextHint) },
                leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = campoColores()
            )

            mensajeError?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, color = Red, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { guardar() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue),
                enabled = !cargando
            ) {
                if (cargando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar cambios", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun FormLabel(texto: String) {
    Text(
        text = texto,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 0.8.sp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    )
}

@Composable
fun campoColores() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = BgInput,
    unfocusedContainerColor = BgInput,
    focusedBorderColor = Blue,
    unfocusedBorderColor = BorderMedium,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = Blue
)