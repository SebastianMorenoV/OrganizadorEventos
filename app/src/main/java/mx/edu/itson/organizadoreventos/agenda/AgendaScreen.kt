package mx.edu.itson.organizadoreventos.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

// Modelo de datos para nuestros servicios/paquetes
data class Servicio(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Float,
    val seleccionado: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen() {
    // --- ESTADO DEL DATEPICKER ---
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var fechaSeleccionada by remember { mutableStateOf("Ninguna fecha seleccionada") }

    // --- ESTADO DEL CATÁLOGO DE SERVICIOS ---
    // Usamos mutableStateListOf para que la UI reaccione cuando modifiquemos un elemento
    val listaServicios = remember {
        mutableStateListOf(
            Servicio(1, "Banquete Estándar (100 pers.)", "Plato fuerte de ave, guarnición y pan.", 15000f, false),
            Servicio(2, "Horario Extendido", "Agrega 2 horas adicionales al evento.", 2500f, false),
            Servicio(3, "Permiso de Alcohol", "Trámite y permiso del ayuntamiento.", 1500f, false),
            Servicio(4, "Música y DJ", "DJ por 5 horas con equipo de sonido e iluminación básica.", 3500f, false),
            Servicio(5, "Mesa de Postres", "Variedad de postres dulces y salados para 100 personas.", 4000f, false)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda y Paquetes") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --------------------------------------------------------
            // 1. SELECTOR DE FECHA (RESERVA)
            // --------------------------------------------------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Fecha del Evento", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fechaSeleccionada, 
                        style = MaterialTheme.typography.bodyLarge, 
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { showDatePicker = true }) {
                        Text("Seleccionar Fecha en Calendario")
                    }
                }
            }

            // Dialogo del calendario (DatePicker de Material 3)
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                // Formatear la fecha a un formato legible
                                val formato = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale("es", "MX"))
                                // Se ajusta la zona horaria UTC para evitar el desfase de 1 día
                                formato.timeZone = TimeZone.getTimeZone("UTC") 
                                fechaSeleccionada = formato.format(Date(millis))
                            }
                            showDatePicker = false
                        }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancelar")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // --------------------------------------------------------
            // 2. CATÁLOGO DE PAQUETES Y SERVICIOS
            // --------------------------------------------------------
            Text(
                text = "Catálogo de Servicios",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Lista perezosa (LazyColumn) para el catálogo interactivo
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(listaServicios) { index, servicio ->
                    ServicioCard(
                        servicio = servicio,
                        onCheckedChange = { isChecked ->
                            // Actualizamos el estado del servicio específico
                            listaServicios[index] = servicio.copy(seleccionado = isChecked)
                        }
                    )
                }
            }
        }
    }
}

// Componente individual para cada tarjeta del catálogo
@Composable
fun ServicioCard(servicio: Servicio, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        // Cambiamos el color de fondo si el servicio está seleccionado para mejor respuesta visual
        colors = CardDefaults.cardColors(
            containerColor = if (servicio.seleccionado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Textos del servicio
            Column(modifier = Modifier.weight(1f)) {
                Text(text = servicio.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = servicio.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "$${servicio.precio}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            
            // Interruptor de activación (Switch)
            Switch(
                checked = servicio.seleccionado,
                onCheckedChange = onCheckedChange
            )
        }
    }
}