package com.alertastock.ui.product.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.data.model.Producto
import com.alertastock.ui.components.AlertaStockBottomBar
import com.alertastock.ui.components.BottomNavDestino
import com.alertastock.ui.product.ProductoViewModel
import com.alertastock.ui.ia.IaViewModel
import com.alertastock.ui.theme.*

enum class FiltroEstado { TODOS, CRITICO, BAJO, POR_VENCER, BUEN_ESTADO }
enum class OrdenarPor { NOMBRE, STOCK_ASC, STOCK_DESC, VENCIMIENTO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    onAtras: () -> Unit,
    onAgregarProducto: () -> Unit = {},
    onEditarProducto: (Producto) -> Unit = {},
    onInicioClick: () -> Unit = {},
    onEscanearClick: () -> Unit = {},
    onAlertasClick: () -> Unit = {},
    viewModel: ProductoViewModel = viewModel(),
    filtroInicial: String = "TODOS"
) {
    val todosLosProductos by viewModel.todosLosProductos.observeAsState(emptyList())
    val productosCriticos by viewModel.productosCriticos.observeAsState(emptyList())
    val iaViewModel: IaViewModel = viewModel()
    val estadosIa by iaViewModel.estados.collectAsState()

    var busqueda by remember { mutableStateOf("") }
    var mostrarFiltros by remember { mutableStateOf(false) }

    var filtroEstadoAplicado by remember {
        mutableStateOf(
            when (filtroInicial) {
                "CRITICO"     -> FiltroEstado.CRITICO
                "BAJO"        -> FiltroEstado.BAJO
                "POR_VENCER"  -> FiltroEstado.POR_VENCER
                "BUEN_ESTADO" -> FiltroEstado.BUEN_ESTADO
                else          -> FiltroEstado.TODOS
            }
        )
    }
    var categoriaAplicada by remember { mutableStateOf("") }
    var ordenAplicado by remember { mutableStateOf(OrdenarPor.NOMBRE) }

    var filtroEstadoTemp by remember { mutableStateOf(filtroEstadoAplicado) }
    var categoriaTemp by remember { mutableStateOf("") }
    var ordenTemp by remember { mutableStateOf(OrdenarPor.NOMBRE) }

    // El estado de inventario usa primero el resultado de IA. Si un producto aun no
    // tiene analisis, conserva la regla manual como respaldo para no dejar la UI sin estado.
    val productosCriticosIa = remember(todosLosProductos, estadosIa) {
        todosLosProductos.filter { producto ->
            val riesgoIa = estadosIa[producto.id]?.resultado?.riesgo
            riesgoIa == "ALTO" || (riesgoIa == null && producto.stockActual <= producto.stockMinimo)
        }
    }
    val productosBajos = remember(todosLosProductos, estadosIa) {
        todosLosProductos.filter { producto ->
            val riesgoIa = estadosIa[producto.id]?.resultado?.riesgo
            riesgoIa == "MEDIO" || (
                riesgoIa == null &&
                    producto.stockActual > producto.stockMinimo &&
                    producto.stockActual <= producto.stockMinimo * 2
            )
        }
    }
    val productosPorVencer = remember(todosLosProductos) {
        todosLosProductos.filter { it.estaVenciendo }
    }
    val productosEnBuenEstado = remember(todosLosProductos, estadosIa) {
        todosLosProductos.filter { producto ->
            val riesgoIa = estadosIa[producto.id]?.resultado?.riesgo
            (riesgoIa == "BAJO" || (riesgoIa == null && producto.stockActual > producto.stockMinimo * 2)) &&
                !producto.estaVenciendo
        }
    }

    // Analiza automaticamente los productos al cargar Inventario. Las llamadas solo se
    // lanzan para productos que aun no tengan un estado IA en esta sesion.
    LaunchedEffect(todosLosProductos) {
        todosLosProductos.forEach { producto ->
            if (estadosIa[producto.id] == null) {
                iaViewModel.analizar(producto)
            }
        }
    }
    val categorias = remember(todosLosProductos) {
        todosLosProductos.map { it.categoria }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val listaFiltrada = remember(
        busqueda, filtroEstadoAplicado, categoriaAplicada, ordenAplicado,
        todosLosProductos, productosCriticosIa, productosBajos, productosPorVencer, productosEnBuenEstado
    ) {
        val base = when (filtroEstadoAplicado) {
            FiltroEstado.TODOS       -> todosLosProductos
            FiltroEstado.CRITICO     -> productosCriticosIa
            FiltroEstado.BAJO        -> productosBajos
            FiltroEstado.POR_VENCER  -> productosPorVencer
            FiltroEstado.BUEN_ESTADO -> productosEnBuenEstado
        }
        val porCategoria = if (categoriaAplicada.isBlank()) base
        else base.filter { it.categoria == categoriaAplicada }
        val porBusqueda = if (busqueda.isBlank()) porCategoria
        else porCategoria.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
                    it.codigoBarras.contains(busqueda, ignoreCase = true) ||
                    it.categoria.contains(busqueda, ignoreCase = true)
        }
        when (ordenAplicado) {
            OrdenarPor.NOMBRE      -> porBusqueda.sortedBy { it.nombre.lowercase() }
            OrdenarPor.STOCK_ASC   -> porBusqueda.sortedBy { it.stockActual }
            OrdenarPor.STOCK_DESC  -> porBusqueda.sortedByDescending { it.stockActual }
            OrdenarPor.VENCIMIENTO -> porBusqueda.sortedWith(
                compareBy({ it.fechaVencimiento.isBlank() }, { it.fechaVencimiento })
            )
        }
    }

    val hayFiltrosSheet = categoriaAplicada.isNotBlank() || ordenAplicado != OrdenarPor.NOMBRE

    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            containerColor = BgCard,
            title = { Text("Eliminar producto", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "¿Seguro que deseas eliminar \"${producto.nombre}\"? Esta acción no se puede deshacer.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminar(producto); productoAEliminar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // Bottom Sheet
    if (mostrarFiltros) {
        ModalBottomSheet(
            onDismissRequest = { mostrarFiltros = false },
            containerColor = BgCard,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text("Filtrar y ordenar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(bottom = 20.dp))

                Text("ESTADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { BottomSheetChip("Todos",       filtroEstadoTemp == FiltroEstado.TODOS)       { filtroEstadoTemp = FiltroEstado.TODOS } }
                    item { BottomSheetChip("Riesgo alto",  filtroEstadoTemp == FiltroEstado.CRITICO)     { filtroEstadoTemp = FiltroEstado.CRITICO } }
                    item { BottomSheetChip("Riesgo medio", filtroEstadoTemp == FiltroEstado.BAJO)        { filtroEstadoTemp = FiltroEstado.BAJO } }
                    item { BottomSheetChip("Por vencer",  filtroEstadoTemp == FiltroEstado.POR_VENCER)  { filtroEstadoTemp = FiltroEstado.POR_VENCER } }
                    item { BottomSheetChip("Riesgo bajo",  filtroEstadoTemp == FiltroEstado.BUEN_ESTADO) { filtroEstadoTemp = FiltroEstado.BUEN_ESTADO } }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("CATEGORÍA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (categorias.isEmpty()) {
                    Text(text = "No hay categorías aún", fontSize = 13.sp, color = TextHint)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categorias) { categoria ->
                            BottomSheetChip(texto = categoria, activo = categoriaTemp == categoria) {
                                categoriaTemp = if (categoriaTemp == categoria) "" else categoria
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text("ORDENAR POR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BottomSheetChip("Nombre",  ordenTemp == OrdenarPor.NOMBRE)      { ordenTemp = OrdenarPor.NOMBRE }
                    BottomSheetChip("Stock ↑", ordenTemp == OrdenarPor.STOCK_ASC)   { ordenTemp = OrdenarPor.STOCK_ASC }
                    BottomSheetChip("Stock ↓", ordenTemp == OrdenarPor.STOCK_DESC)  { ordenTemp = OrdenarPor.STOCK_DESC }
                    BottomSheetChip("Venc.",   ordenTemp == OrdenarPor.VENCIMIENTO) { ordenTemp = OrdenarPor.VENCIMIENTO }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = { filtroEstadoAplicado = filtroEstadoTemp; categoriaAplicada = categoriaTemp; ordenAplicado = ordenTemp; mostrarFiltros = false },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) { Text("Aplicar filtros", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White) }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        filtroEstadoTemp = FiltroEstado.TODOS; categoriaTemp = ""; ordenTemp = OrdenarPor.NOMBRE
                        filtroEstadoAplicado = FiltroEstado.TODOS; categoriaAplicada = ""; ordenAplicado = OrdenarPor.NOMBRE
                        mostrarFiltros = false
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) { Text("Limpiar filtros", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }

    Scaffold(
        containerColor = BgScreen,
        bottomBar = {
            AlertaStockBottomBar(
                destinoActual = BottomNavDestino.PRODUCTOS,
                onInicioClick = onInicioClick,
                onProductosClick = {},
                onEscanearClick = onEscanearClick,
                onAlertasClick = onAlertasClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarProducto,
                containerColor = Blue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgScreen)
                .padding(paddingValues)
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
                    Text(
                        text = "Inventario",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        IconButton(onClick = { filtroEstadoTemp = filtroEstadoAplicado; categoriaTemp = categoriaAplicada; ordenTemp = ordenAplicado; mostrarFiltros = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtrar", tint = if (hayFiltrosSheet) Blue else TextSecondary, modifier = Modifier.size(22.dp))
                        }
                        if (hayFiltrosSheet) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Blue).align(Alignment.TopEnd).offset(x = (-6).dp, y = 6.dp))
                        }
                    }
                }
            }

            // Barra de búsqueda
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar producto...", color = TextHint) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextHint) },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextHint)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgInput, unfocusedContainerColor = BgInput,
                        focusedBorderColor = Blue, unfocusedBorderColor = BorderMedium,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Blue
                    )
                )
            }

            // Chips de acceso rápido
            LazyRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FiltroChipRapido("Todos (${todosLosProductos.size})", filtroEstadoAplicado == FiltroEstado.TODOS, Blue) { filtroEstadoAplicado = FiltroEstado.TODOS } }
                item { FiltroChipRapido("Riesgo alto (${productosCriticosIa.size})", filtroEstadoAplicado == FiltroEstado.CRITICO, Red) { filtroEstadoAplicado = FiltroEstado.CRITICO } }
                item { FiltroChipRapido("Riesgo medio (${productosBajos.size})", filtroEstadoAplicado == FiltroEstado.BAJO, Yellow) { filtroEstadoAplicado = FiltroEstado.BAJO } }
                item { FiltroChipRapido("Por vencer (${productosPorVencer.size})", filtroEstadoAplicado == FiltroEstado.POR_VENCER, Color(0xFFFB8C00)) { filtroEstadoAplicado = FiltroEstado.POR_VENCER } }
                item { FiltroChipRapido("Riesgo bajo (${productosEnBuenEstado.size})", filtroEstadoAplicado == FiltroEstado.BUEN_ESTADO, Green) { filtroEstadoAplicado = FiltroEstado.BUEN_ESTADO } }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text("${listaFiltrada.size} productos", fontSize = 12.sp, color = TextHint, modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))

            if (listaFiltrada.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (busqueda.isNotEmpty()) "Sin resultados para \"$busqueda\""
                            else "No hay productos en este filtro",
                            color = TextSecondary, fontSize = 15.sp
                        )
                        if (busqueda.isEmpty() && filtroEstadoAplicado == FiltroEstado.TODOS && !hayFiltrosSheet) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Toca + para agregar tu primer producto", color = TextHint, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listaFiltrada, key = { it.id }) { producto ->
                        ProductoCard(
                            producto = producto,
                            estadoIa = estadosIa[producto.id],
                            onAnalizarIa = { iaViewModel.analizar(producto) },
                            onEditar = { onEditarProducto(producto) },
                            onEliminar = { productoAEliminar = producto }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun FiltroChipRapido(texto: String, activo: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (activo) color.copy(alpha = 0.2f) else BgCard,
        border = androidx.compose.foundation.BorderStroke(width = 1.5.dp, color = if (activo) color else BorderMedium)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            fontSize = 12.sp,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
            color = if (activo) color else TextSecondary
        )
    }
}

@Composable
fun BottomSheetChip(texto: String, activo: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (activo) Blue.copy(alpha = 0.15f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(width = 1.5.dp, color = if (activo) Blue else BorderMedium)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
            color = if (activo) Blue else TextSecondary
        )
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    estadoIa: IaViewModel.EstadoProductoIa?,
    onAnalizarIa: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val resultadoIa = estadoIa?.resultado
    val minimoReferencia = resultadoIa?.stock_minimo_recomendado ?: producto.stockMinimo
    val stockColor = when (resultadoIa?.riesgo) {
        "ALTO" -> Red
        "MEDIO" -> Yellow
        "BAJO" -> Green
        else -> when {
            producto.stockActual <= producto.stockMinimo -> Red
            producto.stockActual <= producto.stockMinimo * 2 -> Yellow
            else -> Green
        }
    }
    val stockProgress = if (minimoReferencia > 0) {
        (producto.stockActual.toFloat() / (minimoReferencia * 2).toFloat()).coerceIn(0f, 1f)
    } else 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                        .background(stockColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = producto.emoji, fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = producto.nombre,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (resultadoIa != null)
                            "Stock: ${producto.stockActual} unid.  ·  Mín IA: ${resultadoIa.stock_minimo_recomendado}"
                        else
                            "Stock: ${producto.stockActual} unid.  ·  Mín manual: ${producto.stockMinimo}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    if (producto.fechaVencimiento.isNotBlank()) {
                        Text(
                            text = "Vence: ${producto.fechaVencimiento}",
                            fontSize = 11.sp,
                            color = if (producto.estaVenciendo) Color(0xFFFB8C00) else TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(4.dp)
                            .clip(RoundedCornerShape(2.dp)).background(BorderMedium)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(stockProgress).fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp)).background(stockColor)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(stockColor))
                Spacer(modifier = Modifier.width(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onEditar,
                        modifier = Modifier.width(72.dp).height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onEliminar,
                        modifier = Modifier.width(72.dp).height(34.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Red),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Borrar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderMedium)
            Spacer(modifier = Modifier.height(10.dp))

            when {
                estadoIa?.cargando == true -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analizando historial con IA...", fontSize = 12.sp, color = TextSecondary)
                    }
                }

                estadoIa?.resultado != null -> {
                    val resultado = requireNotNull(estadoIa.resultado)
                    val colorRiesgo = when (resultado.riesgo) {
                        "ALTO" -> Red
                        "MEDIO" -> Yellow
                        else -> Green
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Predicción IA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue)
                            Text(
                                "Demanda estimada: ${resultado.demanda_predicha} unid.",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Text(
                                "Stock mínimo recomendado: ${resultado.stock_minimo_recomendado} unid.",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Text(
                                "Reposición sugerida: ${resultado.reposicion_sugerida} unid.",
                                fontSize = 12.sp,
                                color = TextPrimary
                            )
                            Text(
                                "Margen de seguridad: ${resultado.margen_seguridad} unid.",
                                fontSize = 10.sp,
                                color = TextHint
                            )
                            Text(
                                "Historial usado: ${estadoIa.semanasConHistorial}/8 semanas con ventas",
                                fontSize = 10.sp,
                                color = TextHint
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colorRiesgo.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Riesgo ${resultado.riesgo}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorRiesgo
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = onAnalizarIa, contentPadding = PaddingValues(0.dp)) {
                        Text("Actualizar análisis", fontSize = 11.sp)
                    }
                }

                estadoIa?.error != null -> {
                    Text("IA: ${estadoIa.error}", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        "Semanas con ventas detectadas: ${estadoIa.semanasConHistorial}/8",
                        fontSize = 10.sp,
                        color = TextHint
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(onClick = onAnalizarIa, modifier = Modifier.height(34.dp)) {
                        Text("Reintentar análisis", fontSize = 11.sp)
                    }
                }

                else -> {
                    OutlinedButton(
                        onClick = onAnalizarIa,
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analizar con IA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
