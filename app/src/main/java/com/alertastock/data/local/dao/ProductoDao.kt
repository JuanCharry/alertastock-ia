package com.alertastock.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.alertastock.data.model.Producto

@Dao
interface ProductoDao {

    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun obtenerTodos(): LiveData<List<Producto>>

    @Query("""
        SELECT * FROM productos 
        WHERE nombre LIKE '%' || :busqueda || '%' 
        OR codigoBarras LIKE '%' || :busqueda || '%'
    """)
    fun buscar(busqueda: String): LiveData<List<Producto>>

    @Query("SELECT * FROM productos WHERE stockActual <= stockMinimo")
    fun obtenerCriticos(): LiveData<List<Producto>>

    @Query("SELECT * FROM productos WHERE codigoBarras = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): Producto?

    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): Producto?

    // Renombra la categoría en todos los productos que la tengan
    @Query("UPDATE productos SET categoria = :nuevaCategoria WHERE categoria = :categoriaAnterior")
    suspend fun renombrarCategoria(categoriaAnterior: String, nuevaCategoria: String)

    // Elimina la categoría de todos los productos que la tengan (deja el campo vacío)
    @Query("UPDATE productos SET categoria = '' WHERE categoria = :categoria")
    suspend fun eliminarCategoria(categoria: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: Producto)

    @Update
    suspend fun actualizar(producto: Producto)

    @Delete
    suspend fun eliminar(producto: Producto)

    @Query("UPDATE productos SET stockActual = stockActual - :cantidad WHERE id = :id")
    suspend fun descontarStock(id: Int, cantidad: Int)

    // ✅ Borra todos los productos locales para limpiar al cambiar de sesión
    @Query("DELETE FROM productos")
    suspend fun limpiarTodos()

    @Query("SELECT * FROM productos")
    suspend fun obtenerTodosSync(): List<Producto>
}