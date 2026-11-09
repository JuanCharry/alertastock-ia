package com.alertastock.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alertastock.data.local.dao.CategoriaDao
import com.alertastock.data.local.dao.ProductoDao
import com.alertastock.data.local.dao.VentaDao
import com.alertastock.data.model.Categoria
import com.alertastock.data.model.Producto
import com.alertastock.data.model.Venta

@Database(
    entities = [Producto::class, Venta::class, Categoria::class],  // ✅ agregamos Categoria
    version = 3,                                                     // ✅ subimos a versión 3
    exportSchema = false
)
abstract class AlertaStockDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun ventaDao(): VentaDao
    abstract fun categoriaDao(): CategoriaDao  // ✅ nuevo DAO

    companion object {

        @Volatile
        private var INSTANCE: AlertaStockDatabase? = null

        fun getDatabase(context: Context): AlertaStockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlertaStockDatabase::class.java,
                    "alertastock_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}