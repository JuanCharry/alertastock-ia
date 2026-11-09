package com.alertastock.data.repository

import com.alertastock.data.local.dao.VentaDao
import com.alertastock.data.model.ItemCanasta
import com.alertastock.data.model.Venta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class VentaRepository(private val ventaDao: VentaDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Ventas separadas por usuario. Evita mezclar historiales de negocios distintos.
    private val coleccionVentas
        get() = firestore
            .collection("usuarios")
            .document(auth.currentUser?.uid ?: "unknown")
            .collection("ventas")

    val todasLasVentas = ventaDao.obtenerTodas()

    fun obtenerPorFecha(fecha: String) = ventaDao.obtenerPorFecha(fecha)

    suspend fun obtenerTodasSync(): List<Venta> = ventaDao.obtenerTodasSync()

    /**
     * Historial utilizable por la IA. Combina las ventas locales de Room con las
     * ventas almacenadas en Firestore y elimina duplicados. Esto permite que un
     * historial cargado en Firebase pueda utilizarse sin alterar la base local.
     */
    suspend fun obtenerTodasParaIaSync(): List<Venta> {
        val locales = ventaDao.obtenerTodasSync()

        val remotas = try {
            coleccionVentas.get().await().documents.mapNotNull { doc ->
                val fecha = doc.getString("fecha") ?: return@mapNotNull null
                val total = doc.getDouble("total")
                    ?: (doc.getLong("total")?.toDouble() ?: 0.0)

                @Suppress("UNCHECKED_CAST")
                val productos = doc.get("productos") as? List<Map<String, Any?>>
                    ?: emptyList()

                val jsonArray = JSONArray()
                productos.forEach { item ->
                    val obj = JSONObject()
                    obj.put("productoId", (item["productoId"] as? Number)?.toInt() ?: 0)
                    obj.put("nombre", item["nombre"]?.toString() ?: "")
                    obj.put("emoji", item["emoji"]?.toString() ?: "📦")
                    obj.put("codigoBarras", item["codigoBarras"]?.toString() ?: "")
                    obj.put("precioVenta", (item["precioVenta"] as? Number)?.toDouble() ?: 0.0)
                    obj.put("cantidad", (item["cantidad"] as? Number)?.toInt() ?: 0)
                    jsonArray.put(obj)
                }

                Venta(
                    fecha = fecha,
                    productosJson = jsonArray.toString(),
                    total = total
                )
            }
        } catch (_: Exception) {
            emptyList()
        }

        return (locales + remotas).distinctBy { venta -> claveVentaIa(venta) }
    }

    private fun claveVentaIa(venta: Venta): String {
        val items = parsearItemsDeJson(venta.productosJson)
            .sortedWith(compareBy<ItemCanasta> { it.codigoBarras }.thenBy { it.productoId })
            .joinToString(";") { item ->
                val identificador = item.codigoBarras.ifBlank { item.productoId.toString() }
                "$identificador:${item.cantidad}:${item.precioVenta}"
            }

        return "${venta.fecha}|${venta.total}|$items"
    }

    // GUARDAR VENTA - guarda en Room y en Firestore
    suspend fun guardarVenta(items: List<ItemCanasta>, total: Double) {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // Convertir lista de items a JSON para guardar en Room
        val jsonArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("productoId", item.productoId)
            obj.put("nombre", item.nombre)
            obj.put("emoji", item.emoji)
            obj.put("codigoBarras", item.codigoBarras)
            obj.put("precioVenta", item.precioVenta)
            obj.put("cantidad", item.cantidad)
            jsonArray.put(obj)
        }

        val venta = Venta(
            fecha = fecha,
            productosJson = jsonArray.toString(),
            total = total
        )

        // Guardar en Room
        ventaDao.insertar(venta)

        // Guardar en Firestore. productoId es clave para reconstruir el historial de IA.
        try {
            val mapa = hashMapOf<String, Any>(
                "fecha" to fecha,
                "timestamp" to FieldValue.serverTimestamp(),
                "total" to total,
                "vendedor" to (auth.currentUser?.email ?: "desconocido"),
                "uid" to (auth.currentUser?.uid ?: "unknown"),
                "productos" to items.map { item ->
                    hashMapOf(
                        "productoId" to item.productoId,
                        "nombre" to item.nombre,
                        "codigoBarras" to item.codigoBarras,
                        "cantidad" to item.cantidad,
                        "precioVenta" to item.precioVenta,
                        "subtotal" to item.subtotal
                    )
                }
            )
            coleccionVentas.add(mapa).await()
        } catch (_: Exception) {
            // Si falla Firestore, Room ya conserva la venta para operacion local.
        }
    }
}

// Convierte el JSON guardado en Room de vuelta a lista de ItemCanasta
fun parsearItemsDeJson(json: String): List<ItemCanasta> {
    return try {
        val array = JSONArray(json)
        val lista = mutableListOf<ItemCanasta>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            lista.add(
                ItemCanasta(
                    productoId = obj.getInt("productoId"),
                    nombre = obj.getString("nombre"),
                    emoji = obj.getString("emoji"),
                    codigoBarras = obj.getString("codigoBarras"),
                    precioVenta = obj.getDouble("precioVenta"),
                    cantidad = obj.getInt("cantidad")
                )
            )
        }
        lista
    } catch (_: Exception) {
        emptyList()
    }
}
