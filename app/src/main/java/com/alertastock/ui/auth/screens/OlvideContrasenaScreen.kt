package com.alertastock.ui.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.ui.auth.AuthEstado
import com.alertastock.ui.auth.AuthViewModel
import com.alertastock.ui.theme.*

@Composable
fun OlvideContrasenaScreen(
    onCorreoEnviado: () -> Unit,
    onAtras: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var correoEnviado by remember { mutableStateOf(false) }

    val estado by viewModel.estado.observeAsState()

    LaunchedEffect(estado) {
        if (estado is AuthEstado.Exitoso) {
            correoEnviado = true
            viewModel.resetearEstado()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgScreen)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Botón atrás
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onAtras) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Atras",
                    tint = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Ícono
        Card(
            modifier = Modifier.size(88.dp),
            shape = RoundedCornerShape(44.dp),
            colors = CardDefaults.cardColors(
                containerColor = Blue.copy(alpha = 0.15f)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = Blue,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Olvidaste tu contrasena",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Ingresa tu correo y te enviaremos un enlace para restablecer tu contrasena",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electronico") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = TextHint)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue,
                unfocusedBorderColor = BorderSoft,
                focusedLabelColor = Blue,
                unfocusedLabelColor = TextHint,
                cursorColor = Blue,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = BgInput,
                unfocusedContainerColor = BgInput
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Te enviaremos un correo para restablecer tu contrasena",
            fontSize = 12.sp,
            color = TextHint,
            textAlign = TextAlign.Center
        )

        // Mensaje éxito
        if (correoEnviado) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Green.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "✓ Correo enviado. Revisa tu bandeja de entrada",
                    color = Green,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                onCorreoEnviado()
            }
        }

        // Error
        if (estado is AuthEstado.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Red.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = (estado as AuthEstado.Error).mensaje,
                    color = Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Botón Enviar
        Button(
            onClick = { viewModel.recuperarContrasena(email) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            enabled = estado !is AuthEstado.Cargando && !correoEnviado
        ) {
            if (estado is AuthEstado.Cargando) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Enviar correo",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}