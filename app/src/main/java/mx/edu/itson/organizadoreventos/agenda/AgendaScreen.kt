package mx.edu.itson.organizadoreventos.agenda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

data class Servicio(
    val id: Int,
    var nombre: String,
    var descripcion: String,
    var precio: Float,
    var seleccionado: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(onNavigateToCliente: () -> Unit = {}) {

    // --- ESTADOS DE RESERVA ---
    var clienteSeleccionado by remember { mutableStateOf("") }
    var expandirClientes by remember { mutableStateOf(false) }
    val listaClientesMock = listOf("Luciano Barceló", "María González", "Familia López")

    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }
    var fechaSeleccionada by remember { mutableStateOf("") }

    var horaSeleccionada by remember { mutableStateOf("") }
    var expandirHoras by remember { mutableStateOf(false) }
    val listaHorasMock = listOf("14:00 (2:00 PM)", "16:00 (4:00 PM)", "18:00 (6:00 PM)", "20:00 (8:00 PM)", "22:00 (10:00 PM)")

    var notasExtras by remember { mutableStateOf("") }

    // --- ESTADOS DE SERVICIOS ---
    val listaServicios = remember {
        mutableStateListOf(
            Servicio(1, "Banquete Estándar (100 pers.)", "Plato fuerte de ave, guarnición y pan.", 15000f, false),
            Servicio(2, "Horario Extendido", "Agrega 2 horas adicionales al evento.", 2500f, false),
            Servicio(3, "Permiso de Alcohol", "Trámite y permiso del ayuntamiento.", 1500f, false),
            Servicio(4, "Música y DJ", "DJ por 5 horas con equipo de sonido.", 3500f, false)
        )
    }

    // --- ESTADOS PARA LOS POPUPS (DIALOGS) ---
    var mostrarDialogoServicio by remember { mutableStateOf(false) }
    var servicioEditando by remember { mutableStateOf<Servicio?>(null) }
    var tempNombre by remember { mutableStateOf("") }
    var tempDesc by remember { mutableStateOf("") }
    var tempPrecio by remember { mutableStateOf("") }

    var servicioAEliminar by remember { mutableStateOf<Servicio?>(null) }
    var mostrarDialogoAgendar by remember { mutableStateOf(false) }

    // --- CÁLCULO DE SUBTOTAL ---
    val subtotal = listaServicios.filter { it.seleccionado }.sumOf { it.precio.toDouble() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda y Presupuesto") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Subtotal Estimado", style = MaterialTheme.typography.labelMedium)
                        Text("$${subtotal.toFloat()}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = { mostrarDialogoAgendar = true }, // Lanza el Popup de Éxito
                        enabled = clienteSeleccionado.isNotEmpty() && fechaSeleccionada.isNotEmpty() && horaSeleccionada.isNotEmpty() && subtotal > 0
                    ) {
                        Text("Agendar Cita")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 32.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            // 1. DATOS DEL CLIENTE
            item {
                Text("1. Datos del Cliente", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandirClientes, onExpandedChange = { expandirClientes = it }) {
                    OutlinedTextField(
                        value = clienteSeleccionado.ifEmpty { "Seleccione un cliente" },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirClientes) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandirClientes, onDismissRequest = { expandirClientes = false }) {
                        listaClientesMock.forEach { cliente ->
                            DropdownMenuItem(text = { Text(cliente) }, onClick = { clienteSeleccionado = cliente; expandirClientes = false })
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("➕ Registrar nuevo cliente", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                            onClick = { expandirClientes = false; onNavigateToCliente() }
                        )
                    }
                }
            }

            // 2. FECHA Y HORA
            item {
                Text("2. Cuándo será el evento", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1f)) {
                        Text(fechaSeleccionada.ifEmpty { "Día/Mes/Año" })
                    }
                    ExposedDropdownMenuBox(expanded = expandirHoras, onExpandedChange = { expandirHoras = it }, modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { expandirHoras = true }, modifier = Modifier.fillMaxWidth().menuAnchor()) {
                            Text(horaSeleccionada.ifEmpty { "Hora" })
                        }
                        ExposedDropdownMenu(expanded = expandirHoras, onDismissRequest = { expandirHoras = false }) {
                            listaHorasMock.forEach { hora -> DropdownMenuItem(text = { Text(hora) }, onClick = { horaSeleccionada = hora; expandirHoras = false }) }
                        }
                    }
                }
            }

            // 3. TÍTULO DE SERVICIOS Y BOTÓN DE AGREGAR (+)
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("3. Catálogo de Servicios", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    IconButton(
                        onClick = {
                            servicioEditando = null
                            tempNombre = ""
                            tempDesc = ""
                            tempPrecio = ""
                            mostrarDialogoServicio = true
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Servicio", tint = Color.White)
                    }
                }
            }

            // LISTA DE SERVICIOS
            itemsIndexed(listaServicios) { index, servicio ->
                ServicioCard(
                    servicio = servicio,
                    onCheckedChange = { isChecked -> listaServicios[index] = servicio.copy(seleccionado = isChecked) },
                    onEdit = {
                        servicioEditando = servicio
                        tempNombre = servicio.nombre
                        tempDesc = servicio.descripcion
                        tempPrecio = servicio.precio.toString()
                        mostrarDialogoServicio = true
                    },
                    onDelete = { servicioAEliminar = servicio }
                )
            }

            // 4. NOTAS EXTRAS
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("4. Notas Extras", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notasExtras,
                    onValueChange = { notasExtras = it },
                    label = { Text("Instrucciones especiales, alergias, etc.") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4
                )
            }
        }

        // -----------------------------------------------------------------
        // SECCIÓN DE DIÁLOGOS (POPUPS)
        // -----------------------------------------------------------------

        // 1. Calendario (DatePicker)
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formato = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
                            formato.timeZone = TimeZone.getTimeZone("UTC")
                            fechaSeleccionada = formato.format(Date(millis))
                        }
                        showDatePicker = false
                    }) { Text("Aceptar") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // 2. Dialogo para AGREGAR o EDITAR servicio
        if (mostrarDialogoServicio) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoServicio = false },
                title = { Text(if (servicioEditando == null) "Agregar Servicio" else "Editar Servicio") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = tempNombre, onValueChange = { tempNombre = it }, label = { Text("Nombre del servicio") }, singleLine = true)
                        OutlinedTextField(value = tempDesc, onValueChange = { tempDesc = it }, label = { Text("Descripción detallada") })
                        OutlinedTextField(value = tempPrecio, onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) tempPrecio = it }, label = { Text("Costo ($)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val precioFloat = tempPrecio.toFloatOrNull() ?: 0f
                        if (servicioEditando == null) {
                            // Agregar (CORREGIDO: Ahora entra como false para no auto-seleccionarse)
                            val nuevoId = (listaServicios.maxOfOrNull { it.id } ?: 0) + 1
                            listaServicios.add(Servicio(nuevoId, tempNombre, tempDesc.ifBlank { "Sin descripción" }, precioFloat, false))
                        } else {
                            // Editar
                            val index = listaServicios.indexOfFirst { it.id == servicioEditando!!.id }
                            if (index != -1) {
                                listaServicios[index] = servicioEditando!!.copy(nombre = tempNombre, descripcion = tempDesc, precio = precioFloat)
                            }
                        }
                        mostrarDialogoServicio = false
                    }) { Text("Guardar") }
                },
                dismissButton = { TextButton(onClick = { mostrarDialogoServicio = false }) { Text("Cancelar") } }
            )
        }

        // 3. Dialogo de CONFIRMACIÓN PARA ELIMINAR
        if (servicioAEliminar != null) {
            AlertDialog(
                onDismissRequest = { servicioAEliminar = null },
                title = { Text("Eliminar Servicio") },
                text = { Text("¿Estás seguro de que deseas eliminar '${servicioAEliminar?.nombre}' del catálogo?") },
                confirmButton = {
                    Button(
                        onClick = {
                            listaServicios.remove(servicioAEliminar)
                            servicioAEliminar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Sí, Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { servicioAEliminar = null }) { Text("Cancelar") }
                }
            )
        }

        // 4. Dialogo de CITA AGENDADA (Éxito)
        if (mostrarDialogoAgendar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoAgendar = false },
                title = { Text("¡Cita Agendada!", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold) },
                text = { Text("La cita para $clienteSeleccionado el $fechaSeleccionada a las $horaSeleccionada ha sido registrada exitosamente.") },
                confirmButton = {
                    Button(onClick = {
                        mostrarDialogoAgendar = false
                    }) { Text("Aceptar") }
                }
            )
        }
    }
}

@Composable
fun ServicioCard(
    servicio: Servicio,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (servicio.seleccionado) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = servicio.nombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = servicio.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$${servicio.precio}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }

                Switch(checked = servicio.seleccionado, onCheckedChange = onCheckedChange)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}