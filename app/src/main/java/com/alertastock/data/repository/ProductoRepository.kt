package com.alertastock.data.repository

import com.alertastock.data.local.dao.ProductoDao
import com.alertastock.data.model.Producto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductoRepository(private val productoDao: ProductoDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ✅ Ruta por usuario — cada usuario lee y escribe en su propia colección
    private val coleccionProductos
        get() = firestore
            .collection("usuarios")
            .document(auth.currentUser?.uid ?: "unknown")
            .collection("productos")

    val todosLosProductos = productoDao.obtenerTodos()
    val productosCriticos = productoDao.obtenerCriticos()

    fun buscar(texto: String) = productoDao.buscar(texto)

    suspend fun insertar(producto: Producto) {
        productoDao.insertar(producto)
        try {
            coleccionProductos
                .document(producto.codigoBarras.ifEmpty { producto.nombre })
                .set(producto.toMap())
                .await()
        } catch (e: Exception) {}
    }

    suspend fun actualizar(producto: Producto) {
        productoDao.actualizar(producto)
        try {
            coleccionProductos
                .document(producto.codigoBarras.ifEmpty { producto.nombre })
                .set(producto.toMap())
                .await()
        } catch (e: Exception) {}
    }

    suspend fun eliminar(producto: Producto) {
        productoDao.eliminar(producto)
        try {
            coleccionProductos
                .document(producto.codigoBarras.ifEmpty { producto.nombre })
                .delete()
                .await()
        } catch (e: Exception) {}
    }

    suspend fun buscarPorCodigo(codigo: String): Producto? {
        return productoDao.buscarPorCodigo(codigo)
    }

    suspend fun descontarStock(id: Int, cantidad: Int) {
        // Paso 1: descontar en Room
        productoDao.descontarStock(id, cantidad)
        // Paso 2: obtener producto actualizado y sincronizar con Firestore
        try {
            val productoActualizado = productoDao.obtenerPorId(id) ?: return
            coleccionProductos
                .document(productoActualizado.codigoBarras.ifEmpty { productoActualizado.nombre })
                .update("stockActual", productoActualizado.stockActual)
                .await()
        } catch (e: Exception) {}
    }

    suspend fun actualizarStockMinimo(producto: Producto, nuevoMinimo: Int) {
        val actualizado = producto.copy(stockMinimo = nuevoMinimo)
        productoDao.actualizar(actualizado)
        try {
            coleccionProductos
                .document(producto.codigoBarras.ifEmpty { producto.nombre })
                .set(actualizado.toMap())
                .await()
        } catch (e: Exception) {}
    }

    suspend fun renombrarCategoria(categoriaAnterior: String, nuevaCategoria: String) {
        // Actualiza en Room
        productoDao.renombrarCategoria(categoriaAnterior, nuevaCategoria)
        // Actualiza en Firestore — busca todos los productos con esa categoría y los actualiza
        try {
            val documentos = coleccionProductos
                .whereEqualTo("categoria", categoriaAnterior)
                .get()
                .await()
            for (doc in documentos) {
                doc.reference.update("categoria", nuevaCategoria).await()
            }
        } catch (e: Exception) {}
    }

    suspend fun eliminarCategoria(categoria: String) {
        // Actualiza en Room — deja la categoría vacía
        productoDao.eliminarCategoria(categoria)
        // Actualiza en Firestore
        try {
            val documentos = coleccionProductos
                .whereEqualTo("categoria", categoria)
                .get()
                .await()
            for (doc in documentos) {
                doc.reference.update("categoria", "").await()
            }
        } catch (e: Exception) {}
    }

    suspend fun obtenerTodosSync(): List<Producto> {
        return productoDao.obtenerTodosSync()
    }

    // Limpia Room para evitar datos de sesión anterior
    suspend fun limpiarProductosLocales() {
        productoDao.limpiarTodos()
    }

    suspend fun sincronizarDesdeFirestore() {
        try {
            val documentos = coleccionProductos.get().await()
            for (doc in documentos) {
                val codigoBarras = doc.getString("codigoBarras") ?: ""
                val nombre = doc.getString("nombre") ?: ""
                val existente = productoDao.buscarPorCodigo(codigoBarras)
                val producto = Producto(
                    id = existente?.id ?: 0,
                    nombre = nombre,
                    categoria = doc.getString("categoria") ?: "",
                    stockActual = (doc.getLong("stockActual") ?: 0).toInt(),
                    stockMinimo = (doc.getLong("stockMinimo") ?: 0).toInt(),
                    precioCompra = doc.getDouble("precioCompra") ?: 0.0,
                    precioVenta = doc.getDouble("precioVenta") ?: 0.0,
                    codigoBarras = codigoBarras,
                    fechaVencimiento = doc.getString("fechaVencimiento") ?: "",
                    emoji = doc.getString("emoji") ?: "📦"
                )
                productoDao.insertar(producto)
            }
        } catch (e: Exception) {}
    }
}

fun Producto.toMap(): Map<String, Any> = mapOf(
    "nombre" to nombre,
    "categoria" to categoria,
    "stockActual" to stockActual,
    "stockMinimo" to stockMinimo,
    "precioCompra" to precioCompra,
    "precioVenta" to precioVenta,
    "codigoBarras" to codigoBarras,
    "fechaVencimiento" to fechaVencimiento,
    "emoji" to emoji


)