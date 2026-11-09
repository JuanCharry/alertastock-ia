package com.alertastock.ui.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.alertastock.data.model.Alerta
import com.alertastock.data.model.Producto
import com.alertastock.data.model.TipoAlerta
import com.alertastock.data.repository.AlertaRepository
import com.alertastock.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AlertaUiState {
    object Inactivo : AlertaUiState()
    object Cargando : AlertaUiState()
    data class Exitoso(val mensaje: String) : AlertaUiState()
    data class Error(val mensaje: String) : AlertaUiState()
}

class AlertaViewModel(
    private val alertaRepository: AlertaRepository,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _alertas = MutableStateFlow<List<Alerta>>(emptyList())
    val alertas: StateFlow<List<Alerta>> = _alertas.asStateFlow()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    private val _uiState = MutableStateFlow<AlertaUiState>(AlertaUiState.Inactivo)
    val uiState: StateFlow<AlertaUiState> = _uiState.asStateFlow()

    private val _alertaEnEdicion = MutableStateFlow<Alerta?>(null)
    val alertaEnEdicion: StateFlow<Alerta?> = _alertaEnEdicion.asStateFlow()

    init {
        cargarAlertas()
        cargarProductos()
    }

    fun cargarAlertas() {
        viewModelScope.launch {
            _uiState.value = AlertaUiState.Cargando
            try {
                _alertas.value = alertaRepository.obtenerAlertas()
                _uiState.value = AlertaUiState.Inactivo
            } catch (e: Exception) {
                _uiState.value = AlertaUiState.Error(
                    e.message ?: "Error al cargar alertas"
                )
            }
        }
    }

    fun cargarProductos() {
        // Sincronizar desde Firestore en background
        viewModelScope.launch {
            try {
                productoRepository.sincronizarDesdeFirestore()
            } catch (_: Exception) {}
        }
        // ✅ Convierte el LiveData a Flow para observarlo correctamente desde el ViewModel
        viewModelScope.launch {
            productoRepository.todosLosProductos.asFlow().collect { lista ->
                _productos.value = lista ?: emptyList()
            }
        }
    }

    fun seleccionarAlerta(alerta: Alerta) {
        _alertaEnEdicion.value = alerta
    }

    fun limpiarAlertaSeleccionada() {
        _alertaEnEdicion.value = null
    }

    fun guardarAlerta(
        alertaId: String = "",
        producto: Producto?,
        tipo: TipoAlerta,
        stockMinimoTexto: String,
        diasPreviosTexto: String,
        mensaje: String,
        activa: Boolean = true
    ) {
        if (producto == null) {
            _uiState.value = AlertaUiState.Error("Debes seleccionar un producto")
            return
        }

        val stockMinimo = stockMinimoTexto.toIntOrNull() ?: 0
        val diasPrevios = diasPreviosTexto.toIntOrNull() ?: 0

        when (tipo) {
            TipoAlerta.STOCK_BAJO -> {
                if (stockMinimo <= 0) {
                    _uiState.value = AlertaUiState.Error("El stock mínimo debe ser mayor a 0")
                    return
                }
            }
            TipoAlerta.POR_VENCER -> {
                if (diasPrevios <= 0) {
                    _uiState.value = AlertaUiState.Error("Los días previos deben ser mayor a 0")
                    return
                }
            }
            TipoAlerta.PERSONALIZADA -> {
                if (mensaje.isBlank()) {
                    _uiState.value = AlertaUiState.Error("Escribe un mensaje para la alerta")
                    return
                }
            }
        }

        val alerta = Alerta(
            id = alertaId,
            productoId = producto.id.toString(),
            productoNombre = producto.nombre,
            tipo = tipo,
            stockMinimo = stockMinimo,
            diasPreviosVencimiento = diasPrevios,
            mensaje = mensaje,
            activa = activa,
            fechaCreacion = if (alertaId.isBlank()) System.currentTimeMillis()
            else (_alertaEnEdicion.value?.fechaCreacion ?: System.currentTimeMillis()),
            fechaActualizacion = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _uiState.value = AlertaUiState.Cargando
            try {
                alertaRepository.guardarAlerta(alerta)

                if (tipo == TipoAlerta.STOCK_BAJO && stockMinimo > 0) {
                    productoRepository.actualizarStockMinimo(producto, stockMinimo)
                }

                cargarAlertas()
                limpiarAlertaSeleccionada()

                _uiState.value = AlertaUiState.Exitoso(
                    if (alertaId.isBlank()) "Alerta creada correctamente"
                    else "Alerta actualizada correctamente"
                )
            } catch (e: Exception) {
                _uiState.value = AlertaUiState.Error(
                    e.message ?: "Error al guardar alerta"
                )
            }
        }
    }

    fun eliminarAlerta(alertaId: String) {
        if (alertaId.isBlank()) {
            _uiState.value = AlertaUiState.Error("La alerta no tiene id")
            return
        }

        viewModelScope.launch {
            _uiState.value = AlertaUiState.Cargando
            try {
                alertaRepository.eliminarAlerta(alertaId)
                cargarAlertas()
                _uiState.value = AlertaUiState.Exitoso("Alerta eliminada correctamente")
            } catch (e: Exception) {
                _uiState.value = AlertaUiState.Error(
                    e.message ?: "Error al eliminar alerta"
                )
            }
        }
    }

    fun cambiarEstadoAlerta(alertaId: String, activa: Boolean) {
        if (alertaId.isBlank()) {
            _uiState.value = AlertaUiState.Error("La alerta no tiene id")
            return
        }

        viewModelScope.launch {
            _uiState.value = AlertaUiState.Cargando
            try {
                alertaRepository.cambiarEstadoAlerta(alertaId, activa)
                cargarAlertas()
                _uiState.value = AlertaUiState.Exitoso(
                    if (activa) "Alerta activada" else "Alerta desactivada"
                )
            } catch (e: Exception) {
                _uiState.value = AlertaUiState.Error(
                    e.message ?: "Error al cambiar estado de alerta"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AlertaUiState.Inactivo
    }
}

class AlertaViewModelFactory(
    private val alertaRepository: AlertaRepository,
    private val productoRepository: ProductoRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AlertaViewModel::class.java)) {
            return AlertaViewModel(alertaRepository, productoRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}