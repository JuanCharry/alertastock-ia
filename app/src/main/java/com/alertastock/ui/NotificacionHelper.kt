package com.alertastock.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.alertastock.R
import com.alertastock.data.model.Producto

object NotificacionHelper {

    private const val CANAL_STOCK = "canal_stock_critico"
    private const val CANAL_VENCIMIENTO = "canal_vencimiento"

    fun crearCanales(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val canalStock = NotificationChannel(
            CANAL_STOCK,
            "Stock crítico",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Productos con stock bajo el mínimo" }

        val canalVencimiento = NotificationChannel(
            CANAL_VENCIMIENTO,
            "Por vencer",
            NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Productos próximos a vencer" }

        manager.createNotificationChannel(canalStock)
        manager.createNotificationChannel(canalVencimiento)
    }

    fun notificarStockCritico(context: Context, productos: List<Producto>) {
        if (productos.isEmpty()) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        productos.forEachIndexed { index, producto ->
            val notificacion = NotificationCompat.Builder(context, CANAL_STOCK)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("⚠️ Stock crítico")
                .setContentText("${producto.nombre}: solo ${producto.stockActual} unidades")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            manager.notify(1000 + index, notificacion)
        }
    }

    fun notificarPorVencer(context: Context, productos: List<Producto>) {
        if (productos.isEmpty()) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        productos.forEachIndexed { index, producto ->
            val notificacion = NotificationCompat.Builder(context, CANAL_VENCIMIENTO)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("📅 Producto por vencer")
                .setContentText("${producto.nombre} vence el ${producto.fechaVencimiento}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            manager.notify(2000 + index, notificacion)
        }
    }
}