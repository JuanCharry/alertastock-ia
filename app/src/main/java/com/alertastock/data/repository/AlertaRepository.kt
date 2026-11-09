package com.alertastock.data.repository

import android.util.Log
import com.alertastock.data.model.Alerta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AlertaRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getUid(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("No hay usuario autenticado")
    }

    private fun alertasRef() = firestore
        .collection("usuarios")
        .document(getUid())
        .collection("alertas")

    suspend fun obtenerAlertas(): List<Alerta> {
        return try {
            val snapshot = alertasRef()
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Alerta::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("AlertaRepository", "Error al obtener alertas", e)
            throw e
        }
    }

    suspend fun obtenerAlertasPorProducto(productoId: String): List<Alerta> {
        return try {
            val snapshot = alertasRef()
                .whereEqualTo("productoId", productoId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Alerta::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e("AlertaRepository", "Error al obtener alertas por producto", e)
            throw e
        }
    }

    suspend fun crearAlerta(alerta: Alerta): Alerta {
        try {
            require(alerta.productoId.isNotBlank()) { "La alerta debe tener un producto asociado" }
            require(alerta.productoNombre.isNotBlank()) { "La alerta debe tener nombre de producto" }

            val docRef = alertasRef().document()
            val alertaConId = alerta.copy(id = docRef.id)

            docRef.set(alertaConId).await()
            return alertaConId
        } catch (e: Exception) {
            Log.e("AlertaRepository", "Error al crear alerta", e)
            throw e
        }
    }

    suspend fun actualizarAlerta(alerta: Alerta) {
        try {
            require(alerta.id.isNotBlank()) { "La alerta no tiene id" }
            require(alerta.productoId.isNotBlank()) { "La alerta debe tener un producto asociado" }
            require(alerta.productoNombre.isNotBlank()) { "La alerta debe tener nombre de producto" }

            val alertaActualizada = alerta.copy(
                fechaActualizacion = System.currentTimeMillis()
            )

            alertasRef()
                .document(alerta.id)
                .set(alertaActualizada)
                .await()
        } catch (e: Exception) {
            Log.e("AlertaRepository", "Error al actualizar alerta", e)
            throw e
        }
    }

    suspend fun guardarAlerta(alerta: Alerta): Alerta {
        return if (alerta.id.isBlank()) {
            crearAlerta(alerta)
        } else {
            actualizarAlerta(alerta)
            alerta
        }
    }

    suspend fun eliminarAlerta(alertaId: String) {
        try {
            require(alertaId.isNotBlank()) { "El id de la alerta está vacío" }

            alertasRef()
                .document(alertaId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("AlertaRepository", "Error al eliminar alerta", e)
            throw e
        }
    }

    suspend fun cambiarEstadoAlerta(alertaId: String, activa: Boolean) {
        try {
            require(alertaId.isNotBlank()) { "El id de la alerta está vacío" }

            alertasRef()
                .document(alertaId)
                .update(
                    mapOf(
                        "activa" to activa,
                        "fechaActualizacion" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            Log.e("AlertaRepository", "Error al cambiar estado de alerta", e)
            throw e
        }
    }
}