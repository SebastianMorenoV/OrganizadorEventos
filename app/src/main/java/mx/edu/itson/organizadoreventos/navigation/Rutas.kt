package mx.edu.itson.organizadoreventos.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.edu.itson.organizadoreventos.agenda.AgendaScreen
import mx.edu.itson.organizadoreventos.auth.AuthViewModel
import mx.edu.itson.organizadoreventos.clientes.ClienteScreen
import mx.edu.itson.organizadoreventos.screens.LoginScreen
import mx.edu.itson.organizadoreventos.screens.RegisterScreen
import mx.edu.itson.organizadoreventos.screens.SplashScreen
import mx.edu.itson.organizadoreventos.finanzas.FinanzasScreen
import mx.edu.itson.organizadoreventos.cotizacion.CotizacionScreen

object RutasGlobales {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
}

sealed class Rutas(val ruta: String, val titulo: String, val icono: ImageVector) {
    object Cliente : Rutas("cliente", "Cliente", Icons.Default.Person)
    object Agenda : Rutas("agenda", "Agenda", Icons.Default.DateRange)
    object Finanzas : Rutas("finanzas", "Finanzas", Icons.Default.AddCircle)
    object Cotizacion : Rutas("cotizacion", "Cotización", Icons.Default.List)
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = RutasGlobales.SPLASH) {

        composable(RutasGlobales.SPLASH) {
            SplashScreen(onTimeout = {
                if (authViewModel.usuarioActual != null) {
                    navController.navigate(RutasGlobales.MAIN) {
                        popUpTo(RutasGlobales.SPLASH) { inclusive = true }
                    }
                } else {
                    navController.navigate(RutasGlobales.LOGIN) {
                        popUpTo(RutasGlobales.SPLASH) { inclusive = true }
                    }
                }
            })
        }

        composable(RutasGlobales.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(RutasGlobales.MAIN) {
                        popUpTo(RutasGlobales.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(RutasGlobales.REGISTER)
                }
            )
        }

        composable(RutasGlobales.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(RutasGlobales.MAIN) {
                        popUpTo(RutasGlobales.REGISTER) { inclusive = true }
                        popUpTo(RutasGlobales.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(RutasGlobales.MAIN) { 
            MainApp(
                onLogout = {
                    authViewModel.cerrarSesion()
                    navController.navigate(RutasGlobales.LOGIN) {
                        popUpTo(RutasGlobales.MAIN) { inclusive = true }
                    }
                }
            ) 
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(onLogout: () -> Unit) {
    val navControllerTabs = rememberNavController()
    val items = listOf(Rutas.Cliente, Rutas.Agenda, Rutas.Finanzas, Rutas.Cotizacion)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organizador de Eventos") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
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
            composable(Rutas.Agenda.ruta) { AgendaScreen(onNavigateToCliente = { navControllerTabs.navigate(Rutas.Cliente.ruta) }) }
            composable(Rutas.Finanzas.ruta) { FinanzasScreen() }
            composable(Rutas.Cotizacion.ruta) { CotizacionScreen() }
        }
    }
}