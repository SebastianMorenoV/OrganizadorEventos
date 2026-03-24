package mx.edu.itson.organizadoreventos.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.edu.itson.organizadoreventos.agenda.AgendaScreen
import mx.edu.itson.organizadoreventos.clientes.ClienteScreen
import mx.edu.itson.organizadoreventos.screens.SplashScreen
import mx.edu.itson.organizadoreventos.finanzas.FinanzasScreen
import mx.edu.itson.organizadoreventos.cotizacion.CotizacionScreen

// 1. Rutas Globales para el flujo principal (Padre)
object RutasGlobales {
    const val SPLASH = "splash"
    const val MAIN = "main"
}

// 2. Tus rutas del menú inferior se quedan intactas (Hijo)
sealed class Rutas(val ruta: String, val titulo: String, val icono: ImageVector) {
    object Cliente : Rutas("cliente", "Cliente", Icons.Default.Person)
    object Agenda : Rutas("agenda", "Agenda", Icons.Default.DateRange)
    object Finanzas : Rutas("finanzas", "Finanzas", Icons.Default.AddCircle)
    object Cotizacion : Rutas("cotizacion", "Cotización", Icons.Default.ShoppingCart)
}

// 3. NUEVO PUNTO DE ENTRADA: Controlador Padre
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = RutasGlobales.SPLASH
    ) {
        // Pantalla de carga
        composable(RutasGlobales.SPLASH) {
            SplashScreen(onTimeout = {
                navController.navigate(RutasGlobales.MAIN) {
                    // Borramos el splash del historial para que el usuario no pueda regresar a él
                    popUpTo(RutasGlobales.SPLASH) { inclusive = true }
                }
            })
        }

        // Tu aplicación con el menú inferior
        composable(RutasGlobales.MAIN) {
            MainApp()
        }
    }
}

// 4. TU CÓDIGO ORIGINAL ACTUALIZADO
@Composable
fun MainApp() {
    val navControllerTabs = rememberNavController()
    val items = listOf(Rutas.Cliente, Rutas.Agenda, Rutas.Finanzas, Rutas.Cotizacion)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navControllerTabs.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { pantalla ->
                    NavigationBarItem(
                        icon = { Icon(pantalla.icono, contentDescription = pantalla.titulo) },
                        label = { Text(pantalla.titulo) },
                        selected = currentRoute == pantalla.ruta,
                        onClick = {
                            navControllerTabs.navigate(pantalla.ruta) {
                                popUpTo(navControllerTabs.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navControllerTabs,
            startDestination = Rutas.Cliente.ruta,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Rutas.Cliente.ruta) { ClienteScreen() }
            composable(Rutas.Agenda.ruta) { AgendaScreen() }
            composable(Rutas.Finanzas.ruta) { FinanzasScreen() }
            composable(Rutas.Cotizacion.ruta) { CotizacionScreen() }
        }
    }
}