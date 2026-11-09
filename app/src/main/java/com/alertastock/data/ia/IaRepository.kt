package com.alertastock.data.ia

import com.alertastock.data.model.Producto
import com.alertastock.data.remote.IaApiClient
import com.alertastock.data.remote.PrediccionIaResponse
import com.alertastock.data.repository.VentaRepository

class IaRepository(private val ventaRepository: VentaRepository) {

    data class AnalisisIa(
        val resultado: PrediccionIaResponse? = null,
        val semanasConHistorial: Int = 0,
        val error: String? = null
    )

    suspend fun analizar(producto: Producto): AnalisisIa {
        val ventas = ventaRepository.obtenerTodasParaIaSync()
        val datos = HistorialIaBuilder.construir(producto, ventas)
        val request = datos.request ?: return AnalisisIa(
            semanasConHistorial = datos.semanasConHistorial,
            error = datos.mensaje
        )

        return try {
            val response = IaApiClient.service.predecir(request)
            AnalisisIa(
                resultado = response,
                semanasConHistorial = datos.semanasConHistorial
            )
        } catch (e: Exception) {
            AnalisisIa(
                semanasConHistorial = datos.semanasConHistorial,
                error = "No fue posible conectar con la API de IA: ${e.message ?: "error desconocido"}"
            )
        }
    }
}
