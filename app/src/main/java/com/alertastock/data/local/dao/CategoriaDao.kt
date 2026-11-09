package com.alertastock.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.alertastock.data.model.Categoria

@Dao
interface CategoriaDao {

    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    fun obtenerTodas(): LiveData<List<Categoria>>

    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    suspend fun obtenerTodasSuspend(): List<Categoria>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(categoria: Categoria)

    @Update
    suspend fun actualizar(categoria: Categoria)

    @Delete
    suspend fun eliminar(categoria: Categoria)

    @Query("SELECT * FROM categorias WHERE nombre = :nombre LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): Categoria?
}