package com.alertastock.ui.product

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alertastock.data.local.database.AlertaStockDatabase
import com.alertastock.data.model.Producto
import com.alertastock.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.alertastock.ui.NotificacionHelper

class ProductoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductoRepository

    val todosLosProductos by lazy { repository.todosLosProductos }
    val productosCriticos by lazy { repository.productosCriticos }

    var productoSeleccionado: Producto? by mutableStateOf(null)
        private set

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        val dao = AlertaStockDatabase.getDatabase(application).productoDao()
        repository = ProductoRepository(dao)
        // ✅ Crea los canales de notificación al iniciar
        NotificacionHelper.crearCanales(application)
    }
    fun insertar(producto: Producto) = viewModelScope.launch {
        try {
            _cargando.value = true
            repository.insertar(producto)
        } catch (e: Exception) {
            _error.value = "Error al guardar: ${e.message}"
        } finally {
            _cargando.value = false
        }
    }

    fun actualizar(producto: Producto) = viewModelScope.launch {
        try {
            _cargando.value = true
            repository.actualizar(producto)
        } catch (e: Exception) {
            _error.value = "Error al actualizar: ${e.message}"
        } finally {
            _cargando.value = false
        }
    }

    fun eliminar(producto: Producto) = viewModelScope.launch {
        try {
            repository.eliminar(producto)
        } catch (e: Exception) {
            _error.value = "Error al eliminar: ${e.message}"
        }
    }

    fun descontarStock(id: Int, cantidad: Int) = viewModelScope.launch {
        try {
            repository.descontarStock(id, cantidad)
        } catch (e: Exception) {
            _error.value = "Error al descontar stock: ${e.message}"
        }
    }

    fun buscar(texto: String) = repository.buscar(texto)

    fun seleccionarProducto(producto: Producto) {
        productoSeleccionado = producto
    }

    fun limpiarSeleccion() {
        productoSeleccionado = null
    }

    fun sincronizar() = viewModelScope.launch {
        try {
            _cargando.value = true
            repository.limpiarProductosLocales()
            repository.sincronizarDesdeFirestore()

            // ✅ Notifica productos críticos y por vencer
            val productos = repository.obtenerTodosSync()
            val criticos = productos.filter { it.stockActual <= it.stockMinimo }
            val porVencer = productos.filter { it.estaVenciendo }
            NotificacionHelper.notificarStockCritico(getApplication(), criticos)
            NotificacionHelper.notificarPorVencer(getApplication(), porVencer)
        } catch (e: Exception) {
            _error.value = "Error al sincronizar: ${e.message}"
        } finally {
            _cargando.value = false
        }
    }

    fun renombrarCategoria(categoriaAnterior: String, nuevaCategoria: String) = viewModelScope.launch {
        try {
            repository.renombrarCategoria(categoriaAnterior, nuevaCategoria)
        } catch (e: Exception) {
            _error.value = "Error al renombrar categoría: ${e.message}"
        }
    }

    fun eliminarCategoria(categoria: String) = viewModelScope.launch {
        try {
            repository.eliminarCategoria(categoria)
        } catch (e: Exception) {
            _error.value = "Error al eliminar categoría: ${e.message}"
        }
    }

    fun limpiarError() {
        _error.value = null
    }
}