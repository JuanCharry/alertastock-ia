package com.alertastock.ui.venta

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alertastock.data.local.database.AlertaStockDatabase
import com.alertastock.data.model.ItemCanasta
import com.alertastock.data.model.Producto
import com.alertastock.data.repository.ProductoRepository
import com.alertastock.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VentaViewModel(application: Application) : AndroidViewModel(application) {

    private val ventaRepository: VentaRepository
    private val productoRepository: ProductoRepository

    // Canasta actual — lista de items que el usuario va agregando
    private val _canasta = MutableStateFlow<List<ItemCanasta>>(emptyList())
    val canasta: StateFlow<List<ItemCanasta>> = _canasta.asStateFlow()

    // Total de la canasta
    val total: Double get() = _canasta.value.sumOf { it.subtotal }

    // Estado de carga
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    // Última venta guardada (para mostrar en el resumen)
    private val _ultimaVenta = MutableStateFlow<List<ItemCanasta>>(emptyList())
    val ultimaVenta: StateFlow<List<ItemCanasta>> = _ultimaVenta.asStateFlow()

    private val _totalUltimaVenta = MutableStateFlow(0.0)
    val totalUltimaVenta: StateFlow<Double> = _totalUltimaVenta.asStateFlow()

    init {
        val dao = AlertaStockDatabase.getDatabase(application)
        ventaRepository = VentaRepository(dao.ventaDao())
        productoRepository = ProductoRepository(dao.productoDao())
    }

    // Agregar producto a la canasta
    // Si ya existe, aumenta la cantidad en 1
    fun agregarACanasta(producto: Producto) {
        val canastaActual = _canasta.value.toMutableList()
        val itemExistente = canastaActual.find { it.productoId == producto.id }

        if (itemExistente != null) {
            // Ya está en la canasta — aumentar cantidad
            val index = canastaActual.indexOf(itemExistente)
            canastaActual[index] = itemExistente.copy(cantidad = itemExistente.cantidad + 1)
        } else {
            // No está — agregar nuevo item
            canastaActual.add(
                ItemCanasta(
                    productoId = producto.id,
                    nombre = producto.nombre,
                    emoji = producto.emoji,
                    codigoBarras = producto.codigoBarras,
                    precioVenta = producto.precioVenta,
                    cantidad = 1
                )
            )
        }
        _canasta.value = canastaActual
    }

    // Eliminar un item de la canasta
    fun eliminarDeCanasta(item: ItemCanasta) {
        _canasta.value = _canasta.value.filter { it.productoId != item.productoId }
    }

    // Cambiar cantidad de un item
    fun cambiarCantidad(item: ItemCanasta, nuevaCantidad: Int) {
        if (nuevaCantidad <= 0) {
            eliminarDeCanasta(item)
            return
        }
        val canastaActual = _canasta.value.toMutableList()
        val index = canastaActual.indexOf(item)
        if (index >= 0) {
            canastaActual[index] = item.copy(cantidad = nuevaCantidad)
            _canasta.value = canastaActual
        }
    }

    // Finalizar compra:
    // 1. Guarda la venta en Room y Firestore
    // 2. Descuenta el stock de cada producto
    // 3. Limpia la canasta
    fun finalizarCompra() = viewModelScope.launch {
        try {
            _cargando.value = true
            val itemsActuales = _canasta.value

            // Guardar la venta
            ventaRepository.guardarVenta(itemsActuales, total)

            // Descontar stock de cada producto vendido
            itemsActuales.forEach { item ->
                productoRepository.descontarStock(item.productoId, item.cantidad)
            }

            // Guardar copia para el resumen antes de limpiar
            _ultimaVenta.value = itemsActuales
            _totalUltimaVenta.value = total

            // Limpiar canasta
            _canasta.value = emptyList()

        } catch (e: Exception) {
            // Manejar error
        } finally {
            _cargando.value = false
        }
    }

    // Limpiar canasta sin finalizar
    fun limpiarCanasta() {
        _canasta.value = emptyList()
    }
}