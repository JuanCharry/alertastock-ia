package com.alertastock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alertastock.ui.alert.screen.AlertasScreen
import com.alertastock.ui.auth.screens.LoginScreen
import com.alertastock.ui.auth.screens.RegistroScreen
import com.alertastock.ui.auth.screens.OlvideContrasenaScreen
import com.alertastock.ui.auth.screens.VerificarCorreoScreen
import com.alertastock.ui.auth.screens.CuentaCreadaScreen
import com.alertastock.ui.configuracion.CambiarContrasenaScreen
import com.alertastock.ui.configuracion.CategoriasScreen
import com.alertastock.ui.configuracion.ConfiguracionScreen
import com.alertastock.ui.configuracion.EditarPerfilScreen
import com.alertastock.ui.configuracion.NotificacionesScreen
import com.alertastock.ui.dashboard.screens.DashboardScreen
import com.alertastock.ui.product.ProductoViewModel
import com.alertastock.ui.product.screen.AgregarEditarProductoScreen
import com.alertastock.ui.product.screen.ProductosScreen
import com.alertastock.ui.scanner.ScannerScreen
import com.alertastock.ui.venta.CajaScreen
import com.alertastock.ui.venta.ResumenCompraScreen
import com.alertastock.ui.venta.VentaViewModel
import com.google.firebase.auth.FirebaseAuth

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val OLVIDE_CONTRASENA = "olvide_contrasena"
    const val VERIFICAR_CORREO = "verificar_correo"
    const val CUENTA_CREADA = "cuenta_creada"
    const val DASHBOARD = "dashboard"
    const val PRODUCTOS = "productos"
    const val AGREGAR_PRODUCTO = "agregar_producto"
    const val SCANNER = "scanner"
    const val ALERTAS = "alertas"
    const val CAJA = "caja"
    const val RESUMEN_COMPRA = "resumen_compra"
    const val CONFIGURACION = "configuracion"
    const val EDITAR_PERFIL = "editar_perfil"
    const val CAMBIAR_CONTRASENA = "cambiar_contrasena"
    const val NOTIFICACIONES = "notificaciones"
    const val CATEGORIAS = "categorias"
}

@Composable
fun AlertaStockNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val productoViewModel: ProductoViewModel = viewModel()
    val ventaViewModel: VentaViewModel = viewModel()
    val startDestination = if (auth.currentUser != null) Rutas.DASHBOARD else Rutas.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Rutas.DASHBOARD) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                onRegistro = { navController.navigate(Rutas.REGISTRO) },
                onOlvideContrasena = { navController.navigate(Rutas.OLVIDE_CONTRASENA) }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onRegistroExitoso = { navController.navigate(Rutas.VERIFICAR_CORREO) },
                onAtras = { navController.popBackStack() }
            )
        }

        composable(Rutas.VERIFICAR_CORREO) {
            VerificarCorreoScreen(
                onVerificado = { navController.navigate(Rutas.CUENTA_CREADA) },
                onAtras = { navController.popBackStack() }
            )
        }

        composable(Rutas.CUENTA_CREADA) {
            CuentaCreadaScreen(
                onIrDashboard = {
                    navController.navigate(Rutas.DASHBOARD) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.OLVIDE_CONTRASENA) {
            OlvideContrasenaScreen(
                onCorreoEnviado = { navController.popBackStack() },
                onAtras = { navController.popBackStack() }
            )
        }

        composable(Rutas.DASHBOARD) {
            DashboardScreen(
                viewModel = productoViewModel,
                onProductos = { navController.navigate("${Rutas.PRODUCTOS}?filtro=TODOS") },
                onProductosCriticos = { navController.navigate("${Rutas.PRODUCTOS}?filtro=CRITICO") },
                onProductosBajos = { navController.navigate("${Rutas.PRODUCTOS}?filtro=BAJO") },
                onProductosPorVencer = { navController.navigate("${Rutas.PRODUCTOS}?filtro=POR_VENCER") },
                onProductosBuenEstado = { navController.navigate("${Rutas.PRODUCTOS}?filtro=BUEN_ESTADO") },
                onEscanear = { navController.navigate(Rutas.SCANNER) },
                onAlertas = { navController.navigate(Rutas.ALERTAS) },
                onConfigurar = { navController.navigate(Rutas.CONFIGURACION) },
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Rutas.PRODUCTOS}?filtro={filtro}",
            arguments = listOf(navArgument("filtro") { type = NavType.StringType; defaultValue = "TODOS" })
        ) { backStackEntry ->
            val filtroInicial = backStackEntry.arguments?.getString("filtro") ?: "TODOS"
            ProductosScreen(
                viewModel = productoViewModel,
                filtroInicial = filtroInicial,
                onAtras = { navController.popBackStack() },
                onInicioClick = {
                    navController.navigate(Rutas.DASHBOARD) {
                        popUpTo(Rutas.DASHBOARD) { inclusive = true }
                    }
                },
                onEscanearClick = { navController.navigate(Rutas.SCANNER) },
                onAlertasClick = { navController.navigate(Rutas.ALERTAS) },
                onAgregarProducto = { navController.navigate("${Rutas.AGREGAR_PRODUCTO}?codigo=") },
                onEditarProducto = { producto ->
                    productoViewModel.seleccionarProducto(producto)
                    navController.navigate("${Rutas.AGREGAR_PRODUCTO}?codigo=")
                }
            )
        }

        composable(
            route = "${Rutas.AGREGAR_PRODUCTO}?codigo={codigo}",
            arguments = listOf(navArgument("codigo") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val codigoInicial = backStackEntry.arguments?.getString("codigo") ?: ""
            val productoEditar = productoViewModel.productoSeleccionado
            AgregarEditarProductoScreen(
                productoExistente = productoEditar,
                codigoInicial = codigoInicial,
                onGuardado = {
                    productoViewModel.limpiarSeleccion()
                    navController.popBackStack()
                },
                onAtras = {
                    productoViewModel.limpiarSeleccion()
                    navController.popBackStack()
                },
                viewModel = productoViewModel
            )
        }

        composable(Rutas.SCANNER) {
            ScannerScreen(
                viewModel = productoViewModel,
                onAtras = { navController.popBackStack() },
                onInicioClick = {
                    navController.navigate(Rutas.DASHBOARD) {
                        popUpTo(Rutas.DASHBOARD) { inclusive = true }
                    }
                },
                onProductosClick = { navController.navigate("${Rutas.PRODUCTOS}?filtro=TODOS") },
                onAlertasClick = { navController.navigate(Rutas.ALERTAS) },
                onAgregarProducto = { codigo ->
                    productoViewModel.limpiarSeleccion()
                    navController.navigate("${Rutas.AGREGAR_PRODUCTO}?codigo=$codigo")
                },
                onVerDetalle = { producto ->
                    productoViewModel.seleccionarProducto(producto)
                    navController.navigate("${Rutas.AGREGAR_PRODUCTO}?codigo=")
                },
                onAgregarACanasta = { producto ->
                    ventaViewModel.agregarACanasta(producto)
                    navController.navigate(Rutas.CAJA)
                }
            )
        }

        composable(Rutas.CAJA) {
            CajaScreen(
                viewModel = ventaViewModel,
                onAtras = { navController.popBackStack() },
                onEscanearMas = {
                    navController.navigate(Rutas.SCANNER) {
                        popUpTo(Rutas.CAJA) { inclusive = false }
                    }
                },
                onFinalizarCompra = {
                    navController.navigate(Rutas.RESUMEN_COMPRA) {
                        popUpTo(Rutas.CAJA) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.RESUMEN_COMPRA) {
            ResumenCompraScreen(
                viewModel = ventaViewModel,
                onVolver = {
                    navController.navigate(Rutas.DASHBOARD) {
                        popUpTo(Rutas.DASHBOARD) { inclusive = true }
                    }
                },
                onNuevaVenta = {
                    navController.navigate(Rutas.SCANNER) {
                        popUpTo(Rutas.DASHBOARD) { inclusive = false }
                    }
                }
            )
        }

        composable(Rutas.ALERTAS) {
            AlertasScreen(
                onAtras = { navController.popBackStack() },
                onInicioClick = {
                    navController.navigate(Rutas.DASHBOARD) {
                        popUpTo(Rutas.DASHBOARD) { inclusive = true }
                    }
                },
                onProductosClick = { navController.navigate("${Rutas.PRODUCTOS}?filtro=TODOS") },
                onEscanearClick = { navController.navigate(Rutas.SCANNER) }
            )
        }

        // ── Módulo Configuración ──────────────────────────────────────────────
        composable(Rutas.CONFIGURACION) {
            ConfiguracionScreen(
                onAtras = { navController.popBackStack() },
                onEditarPerfil = { navController.navigate(Rutas.EDITAR_PERFIL) },
                onCambiarContrasena = { navController.navigate(Rutas.CAMBIAR_CONTRASENA) },
                onNotificaciones = { navController.navigate(Rutas.NOTIFICACIONES) },
                onCategorias = { navController.navigate(Rutas.CATEGORIAS) },
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.DASHBOARD) { inclusive = true }
                    }
                },
                onInicioClick = {
                    navController.navigate(Rutas.DASHBOARD) {
                        popUpTo(Rutas.DASHBOARD) { inclusive = true }
                    }
                },
                onProductosClick = { navController.navigate("${Rutas.PRODUCTOS}?filtro=TODOS") },
                onEscanearClick = { navController.navigate(Rutas.SCANNER) },
                onAlertasClick = { navController.navigate(Rutas.ALERTAS) }
            )
        }

        composable(Rutas.EDITAR_PERFIL) {
            EditarPerfilScreen(
                onAtras = { navController.popBackStack() }
            )
        }

        composable(Rutas.CAMBIAR_CONTRASENA) {
            CambiarContrasenaScreen(
                onAtras = { navController.popBackStack() }
            )
        }

        composable(Rutas.NOTIFICACIONES) {
            NotificacionesScreen(
                onAtras = { navController.popBackStack() },
                onInicioClick = {
                    navController.navigate(Rutas.DASHBOARD) {
                        popUpTo(Rutas.DASHBOARD) { inclusive = true }
                    }
                },
                onProductosClick = { navController.navigate("${Rutas.PRODUCTOS}?filtro=TODOS") },
                onEscanearClick = { navController.navigate(Rutas.SCANNER) },
                onAlertasClick = { navController.navigate(Rutas.ALERTAS) }
            )
        }

        composable(Rutas.CATEGORIAS) {
            CategoriasScreen(
                onAtras = { navController.popBackStack() },
                viewModel = productoViewModel
            )
        }
    }
}