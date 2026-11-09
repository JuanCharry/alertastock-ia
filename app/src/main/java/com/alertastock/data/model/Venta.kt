package com.alertastock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Representa una venta completa (el ticket)
@Entity(tableName = "ventas")
data class Venta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fecha: String,           // Fecha en formato "dd/MM/yyyy HH:mm"
    val productosJson: String,   // Lista de productos como JSON
    val total: Double            // Total de la venta
)

// Representa un producto dentro de la canasta
// No se guarda en Room directamente — se convierte a JSON dentro de Venta
data class ItemCanasta(
    val productoId: Int,
    val nombre: String,
    val emoji: String,
    val codigoBarras: String,
    val precioVenta: Double,
    val cantidad: Int
) {
    // Precio total de este item (precio × cantidad)
    val subtotal: Double get() = precioVenta * cantidad
}