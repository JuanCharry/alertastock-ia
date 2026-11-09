package com.alertastock.ui.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.R
import com.alertastock.ui.auth.AuthError
import com.alertastock.ui.auth.AuthEstado
import com.alertastock.ui.auth.AuthViewModel
import com.alertastock.ui.theme.*

@Composable
fun RegistroScreen(
    onRegistroExitoso: () -> Unit,
    onAtras: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var nombre by remember { mutableStateOf("") }
    var negocio by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var mostrarContrasena by remember { mutableStateOf(false) }

    val estado by viewModel.estado.observeAsState()

    LaunchedEffect(estado) {
        if (estado is AuthEstado.Exitoso) {
            onRegistroExitoso()
            viewModel.resetearEstado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgScreen)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = onAtras) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Atras", tint = TextPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Crear cuenta", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

        Text(text = "Ingresa tus datos para registrarte", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(28.dp))

        // Campo Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TextHint) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposColores()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Campo Negocio
        OutlinedTextField(
            value = negocio,
            onValueChange = { negocio = it },
            label = { Text("Nombre del negocio") },
            leadingIcon = { Icon(Icons.Default.Store, contentDescription = null, tint = TextHint) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposColores()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electronico") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TextHint) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposColores()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = contrasena,
            onValueChange = { contrasena = it },
            label = { Text("Contrasena") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TextHint) },
            trailingIcon = {
                IconButton(onClick = { mostrarContrasena = !mostrarContrasena }) {
                    Icon(
                        if (mostrarContrasena) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null, tint = TextHint
                    )
                }
            },
            visualTransformation = if (mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposColores()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Indicador fortaleza
        val fortaleza = when {
            contrasena.length >= 8 && contrasena.any { it.isUpperCase() } && contrasena.any { it.isDigit() } -> 3
            contrasena.length >= 6 -> 2
            contrasena.isNotEmpty() -> 1
            else -> 0
        }
        val colorFortaleza = when (fortaleza) { 3 -> Green; 2 -> Yellow; 1 -> Red; else -> BorderMedium }
        val textoFortaleza = when (fortaleza) { 3 -> "Contrasena fuerte"; 2 -> "Contrasena media"; 1 -> "Contrasena debil"; else -> "Ingresa una contrasena" }

        LinearProgressIndicator(
            progress = { fortaleza / 3f },
            modifier = Modifier.fillMaxWidth(),
            color = colorFortaleza,
            trackColor = BorderMedium
        )

        Text(text = textoFortaleza, color = colorFortaleza, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Al registrarte aceptas los Terminos y condiciones", color = TextSecondary, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(20.dp))

        // Botón Registrarse
        Button(
            onClick = { viewModel.registrar(nombre, email, contrasena) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green),
            enabled = estado !is AuthEstado.Cargando
        ) {
            if (estado is AuthEstado.Cargando) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "Crear cuenta", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // Error
        if (estado is AuthEstado.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = mensajeDeError((estado as AuthEstado.Error).mensaje),
                    color = Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
