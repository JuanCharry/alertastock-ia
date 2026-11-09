package com.alertastock.data.ia

import com.alertastock.data.model.Producto
import com.alertastock.data.model.Venta
import com.alertastock.data.remote.PrediccionIaRequest
import com.alertastock.data.repository.parsearItemsDeJson
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Convierte las ventas reales guardadas por AlertaStock en las mismas variables
 * historicas usadas durante el entrenamiento del modelo.
 */
object HistorialIaBuilder {

    data class Resultado(
        val request: PrediccionIaRequest?,
        val semanasConHistorial: Int,
        val mensaje: String? = null
    )

    private val formatoVenta = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    fun construir(producto: Producto, ventas: List<Venta>, hoy: LocalDate = LocalDate.now()): Resultado {
        val ventasProducto = ventas.mapNotNull { venta ->
            val fecha = try {
                formatoVenta.parse(venta.fecha)?.toInstant()
                    ?.atZone(ZoneId.systemDefault())?.toLocalDate()
            } catch (_: Exception) {
                null
            } ?: return@mapNotNull null

            val cantidad = parsearItemsDeJson(venta.productosJson)
                .filter { item ->
                    item.productoId == producto.id ||
                        (producto.codigoBarras.isNotBlank() && item.codigoBarras == producto.codigoBarras)
                }
                .sumOf { it.cantidad }

            if (cantidad <= 0) null else fecha to cantidad
        }

        val inicioSemanaActual = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val iniciosSemanas = (1..8).map { semanasAtras ->
            inicioSemanaActual.minusWeeks(semanasAtras.toLong())
        }

        val ventasPorSemana = iniciosSemanas.associateWith { inicio ->
            val fin = inicio.plusDays(6)
            ventasProducto
                .filter { (fecha, _) -> !fecha.isBefore(inicio) && !fecha.isAfter(fin) }
                .sumOf { it.second }
                .toDouble()
        }

        val semanasConHistorial = ventasPorSemana.values.count { it > 0.0 }
        if (semanasConHistorial < 4) {
            return Resultado(
                request = null,
                semanasConHistorial = semanasConHistorial,
                mensaje = "Historial insuficiente: se requieren ventas registradas en al menos 4 de las ultimas 8 semanas para generar una prediccion confiable."
            )
        }

        val ultimas8 = iniciosSemanas.map { ventasPorSemana[it] ?: 0.0 }
        return Resultado(
            request = construirRequest(producto, ultimas8, hoy),
            semanasConHistorial = semanasConHistorial
        )
    }

    private fun construirRequest(
        producto: Producto,
        ultimas8: List<Double>,
        hoy: LocalDate
    ): PrediccionIaRequest {
        require(ultimas8.size == 8)

        val lag1 = ultimas8[0]
        val lag2 = ultimas8[1]
        val lag3 = ultimas8[2]
        val lag4 = ultimas8[3]
        val promedio4 = ultimas8.take(4).average()
        val promedio8 = ultimas8.average()
        val tendencia4 = lag1 - lag4
        val weekFields = WeekFields.ISO

        return PrediccionIaRequest(
            lag_1 = lag1,
            lag_2 = lag2,
            lag_3 = lag3,
            lag_4 = lag4,
            promedio_movil_4 = promedio4,
            promedio_movil_8 = promedio8,
            tendencia_4 = tendencia4,
            // La app actual aun no captura promociones/descuentos; se usa 0 como valor neutro.
            descuento_pct = 0.0,
            mes = hoy.monthValue,
            semana_del_ano = hoy.get(weekFields.weekOfWeekBasedYear()),
            categoria = producto.categoria,
            stockActual = producto.stockActual,
            stockMinimo = producto.stockMinimo
        )
    }
}
