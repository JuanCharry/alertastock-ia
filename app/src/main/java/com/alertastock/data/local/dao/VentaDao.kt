package com.alertastock.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.alertastock.data.model.Venta

@Dao
interface VentaDao {

    // Insertar una venta nueva
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(venta: Venta)

    // Obtener todas las ventas ordenadas por más reciente
    @Query("SELECT * FROM ventas ORDER BY id DESC")
    fun obtenerTodas(): LiveData<List<Venta>>

    // Obtener ventas de una fecha específica
    @Query("SELECT * FROM ventas WHERE fecha LIKE :fecha || '%' ORDER BY id DESC")
    fun obtenerPorFecha(fecha: String): LiveData<List<Venta>>

    // Obtener todas las ventas de forma suspendida para construir variables de IA
    @Query("SELECT * FROM ventas ORDER BY id ASC")
    suspend fun obtenerTodasSync(): List<Venta>

    // Eliminar todas las ventas (para limpiar historial)
    @Query("DELETE FROM ventas")
    suspend fun limpiarTodo()
}