package com.alertastock.ui.ia

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alertastock.data.ia.IaRepository
import com.alertastock.data.local.database.AlertaStockDatabase
import com.alertastock.data.model.Producto
import com.alertastock.data.remote.PrediccionIaResponse
import com.alertastock.data.repository.VentaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IaViewModel(application: Application) : AndroidViewModel(application) {

    data class EstadoProductoIa(
        val cargando: Boolean = false,
        val resultado: PrediccionIaResponse? = null,
        val error: String? = null,
        val semanasConHistorial: Int = 0
    )

    private val repository: IaRepository

    private val _estados = MutableStateFlow<Map<Int, EstadoProductoIa>>(emptyMap())
    val estados: StateFlow<Map<Int, EstadoProductoIa>> = _estados.asStateFlow()

    init {
        val database = AlertaStockDatabase.getDatabase(application)
        repository = IaRepository(VentaRepository(database.ventaDao()))
    }

    fun analizar(producto: Producto) = viewModelScope.launch {
        actualizar(producto.id, EstadoProductoIa(cargando = true))
        val analisis = repository.analizar(producto)
        actualizar(
            producto.id,
            EstadoProductoIa(
                cargando = false,
                resultado = analisis.resultado,
                error = analisis.error,
                semanasConHistorial = analisis.semanasConHistorial
            )
        )
    }

    private fun actualizar(productoId: Int, estado: EstadoProductoIa) {
        _estados.value = _estados.value.toMutableMap().apply {
            put(productoId, estado)
        }
    }
}
