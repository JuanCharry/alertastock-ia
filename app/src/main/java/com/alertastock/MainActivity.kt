package com.alertastock

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.alertastock.ui.navigation.AlertaStockNavigation
import com.alertastock.ui.theme.AlertaStockTheme
import com.alertastock.ui.theme.BgScreen

class MainActivity : ComponentActivity() {

    // ✅ Launcher para pedir permiso de notificaciones
    private val permisosNotificaciones = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Pide permiso de notificaciones en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permisosNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            AlertaStockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgScreen
                ) {
                    AlertaStockNavigation()
                }
            }
        }
    }
}