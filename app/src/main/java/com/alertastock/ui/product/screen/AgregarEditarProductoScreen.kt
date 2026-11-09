package com.alertastock.ui.product.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.data.local.database.AlertaStockDatabase
import com.alertastock.data.model.Producto
import com.alertastock.ui.product.ProductoViewModel
import com.alertastock.ui.theme.*
import com.alertastock.data.repository.CategoriaRepository

private val EMOJIS = listOf(
    "📦", "🥛", "🍞", "🥤", "☕", "🧃", "🧴", "🧹",
    "🍫", "🧀", "🥩", "🐟", "🥚", "🧂", "🛒", "🏠"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarEditarProductoScreen(
    productoExistente: Producto? = null,
    codigoInicial: String = "",
    onGuardado: () -> Unit,
    onAtras: () -> Unit,
    viewModel: ProductoViewModel = viewModel()
) {
    val context = LocalContext.current
    val database = remember { AlertaStockDatabase.getDatabase(context) }
    val categoriaRepository = remember { CategoriaRepository(database.categoriaDao()) }

// ✅ Sincroniza categorías desde Firestore al abrir el formulario
    LaunchedEffect(Unit) {
        categoriaRepository.sincronizarDesdeFirestore()
    }

    val categorias by categoriaRepository.todasLasCategorias.observeAsState(emptyList())

    val esEdicion = productoExistente != null
    val titulo = if (esEdicion) "Editar producto" else "Agregar producto"

    var nombre by remember { mutableStateOf(productoExistente?.nombre ?: "") }
    var stockActual by remember { mutableStateOf(productoExistente?.stockActual?.toString() ?: "0") }
    var stockMinimo by remember { mutableStateOf(productoExistente?.stockMinimo?.toString() ?: "1") }
    var codigoBarras by remember { mutableStateOf(productoExistente?.codigoBarras ?: codigoInicial) }
    var categoria by remember { mutableStateOf(productoExistente?.categoria ?: "") }
    var fechaVencimiento by remember { mutableStateOf(productoExistente?.fechaVencimiento ?: "") }
    var precioCompra by remember { mutableStateOf(productoExistente?.precioCompra?.toString() ?: "") }
    var precioVenta by remember { mutableStateOf(productoExistente?.precioVenta?.toString() ?: "") }
    var emojiSeleccionado by remember { mutableStateOf(productoExistente?.emoji ?: "📦") }
    var mostrarSelectorEmoji by remember { mutableStateOf(false) }

    // ✅ Estado del dropdown de categorías
    var expandedCategoria by remember { mutableStateOf(false) }

    var nombreError by remember { mutableStateOf(false) }
    var stockError by remember { mutableStateOf(false) }

    fun guardar() {
        nombreError = nombre.isBlank()
        stockError = stockActual.toIntOrNull() == null
        if (nombreError || stockError) return

        val producto = Producto(
            id = productoExistente?.id ?: 0,
            nombre = nombre.trim(),
            categoria = categoria.trim(),
            stockActual = stockActual.toIntOrNull() ?: 0,
            stockMinimo = stockMinimo.toIntOrNull() ?: 1,
            precioCompra = precioCompra.toDoubleOrNull() ?: 0.0,
            precioVenta = precioVenta.toDoubleOrNull() ?: 0.0,
            codigoBarras = codigoBarras.trim(),
            fechaVencimiento = fechaVencimiento.trim(),
            emoji = emojiSeleccionado
        )

        if (esEdicion) viewModel.actualizar(producto)
        else viewModel.insertar(producto)
        onGuardado()
    }

    Column(modifier = Modifier.fillMaxSize().background(BgScreen)) {
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
                Text(text = titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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

            // Selector de emoji
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(BgCard)
                    .border(1.5.dp, if (mostrarSelectorEmoji) Blue else BorderMedium, RoundedCornerShape(20.dp))
                    .clickable { mostrarSelectorEmoji = !mostrarSelectorEmoji },
                contentAlignment = Alignment.Center
            ) { Text(text = emojiSeleccionado, fontSize = 36.sp) }
            Text(text = "Toca para cambiar ícono", fontSize = 11.sp, color = TextHint, modifier = Modifier.padding(top = 6.dp))

            if (mostrarSelectorEmoji) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = BgCard), shape = RoundedCornerShape(16.dp)) {
                    val rows = EMOJIS.chunked(8)
                    Column(modifier = Modifier.padding(12.dp)) {
                        rows.forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                row.forEach { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (emoji == emojiSeleccionado) Blue.copy(alpha = 0.2f) else Color.Transparent)
                                            .clickable { emojiSeleccionado = emoji; mostrarSelectorEmoji = false },
                                        contentAlignment = Alignment.Center
                                    ) { Text(text = emoji, fontSize = 22.sp) }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FormLabel("NOMBRE DEL PRODUCTO")
            FormTextField(
                value = nombre,
                onValueChange = { nombre = it; nombreError = false },
                placeholder = "Ej: Leche entera 1L",
                isError = nombreError,
                errorMessage = "El nombre es obligatorio"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("STOCK ACTUAL")
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockActual,
                            onValueChange = { if (it.all { c -> c.isDigit() }) { stockActual = it; stockError = false } },
                            modifier = Modifier.weight(1f),
                            isError = stockError,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = formFieldColors()
                        )
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Blue.copy(alpha = 0.15f))
                                .clickable { stockActual = ((stockActual.toIntOrNull() ?: 0) + 1).toString() },
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Add, contentDescription = "+", tint = Blue, modifier = Modifier.size(20.dp)) }
                    }
                    if (stockError) Text("Ingresa un número válido", color = Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("CÓDIGO")
                    FormTextField(
                        value = codigoBarras,
                        onValueChange = { codigoBarras = it },
                        placeholder = "Cód. de barras",
                        keyboardType = KeyboardType.Number
                    )
                    if (codigoInicial.isNotBlank() && !esEdicion) {
                        Text("✓ Código escaneado", fontSize = 10.sp, color = Green, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormLabel("STOCK MÍNIMO (alerta)")
            FormTextField(
                value = stockMinimo,
                onValueChange = { if (it.all { c -> c.isDigit() }) stockMinimo = it },
                placeholder = "Ej: 5",
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Selector de categoría con dropdown
            FormLabel("CATEGORÍA")
            if (categorias.isEmpty()) {
                // Sin categorías creadas — campo de texto libre
                FormTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    placeholder = "Ej: Lácteos (crea categorías en Configuración)"
                )
            } else {
                // Con categorías — dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategoria,
                    onExpandedChange = { expandedCategoria = !expandedCategoria }
                ) {
                    OutlinedTextField(
                        value = if (categoria.isBlank()) "" else {
                            val cat = categorias.find { it.nombre == categoria }
                            if (cat != null) "${cat.emoji} ${cat.nombre}" else categoria
                        },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        placeholder = { Text("Selecciona una categoría", color = TextHint) },
                        trailingIcon = {
                            Icon(
                                if (expandedCategoria) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = formFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoria,
                        onDismissRequest = { expandedCategoria = false }
                    ) {
                        // Opción vacía
                        DropdownMenuItem(
                            text = { Text("Sin categoría", color = TextHint) },
                            onClick = { categoria = ""; expandedCategoria = false }
                        )
                        // Categorías disponibles
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(cat.emoji, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(cat.nombre, color = TextPrimary)
                                    }
                                },
                                onClick = { categoria = cat.nombre; expandedCategoria = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FormLabel("FECHA DE VENCIMIENTO")
            FormTextField(
                value = fechaVencimiento,
                onValueChange = { fechaVencimiento = it },
                placeholder = "Ej: 2027-03-08  (opcional)"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("PRECIO COMPRA")
                    FormTextField(value = precioCompra, onValueChange = { precioCompra = it }, placeholder = "0", keyboardType = KeyboardType.Decimal)
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("PRECIO VENTA")
                    FormTextField(value = precioVenta, onValueChange = { precioVenta = it }, placeholder = "0", keyboardType = KeyboardType.Decimal)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { guardar() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Icon(if (esEdicion) Icons.Default.Check else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (esEdicion) "Guardar cambios" else "Guardar producto",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        letterSpacing = 0.8.sp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = TextHint) },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = formFieldColors()
    )
    if (isError && errorMessage.isNotEmpty()) {
        Text(errorMessage, color = Red, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = BgInput,
    unfocusedContainerColor = BgInput,
    focusedBorderColor = Blue,
    unfocusedBorderColor = BorderMedium,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = Blue,
    errorBorderColor = Red,
    errorContainerColor = BgInput
)