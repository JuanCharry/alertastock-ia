package com.alertastock.ui.venta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ResumenCompraScreen(
    onVolver: () -> Unit,
    onNuevaVenta: () -> Unit,
    viewModel: VentaViewModel = viewModel()
) {
    val items by viewModel.ultimaVenta.collectAsState()
    val total by viewModel.totalUltimaVenta.collectAsState()

    val formatoPesos = remember {
        NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
            maximumFractionDigits = 0
        }
    }

    val fechaActual = remember {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgScreen)
    ) {
        // Header verde — indica éxito
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green)
                .padding(start = 8.dp, end = 20.dp, top = 48.dp, bottom = 20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onVolver) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Resumen de compra",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Text("✅", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "FECHA: $fechaActual",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Título sección
            item {
                Text(
                    text = "PRODUCTOS COMPRADOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // Lista de items vendidos
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Emoji
                        Text(text = item.emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))

                        // Nombre y código
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.nombre,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = item.codigoBarras,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Cant. ${item.cantidad}",
                                fontSize = 11.sp,
                                color = TextHint
                            )
                        }

                        // Precio
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Blue.copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Precio\n$${formatoPesos.format(item.subtotal)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        // Footer con total y botones
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgCard)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Total pagado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Total pagado:", fontSize = 16.sp, color = TextSecondary)
                Text(
                    text = "$ ${formatoPesos.format(total)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Mensaje divertido
            Text(
                text = "Profe... ¿esto ya es un 5? 😅📚",
                fontSize = 12.sp,
                color = Yellow,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Botón nueva venta
            Button(
                onClick = onNuevaVenta,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Nueva venta",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón volver al dashboard
            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("Volver al inicio", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}