package com.alertastock.data.model

enum class TipoAlerta {
    STOCK_BAJO,
    POR_VENCER,
    PERSONALIZADA
}

data class Alerta(
    val id: String = "",
    val productoId: String = "",
    val productoNombre: String = "",
    val tipo: TipoAlerta = TipoAlerta.STOCK_BAJO,
    val stockMinimo: Int = 0,
    val diasPreviosVencimiento: Int = 0,
    val mensaje: String = "",
    val activa: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaActualizacion: Long = System.currentTimeMillis()
)