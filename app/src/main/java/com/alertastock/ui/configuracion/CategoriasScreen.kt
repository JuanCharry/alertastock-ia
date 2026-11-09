package com.alertastock.ui.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.data.local.database.AlertaStockDatabase
import com.alertastock.data.model.Categoria
import com.alertastock.data.repository.CategoriaRepository
import com.alertastock.ui.product.ProductoViewModel
import com.alertastock.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val EMOJIS_CATEGORIA = listOf(
    "🥛", "🥤", "🌾", "🧼", "🍿", "🍞",
    "🥩", "🐟", "🥦", "🍎", "🧃", "🏪",
    "💊", "🧴", "📦", "🛒", "🍫", "🥚"
)

val COLORES_CATEGORIA = listOf(
    Color(0xFF2E67F8),
    Color(0xFF43A047),
    Color(0xFFFB8C00),
    Color(0xFFE53935),
    Color(0xFF9C27B0),
    Color(0xFFFFB300)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    onAtras: () -> Unit,
    viewModel: ProductoViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AlertaStockDatabase.getDatabase(context) }
    val categoriaRepository = remember { CategoriaRepository(database.categoriaDao()) }

    LaunchedEffect(Unit) {
        categoriaRepository.sincronizarDesdeFirestore()
    }

    val categorias by categoriaRepository.todasLasCategorias.observeAsState(emptyList())
    val todosLosProductos by viewModel.todosLosProductos.observeAsState(emptyList())

    var mostrarFormulario by remember { mutableStateOf(false) }
    var categoriaEnEdicion by remember { mutableStateOf<Categoria?>(null) }
    var nombreCategoria by remember { mutableStateOf("") }
    var emojiSeleccionado by remember { mutableStateOf(EMOJIS_CATEGORIA[0]) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var mostrarEliminar by remember { mutableStateOf<Categoria?>(null) }

    // Diálogo confirmar eliminar
    mostrarEliminar?.let { categoria ->
        val conteo = todosLosProductos.count { it.categoria == categoria.nombre }
        AlertDialog(
            onDismissRequest = { mostrarEliminar = null },
            containerColor = BgCard,
            icon = {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(Red.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Red, modifier = Modifier.size(28.dp))
                }
            },
            title = { Text("¿Eliminar Categoría?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "¿Estás seguro que deseas eliminar \"${categoria.nombre}\"?${if (conteo > 0) " Se quitará la categoría de $conteo productos." else ""}",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            categoriaRepository.eliminar(categoria)
                            viewModel.eliminarCategoria(categoria.nombre)
                            mensajeExito = "Categoría eliminada"
                            mostrarEliminar = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Sí, eliminar", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarEliminar = null }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // Bottom Sheet — Nueva / Editar categoría
    if (mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarFormulario = false
                categoriaEnEdicion = null
                nombreCategoria = ""
                emojiSeleccionado = EMOJIS_CATEGORIA[0]
            },
            containerColor = BgCard,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = if (categoriaEnEdicion == null) "Nueva Categoría" else "Editar Categoría",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                if (categoriaEnEdicion != null) {
                    val conteo = todosLosProductos.count { it.categoria == categoriaEnEdicion?.nombre }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Yellow.copy(alpha = 0.10f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Yellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Los cambios afectarán los $conteo productos de esta categoría", color = Yellow, fontSize = 12.sp)
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Nombre
                Text("NOMBRE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nombreCategoria,
                    onValueChange = { nombreCategoria = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Lácteos", color = TextHint) },
                    leadingIcon = {
                        Text(emojiSeleccionado, fontSize = 18.sp, modifier = Modifier.padding(start = 4.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgInput, unfocusedContainerColor = BgInput,
                        focusedBorderColor = Blue, unfocusedBorderColor = BorderMedium,
                        focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = Blue
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Selector de ícono
                Text("ÍCONO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EMOJIS_CATEGORIA) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (emojiSeleccionado == emoji) Blue.copy(alpha = 0.15f) else BgInput)
                                .clickable { emojiSeleccionado = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (categoriaEnEdicion != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                if (nombreCategoria.isNotBlank()) {
                                    scope.launch {
                                        val nombreAnterior = categoriaEnEdicion!!.nombre
                                        val categoriaActualizada = categoriaEnEdicion!!.copy(
                                            nombre = nombreCategoria.trim(),
                                            emoji = emojiSeleccionado
                                        )
                                        // ✅ Pasa el nombre anterior para borrar el doc viejo en Firestore
                                        categoriaRepository.actualizar(categoriaActualizada, nombreAnterior)
                                        // ✅ Renombra en productos si cambió el nombre
                                        if (nombreAnterior != nombreCategoria.trim()) {
                                            viewModel.renombrarCategoria(nombreAnterior, nombreCategoria.trim())
                                        }
                                        mensajeExito = "Guardado correctamente"
                                        mostrarFormulario = false
                                        categoriaEnEdicion = null
                                        nombreCategoria = ""
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue),
                            enabled = nombreCategoria.isNotBlank()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(
                            onClick = { mostrarEliminar = categoriaEnEdicion; mostrarFormulario = false },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Red)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Eliminar", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = { mostrarFormulario = false; categoriaEnEdicion = null; nombreCategoria = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancelar", color = TextSecondary) }
                } else {
                    Button(
                        onClick = {
                            if (nombreCategoria.isNotBlank()) {
                                scope.launch {
                                    val existente = database.categoriaDao().buscarPorNombre(nombreCategoria.trim())
                                    if (existente == null) {
                                        categoriaRepository.insertar(
                                            Categoria(nombre = nombreCategoria.trim(), emoji = emojiSeleccionado)
                                        )
                                        mensajeExito = "Categoría agregada correctamente"
                                        mostrarFormulario = false
                                        nombreCategoria = ""
                                        emojiSeleccionado = EMOJIS_CATEGORIA[0]
                                    } else {
                                        mensajeExito = "Ya existe una categoría con ese nombre"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        enabled = nombreCategoria.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear categoría +", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = { mostrarFormulario = false; nombreCategoria = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancelar", color = TextSecondary) }
                }
            }
        }
    }

    Scaffold(
        containerColor = BgScreen,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    categoriaEnEdicion = null
                    nombreCategoria = ""
                    emojiSeleccionado = EMOJIS_CATEGORIA[0]
                    mostrarFormulario = true
                },
                containerColor = Blue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva categoría")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgScreen)
                .padding(paddingValues)
        ) {
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
                    Text("Categorías", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.weight(1f))
                }
            }

            mensajeExito?.let { msg ->
                LaunchedEffect(msg) { delay(2500); mensajeExito = null }
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (msg.contains("existe")) Yellow.copy(alpha = 0.12f) else Green.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (msg.contains("existe")) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (msg.contains("existe")) Yellow else Green,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, color = if (msg.contains("existe")) Yellow else Green, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (categorias.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗂️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No hay categorías aún", color = TextSecondary, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Toca + para crear tu primera categoría", color = TextHint, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categorias, key = { it.id }) { categoria ->
                        val colorIndex = (categoria.nombre.hashCode() and 0x7FFFFFFF) % COLORES_CATEGORIA.size
                        val color = COLORES_CATEGORIA[colorIndex]
                        val conteo = todosLosProductos.count { it.categoria == categoria.nombre }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    categoriaEnEdicion = categoria
                                    nombreCategoria = categoria.nombre
                                    emojiSeleccionado = categoria.emoji
                                    mostrarFormulario = true
                                },
                            colors = CardDefaults.cardColors(containerColor = BgCard),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(categoria.emoji, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(categoria.nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("$conteo productos", fontSize = 12.sp, color = TextSecondary)
                                }
                                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}