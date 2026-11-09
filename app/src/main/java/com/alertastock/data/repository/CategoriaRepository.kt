package com.alertastock.data.repository

import com.alertastock.data.local.dao.CategoriaDao
import com.alertastock.data.model.Categoria
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CategoriaRepository(private val categoriaDao: CategoriaDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val coleccionCategorias
        get() = firestore
            .collection("usuarios")
            .document(auth.currentUser?.uid ?: "unknown")
            .collection("categorias")

    val todasLasCategorias = categoriaDao.obtenerTodas()

    suspend fun insertar(categoria: Categoria) {
        categoriaDao.insertar(categoria)
        try {
            coleccionCategorias
                .document(categoria.nombre)
                .set(mapOf("nombre" to categoria.nombre, "emoji" to categoria.emoji))
                .await()
        } catch (e: Exception) {}
    }

    // ✅ Recibe el nombre anterior para borrar el documento viejo en Firestore
    suspend fun actualizar(categoria: Categoria, nombreAnterior: String) {
        categoriaDao.actualizar(categoria)
        try {
            // Borra el documento con el nombre viejo
            if (nombreAnterior != categoria.nombre) {
                coleccionCategorias.document(nombreAnterior).delete().await()
            }
            // Crea o actualiza el documento con el nombre nuevo
            coleccionCategorias
                .document(categoria.nombre)
                .set(mapOf("nombre" to categoria.nombre, "emoji" to categoria.emoji))
                .await()
        } catch (e: Exception) {}
    }

    suspend fun eliminar(categoria: Categoria) {
        categoriaDao.eliminar(categoria)
        try {
            coleccionCategorias
                .document(categoria.nombre)
                .delete()
                .await()
        } catch (e: Exception) {}
    }

    suspend fun sincronizarDesdeFirestore() {
        try {
            val documentos = coleccionCategorias.get().await()
            for (doc in documentos) {
                val nombre = doc.getString("nombre") ?: ""
                val emoji = doc.getString("emoji") ?: "📦"
                if (nombre.isNotBlank()) {
                    val existente = categoriaDao.buscarPorNombre(nombre)
                    categoriaDao.insertar(
                        Categoria(
                            id = existente?.id ?: 0,
                            nombre = nombre,
                            emoji = emoji
                        )
                    )
                }
            }
        } catch (e: Exception) {}
    }
}