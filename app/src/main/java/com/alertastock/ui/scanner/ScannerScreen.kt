package com.alertastock.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.data.model.Producto
import com.alertastock.ui.components.AlertaStockBottomBar
import com.alertastock.ui.components.BottomNavDestino
import com.alertastock.ui.product.ProductoViewModel
import com.alertastock.ui.theme.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

sealed class EstadoEscaner {
    object Escaneando : EstadoEscaner()
    object IngresoManual : EstadoEscaner()
    data class ProductoEncontrado(val producto: Producto) : EstadoEscaner()
    data class ProductoNoEncontrado(val codigo: String) : EstadoEscaner()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onAtras: () -> Unit,
    onAgregarProducto: (String) -> Unit = {},
    onVerDetalle: (Producto) -> Unit = {},
    onAgregarACanasta: (Producto) -> Unit = {},
    onInicioClick: () -> Unit = {},
    onProductosClick: () -> Unit = {},
    onAlertasClick: () -> Unit = {},
    viewModel: ProductoViewModel = viewModel()
) {
    val context = LocalContext.current
    val todosLosProductos by viewModel.todosLosProductos.observeAsState(emptyList())

    var estado by remember { mutableStateOf<EstadoEscaner>(EstadoEscaner.Escaneando) }
    var linternaActiva by remember { mutableStateOf(false) }
    var camaraControl by remember { mutableStateOf<Camera?>(null) }
    var codigoManual by remember { mutableStateOf("") }
    var sesionEscaneo by remember { mutableIntStateOf(0) }

    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val lanzadorPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { otorgado -> tienePermiso = otorgado }

    LaunchedEffect(Unit) {
        if (!tienePermiso) lanzadorPermiso.launch(Manifest.permission.CAMERA)
    }

    fun buscarProducto(codigo: String) {
        val producto = todosLosProductos.find {
            it.codigoBarras == codigo || it.nombre.equals(codigo, ignoreCase = true)
        }
        estado = if (producto != null) EstadoEscaner.ProductoEncontrado(producto)
        else EstadoEscaner.ProductoNoEncontrado(codigo)
    }

    fun volverAEscanear() {
        sesionEscaneo++
        estado = EstadoEscaner.Escaneando
    }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            AlertaStockBottomBar(
                destinoActual = BottomNavDestino.ESCANEAR,
                onInicioClick = onInicioClick,
                onProductosClick = onProductosClick,
                onEscanearClick = {},
                onAlertasClick = onAlertasClick
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (tienePermiso && estado is EstadoEscaner.Escaneando) {
                key(sesionEscaneo) {
                    CamaraEscaner(
                        onCodigoDetectado = { codigo -> buscarProducto(codigo) },
                        onCamaraLista = { camara -> camaraControl = camara }
                    )
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (estado is EstadoEscaner.Escaneando) Color.Transparent else BgCard)
                        .padding(start = 8.dp, end = 20.dp, top = 48.dp, bottom = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onAtras) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                        }
                        Text("Escanear producto", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.weight(1f))
                        if (estado is EstadoEscaner.Escaneando) {
                            IconButton(onClick = { linternaActiva = !linternaActiva; camaraControl?.cameraControl?.enableTorch(linternaActiva) }) {
                                Icon(
                                    if (linternaActiva) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                    contentDescription = "Linterna",
                                    tint = if (linternaActiva) Color.Yellow else Color.White
                                )
                            }
                        }
                    }
                }

                when (val estadoActual = estado) {
                    is EstadoEscaner.Escaneando -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.align(Alignment.Center), contentAlignment = Alignment.Center) {
                                MarcoEscaneo()
                                Text("Apunta al código de barras del producto", color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = 60.dp).padding(horizontal = 32.dp))
                            }
                            Button(
                                onClick = { estado = EstadoEscaner.IngresoManual },
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ingresar código manualmente", color = Color.White, fontSize = 13.sp)
                            }
                            if (!tienePermiso) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("📷", fontSize = 48.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("Se necesita permiso de cámara", color = Color.White, fontSize = 15.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(onClick = { lanzadorPermiso.launch(Manifest.permission.CAMERA) }) { Text("Conceder permiso") }
                                    }
                                }
                            }
                        }
                    }

                    is EstadoEscaner.IngresoManual -> {
                        Column(modifier = Modifier.fillMaxSize().background(BgScreen).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("CÓDIGO DE BARRAS O REFERENCIA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp, modifier = Modifier.fillMaxWidth())
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = codigoManual, onValueChange = { codigoManual = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Ej: 7702001234567", color = TextHint) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = { if (codigoManual.isNotEmpty()) IconButton(onClick = { codigoManual = "" }) { Icon(Icons.Default.Clear, contentDescription = null, tint = TextHint) } },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = BgInput, unfocusedContainerColor = BgInput, focusedBorderColor = Blue, unfocusedBorderColor = BorderMedium, focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Blue)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = { if (codigoManual.isNotBlank()) buscarProducto(codigoManual) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue), enabled = codigoManual.isNotBlank()) {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Buscar producto", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(onClick = { codigoManual = ""; volverAEscanear() }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TextSecondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Usar cámara", fontSize = 15.sp, color = TextSecondary)
                            }
                        }
                    }

                    is EstadoEscaner.ProductoEncontrado -> {
                        val producto = todosLosProductos.find { it.id == estadoActual.producto.id } ?: estadoActual.producto
                        var mostrarDialogoStock by remember { mutableStateOf(false) }

                        if (mostrarDialogoStock) {
                            DialogoDescontarStock(
                                producto = producto,
                                onConfirmar = { cantidad ->
                                    viewModel.descontarStock(producto.id, cantidad)
                                    mostrarDialogoStock = false
                                },
                                onCancelar = { mostrarDialogoStock = false }
                            )
                        }

                        Column(modifier = Modifier.fillMaxSize().background(BgScreen).padding(20.dp)) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Green))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("PRODUCTO ENCONTRADO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Green, letterSpacing = 0.8.sp)
                            }
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = BgCard), shape = RoundedCornerShape(16.dp)) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)).background(Blue.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                        Text(text = producto.emoji, fontSize = 26.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(producto.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("Ref: #${producto.codigoBarras.takeLast(6)}  ·  ${producto.categoria}", fontSize = 12.sp, color = TextSecondary)
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    val stockColor = when {
                                        producto.stockActual <= producto.stockMinimo -> Red
                                        producto.stockActual <= producto.stockMinimo * 2 -> Yellow
                                        else -> Green
                                    }
                                    StatChip("${producto.stockActual}", "STOCK", stockColor)
                                    StatChip("${producto.stockMinimo}", "MÍNIMO", TextSecondary)
                                    if (producto.fechaVencimiento.isNotBlank()) StatChip(producto.fechaVencimiento, "VENCE", Yellow)
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedButton(onClick = { onVerDetalle(producto) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue)) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Ver detalle", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { onAgregarACanasta(producto) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Agregar a canasta", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = { mostrarDialogoStock = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Actualizar stock", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            TextButton(onClick = { volverAEscanear() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Escanear otro", fontSize = 14.sp, color = TextSecondary)
                            }
                        }
                    }

                    is EstadoEscaner.ProductoNoEncontrado -> {
                        val codigo = estadoActual.codigo
                        Column(modifier = Modifier.fillMaxSize().background(BgScreen).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Red))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("CÓDIGO NO ENCONTRADO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Red, letterSpacing = 0.8.sp)
                            }
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Red.copy(alpha = 0.08f)), shape = RoundedCornerShape(16.dp)) {
                                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🔍", fontSize = 40.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Código: $codigo", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Este producto no está en tu inventario", fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = { onAgregarProducto(codigo) }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Agregar al inventario", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(onClick = { volverAEscanear() }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)) {
                                Text("Escanear otro", fontSize = 15.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun CamaraEscaner(onCodigoDetectado: (String) -> Unit, onCamaraLista: (Camera) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    var yaDetecto by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val futuro = ProcessCameraProvider.getInstance(ctx)
            futuro.addListener({
                val proveedor = futuro.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val analizador = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                    .also { analisis ->
                        analisis.setAnalyzer(executor) { imageProxy ->
                            if (yaDetecto) { imageProxy.close(); return@setAnalyzer }
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val imagen = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                BarcodeScanning.getClient().process(imagen)
                                    .addOnSuccessListener { codigos ->
                                        val codigo = codigos.firstOrNull { it.rawValue != null }?.rawValue
                                        if (codigo != null && !yaDetecto) { yaDetecto = true; onCodigoDetectado(codigo) }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else { imageProxy.close() }
                        }
                    }
                try {
                    proveedor.unbindAll()
                    val camara = proveedor.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analizador)
                    onCamaraLista(camara)
                } catch (e: Exception) { e.printStackTrace() }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun MarcoEscaneo() {
    val colorMarco = Color(0xFF4FC3F7)
    Box(modifier = Modifier.size(260.dp).border(2.dp, colorMarco.copy(alpha = 0.3f), RoundedCornerShape(16.dp))) {
        Box(modifier = Modifier.size(40.dp, 4.dp).background(colorMarco).align(Alignment.TopStart))
        Box(modifier = Modifier.size(4.dp, 40.dp).background(colorMarco).align(Alignment.TopStart))
        Box(modifier = Modifier.size(40.dp, 4.dp).background(colorMarco).align(Alignment.TopEnd))
        Box(modifier = Modifier.size(4.dp, 40.dp).background(colorMarco).align(Alignment.TopEnd))
        Box(modifier = Modifier.size(40.dp, 4.dp).background(colorMarco).align(Alignment.BottomStart))
        Box(modifier = Modifier.size(4.dp, 40.dp).background(colorMarco).align(Alignment.BottomStart))
        Box(modifier = Modifier.size(40.dp, 4.dp).background(colorMarco).align(Alignment.BottomEnd))
        Box(modifier = Modifier.size(4.dp, 40.dp).background(colorMarco).align(Alignment.BottomEnd))
    }
}

@Composable
fun StatChip(valor: String, etiqueta: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = etiqueta, fontSize = 10.sp, color = TextSecondary, letterSpacing = 0.5.sp)
    }
}

@Composable
fun DialogoDescontarStock(producto: Producto, onConfirmar: (Int) -> Unit, onCancelar: () -> Unit) {
    var cantidad by remember { mutableStateOf("1") }
    val cantidadInt = cantidad.toIntOrNull() ?: 0
    val stockResultante = producto.stockActual - cantidadInt
    val esValido = cantidadInt > 0 && cantidadInt <= producto.stockActual

    AlertDialog(
        onDismissRequest = onCancelar,
        containerColor = BgCard,
        title = { Text("Actualizar stock", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(producto.nombre, fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(bottom = 16.dp))
                Text("CANTIDAD A DESCONTAR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { val a = cantidad.toIntOrNull() ?: 1; if (a > 1) cantidad = (a - 1).toString() }) {
                        Icon(Icons.Default.Remove, contentDescription = "Menos", tint = if ((cantidad.toIntOrNull() ?: 1) > 1) Blue else BorderMedium)
                    }
                    OutlinedTextField(
                        value = cantidad, onValueChange = { if (it.all { c -> c.isDigit() }) cantidad = it },
                        modifier = Modifier.width(80.dp), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = BgInput, unfocusedContainerColor = BgInput, focusedBorderColor = Blue, unfocusedBorderColor = BorderMedium, cursorColor = Blue)
                    )
                    IconButton(onClick = { val a = cantidad.toIntOrNull() ?: 0; if (a < producto.stockActual) cantidad = (a + 1).toString() }) {
                        Icon(Icons.Default.Add, contentDescription = "Más", tint = if ((cantidad.toIntOrNull() ?: 0) < producto.stockActual) Blue else BorderMedium)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = if (esValido) Green.copy(alpha = 0.08f) else Red.copy(alpha = 0.08f)), shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${producto.stockActual}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Actual", fontSize = 11.sp, color = TextSecondary)
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$stockResultante", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (esValido) Green else Red)
                            Text("Resultante", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
                if (!esValido && cantidadInt > 0) {
                    Text("No puede descontar más del stock disponible", color = Red, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmar(cantidadInt) }, enabled = esValido, colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                Text("Confirmar", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onCancelar) { Text("Cancelar", color = TextSecondary) } }
    )
}