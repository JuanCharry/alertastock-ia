package com.alertastock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val categoria: String,
    val stockActual: Int,
    val stockMinimo: Int,
    val precioCompra: Double,
    val precioVenta: Double,
    val codigoBarras: String = "",
    val fechaVencimiento: String = "",
    val emoji: String = "📦"
) {
    // Calcula si el producto vence en los próximos 30 días
    // No se guarda en la BD, se recalcula cada vez que se usa
    val estaVenciendo: Boolean
        get() {
            if (fechaVencimiento.isBlank()) return false
            return try {
                val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fecha = formato.parse(fechaVencimiento) ?: return false
                val diferenciaDias = (fecha.time - Date().time) / (1000 * 60 * 60 * 24)
                diferenciaDias <= 30
            } catch (e: Exception) {
                false
            }
        }
}