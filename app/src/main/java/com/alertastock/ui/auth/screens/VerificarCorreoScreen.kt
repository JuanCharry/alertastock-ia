package com.alertastock.ui.auth.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertastock.ui.theme.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun VerificarCorreoScreen(
    onVerificado: () -> Unit,
    onAtras: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val email = auth.currentUser?.email ?: ""
    var mensaje by remember { mutableStateOf("") }
    var mensajeColor by remember { mutableStateOf(Green) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgScreen)
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

        Spacer(modifier = Modifier.height(32.dp))

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
            text = "Revisa tu correo",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Enviamos un enlace de verificacion a",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        Text(
            text = email,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Blue,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Banner info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Blue.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🕐", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Haz clic en el enlace del correo para activar tu cuenta. Revisa tambien la carpeta de spam",
                    color = Blue,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        // Mensaje
        if (mensaje.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = mensajeColor.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = mensaje,
                    color = mensajeColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Ya verifiqué
        Button(
            onClick = {
                auth.currentUser?.reload()?.addOnCompleteListener { tarea ->
                    if (tarea.isSuccessful) {
                        if (auth.currentUser?.isEmailVerified == true) {
                            onVerificado()
                        } else {
                            mensaje = "Aun no has verificado tu correo"
                            mensajeColor = Yellow
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue)
        ) {
            Text(
                text = "Ya verifique mi correo",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón Reenviar
        OutlinedButton(
            onClick = {
                auth.currentUser?.sendEmailVerification()
                    ?.addOnCompleteListener {
                        mensaje = "Correo reenviado correctamente"
                        mensajeColor = Green
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = BgCard,
                contentColor = TextPrimary
            )
        ) {
            Text(
                text = "Reenviar correo",
                fontSize = 13.sp,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        TextButton(onClick = onAtras) {
            Text(
                text = "¿Correo incorrecto? Cambiar correo",
                color = Blue,
                fontSize = 12.sp
            )
        }
    }
}