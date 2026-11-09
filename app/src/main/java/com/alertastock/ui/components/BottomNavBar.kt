package com.alertastock.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.alertastock.ui.theme.*

enum class BottomNavDestino {
    INICIO, PRODUCTOS, ESCANEAR, ALERTAS
}

data class ItemNavBar(
    val destino: BottomNavDestino,
    val etiqueta: String,
    val icono: ImageVector
)

@Composable
fun AlertaStockBottomBar(
    destinoActual: BottomNavDestino,
    onInicioClick: () -> Unit = {},
    onProductosClick: () -> Unit = {},
    onEscanearClick: () -> Unit = {},
    onAlertasClick: () -> Unit = {}
) {
    val items = listOf(
        ItemNavBar(BottomNavDestino.INICIO,    "Inicio",    Icons.Default.Home),
        ItemNavBar(BottomNavDestino.PRODUCTOS, "Productos", Icons.Default.Inventory),
        ItemNavBar(BottomNavDestino.ESCANEAR,  "Escanear",  Icons.Default.QrCodeScanner),
        ItemNavBar(BottomNavDestino.ALERTAS,   "Alertas",   Icons.Default.Notifications)
    )

    NavigationBar(containerColor = BgCard) {
        items.forEach { item ->
            NavigationBarItem(
                selected = destinoActual == item.destino,
                onClick = {
                    when (item.destino) {
                        BottomNavDestino.INICIO    -> onInicioClick()
                        BottomNavDestino.PRODUCTOS -> onProductosClick()
                        BottomNavDestino.ESCANEAR  -> onEscanearClick()
                        BottomNavDestino.ALERTAS   -> onAlertasClick()
                    }
                },
                icon = {
                    Icon(item.icono, contentDescription = item.etiqueta)
                },
                label = { Text(item.etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Blue,
                    selectedTextColor = Blue,
                    indicatorColor = Blue.copy(alpha = 0.15f),
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}