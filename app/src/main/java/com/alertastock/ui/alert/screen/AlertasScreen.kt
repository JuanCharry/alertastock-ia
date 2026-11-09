package com.alertastock.ui.alert.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.data.local.database.AlertaStockDatabase
import com.alertastock.data.model.Alerta
import com.alertastock.data.model.Producto
import com.alertastock.data.model.TipoAlerta
import com.alertastock.data.repository.AlertaRepository
import com.alertastock.data.repository.ProductoRepository
import com.alertastock.ui.alert.AlertaUiState
import com.alertastock.ui.alert.AlertaViewModel
import com.alertastock.ui.alert.AlertaViewModelFactory
import com.alertastock.ui.components.AlertaStockBottomBar
import com.alertastock.ui.components.BottomNavDestino
import com.alertastock.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertasScreen(
    onAtras: () -> Unit,
    onInicioClick: () -> Unit = {},
    onProductosClick: () -> Unit = {},
    onEscanearClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { AlertaStockDatabase.getDatabase(context) }
    val productoDao = remember { database.productoDao() }

    val viewModel: AlertaViewModel = viewModel(
        factory = AlertaViewModelFactory(
            alertaRepository = AlertaRepository(),
            productoRepository = ProductoRepository(productoDao)
        )
    )

    val alertas by viewModel.alertas.collectAsState()
    val productos by viewModel.productos.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val alertaEnEdicion by viewModel.alertaEnEdicion.collectAsState()

    var mostrarFormulario by remember { mutableStateOf(false) }
    var mostrarEliminar by remember { mutableStateOf<Alerta?>(null) }
    var expandedProducto by remember { mutableStateOf(false) }
    var expandedTipo by remember { mutableStateOf(false) }

    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var tipoSeleccionado by remember { mutableStateOf(TipoAlerta.STOCK_BAJO) }
    var stockMinimo by remember { mutableStateOf("") }
    var diasPrevios by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var activa by remember { mutableStateOf(true) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(alertaEnEdicion, productos) {
        alertaEnEdicion?.let { alerta ->
            productoSeleccionado = productos.find { it.id.toString() == alerta.productoId }
            tipoSeleccionado = alerta.tipo
            stockMinimo = if (alerta.stockMinimo > 0) alerta.stockMinimo.toString() else ""
            diasPrevios = if (alerta.diasPreviosVencimiento > 0) alerta.diasPreviosVencimiento.toString() else ""
            mensaje = alerta.mensaje
            activa = alerta.activa
            mostrarFormulario = true
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AlertaUiState.Exitoso -> {
                snackbarMessage = (uiState as AlertaUiState.Exitoso).mensaje
                productoSeleccionado = null
                tipoSeleccionado = TipoAlerta.STOCK_BAJO
                stockMinimo = ""; diasPrevios = ""; mensaje = ""
                activa = true; mostrarFormulario = false
                viewModel.limpiarAlertaSeleccionada()
                viewModel.resetState()
            }
            is AlertaUiState.Error -> {
                snackbarMessage = (uiState as AlertaUiState.Error).mensaje
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    mostrarEliminar?.let { alerta ->
        AlertDialog(
            onDismissRequest = { mostrarEliminar = null },
            containerColor = BgCard,
            title = { Text("Eliminar alerta", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que deseas eliminar la alerta de \"${alerta.productoNombre}\"?", color = TextSecondary) },
            confirmButton = {
                Button(onClick = { viewModel.eliminarAlerta(alerta.id); mostrarEliminar = null }, colors = ButtonDefaults.buttonColors(containerColor = Red)) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = { TextButton(onClick = { mostrarEliminar = null }) { Text("Cancelar", color = TextSecondary) } }
        )
    }

    if (mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = { mostrarFormulario = false; viewModel.limpiarAlertaSeleccionada() },
            containerColor = BgCard
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
                Text(text = if (alertaEnEdicion == null) "Crear alerta" else "Editar alerta", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(expanded = expandedProducto, onExpandedChange = { expandedProducto = !expandedProducto }) {
                    OutlinedTextField(value = productoSeleccionado?.nombre ?: "", onValueChange = {}, readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(), label = { Text("Producto") },
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = BgInput, unfocusedContainerColor = BgInput, focusedBorderColor = Blue, unfocusedBorderColor = BorderMedium, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                    ExposedDropdownMenu(expanded = expandedProducto, onDismissRequest = { expandedProducto = false }) {
                        productos.forEach { producto ->
                            DropdownMenuItem(text = { Text(producto.nombre) }, onClick = { productoSeleccionado = producto; expandedProducto = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(expanded = expandedTipo, onExpandedChange = { expandedTipo = !expandedTipo }) {
                    OutlinedTextField(value = tipoSeleccionado.name.replace("_", " "), onValueChange = {}, readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(), label = { Text("Tipo de alerta") },
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = BgInput, unfocusedContainerColor = BgInput, focusedBorderColor = Blue, unfocusedBorderColor = BorderMedium, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                    ExposedDropdownMenu(expanded = expandedTipo, onDismissRequest = { expandedTipo = false }) {
                        TipoAlerta.entries.forEach { tipo ->
                            DropdownMenuItem(text = { Text(tipo.name.replace("_", " ")) }, onClick = { tipoSeleccionado = tipo; expandedTipo = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (tipoSeleccionado == TipoAlerta.STOCK_BAJO) {
                    OutlinedTextField(value = stockMinimo, onValueChange = { stockMinimo = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Stock mínimo") },
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = BgInput, unfocusedContainerColor = BgInput, focusedBorderColor = Blue, unfocusedBorderColor = BorderMedium, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                }
                if (tipoSeleccionado == TipoAlerta.POR_VENCER) {
                    OutlinedTextField(value = diasPrevios, onValueChange = { diasPrevios = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Días previos al vencimiento") },
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = BgInput, unfocusedContainerColor = BgInput, focusedBorderColor = Blue, unfocusedBorderColor = BorderMedium, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                }
                if (tipoSeleccionado == TipoAlerta.PERSONALIZADA) {
                    OutlinedTextField(value = mensaje, onValueChange = { mensaje = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Mensaje") },
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = BgInput, unfocusedContainerColor = BgInput, focusedBorderColor = Blue, unfocusedBorderColor = BorderMedium, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Alerta activa", color = TextPrimary, modifier = Modifier.weight(1f))
                    Switch(checked = activa, onCheckedChange = { activa = it })
                }
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.guardarAlerta(alertaId = alertaEnEdicion?.id ?: "", producto = productoSeleccionado,
                            tipo = tipoSeleccionado, stockMinimoTexto = stockMinimo, diasPreviosTexto = diasPrevios, mensaje = mensaje, activa = activa)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Text(text = if (alertaEnEdicion == null) "Guardar alerta" else "Actualizar alerta", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
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
                onAlertasClick = {}
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.limpiarAlertaSeleccionada()
                    productoSeleccionado = null; tipoSeleccionado = TipoAlerta.STOCK_BAJO
                    stockMinimo = ""; diasPrevios = ""; mensaje = ""; activa = true
                    mostrarFormulario = true
                },
                containerColor = Green, contentColor = Color.White, shape = CircleShape
            ) { Icon(Icons.Default.Add, contentDescription = "Crear alerta") }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(BgScreen).padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().background(BgCard).padding(start = 8.dp, end = 20.dp, top = 48.dp, bottom = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onAtras) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = TextPrimary)
                        }
                        Text(text = "Alertas", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (uiState is AlertaUiState.Cargando) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Blue)
                    }
                }

                if (alertas.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔔", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No hay alertas creadas", color = TextSecondary, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Toca + para crear tu primera alerta", color = TextHint, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(alertas, key = { it.id }) { alerta ->
                            AlertaCard(
                                alerta = alerta,
                                onEditar = { viewModel.seleccionarAlerta(alerta) },
                                onEliminar = { mostrarEliminar = alerta },
                                onCambiarEstado = { nuevoEstado -> viewModel.cambiarEstadoAlerta(alerta.id, nuevoEstado) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(90.dp)) }
                    }
                }
            }

            snackbarMessage?.let { mensajeActual ->
                LaunchedEffect(mensajeActual) { delay(2200); snackbarMessage = null }
                Box(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = Blue), shape = RoundedCornerShape(14.dp)) {
                        Text(text = mensajeActual, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AlertaCard(alerta: Alerta, onEditar: () -> Unit, onEliminar: () -> Unit, onCambiarEstado: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BgCard), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = alerta.productoNombre, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Tipo: ${alerta.tipo.name.replace("_", " ")}", color = TextSecondary, fontSize = 13.sp)
                    if (alerta.tipo == TipoAlerta.STOCK_BAJO) Text(text = "Stock mínimo: ${alerta.stockMinimo}", color = TextSecondary, fontSize = 13.sp)
                    if (alerta.tipo == TipoAlerta.POR_VENCER) Text(text = "Avisar ${alerta.diasPreviosVencimiento} días antes", color = TextSecondary, fontSize = 13.sp)
                    if (alerta.tipo == TipoAlerta.PERSONALIZADA && alerta.mensaje.isNotBlank()) Text(text = alerta.mensaje, color = TextHint, fontSize = 12.sp)
                }
                Switch(checked = alerta.activa, onCheckedChange = onCambiarEstado)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onEditar, colors = ButtonDefaults.buttonColors(containerColor = Blue), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", color = Color.White)
                }
                Button(onClick = onEliminar, colors = ButtonDefaults.buttonColors(containerColor = Red), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar", color = Color.White)
                }
            }
        }
    }
}