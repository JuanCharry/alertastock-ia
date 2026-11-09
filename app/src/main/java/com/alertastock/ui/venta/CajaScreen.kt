package com.alertastock.ui.venta

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.data.model.ItemCanasta
import com.alertastock.ui.theme.*
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CajaScreen(
    onAtras: () -> Unit,
    onEscanearMas: () -> Unit,
    onFinalizarCompra: () -> Unit,
    viewModel: VentaViewModel = viewModel()
) {
    val canasta by viewModel.canasta.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    // Formatea números como moneda colombiana: $3.200
    val formatoPesos = remember {
        NumberFormat.getNumberInstance(Locale("es", "CO")).apply {
            maximumFractionDigits = 0
        }
    }

    Scaffold(
        containerColor = BgScreen,
        topBar = {
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
                    Text(
                        text = "Caja",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    // Contador de items en la canasta
                    if (canasta.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Blue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${canasta.sumOf { it.cantidad }}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Barra inferior con total y botones
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgCard)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Total
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total a pagar:",
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "$ ${formatoPesos.format(viewModel.total)}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Botón finalizar compra
                Button(
                    onClick = {
                        viewModel.finalizarCompra()
                        onFinalizarCompra()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    enabled = canasta.isNotEmpty() && !cargando
                ) {
                    if (cargando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Finalizar compra",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botón agregar otro producto
                OutlinedButton(
                    onClick = onEscanearMas,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue)
                ) {
                    Icon(
                        Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "¿Agregar otro producto?",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        if (canasta.isEmpty()) {
            // Canasta vacía
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "La canasta está vacía",
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Escanea un producto para comenzar",
                        fontSize = 13.sp,
                        color = TextHint
                    )
                }
            }
        } else {
            // Lista de productos en la canasta
            Text(
                text = "PRODUCTOS DE LA CANASTA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 20.dp, top = paddingValues.calculateTopPadding() + 16.dp, bottom = 8.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = paddingValues.calculateTopPadding() + 48.dp,
                    bottom = paddingValues.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(canasta, key = { it.productoId }) { item ->
                    ItemCanastaCard(
                        item = item,
                        formatoPesos = formatoPesos,
                        onAumentar = { viewModel.cambiarCantidad(item, item.cantidad + 1) },
                        onDisminuir = { viewModel.cambiarCantidad(item, item.cantidad - 1) },
                        onEliminar = { viewModel.eliminarDeCanasta(item) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

// ── Tarjeta de item en la canasta ─────────────────────────────────────────────
@Composable
fun ItemCanastaCard(
    item: ItemCanasta,
    formatoPesos: java.text.NumberFormat,
    onAumentar: () -> Unit,
    onDisminuir: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Blue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y código
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1
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

            Spacer(modifier = Modifier.width(8.dp))

            // Precio y controles
            Column(horizontalAlignment = Alignment.End) {
                // Badge precio
                Card(
                    colors = CardDefaults.cardColors(containerColor = Blue.copy(alpha = 0.15f)),
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

                Spacer(modifier = Modifier.height(6.dp))

                // Controles cantidad
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Botón disminuir / eliminar
                    IconButton(
                        onClick = onDisminuir,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            if (item.cantidad == 1) Icons.Default.Delete else Icons.Default.Remove,
                            contentDescription = null,
                            tint = if (item.cantidad == 1) Red else TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${item.cantidad}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    // Botón aumentar
                    IconButton(
                        onClick = onAumentar,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Blue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}