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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import mx.edu.itson.organizadoreventos.R
import mx.edu.itson.organizadoreventos.model.ServicioEvento
import mx.edu.itson.organizadoreventos.model.Cliente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(viewModel: AgendaViewModel = viewModel(), onNavigateToCliente: () -> Unit = {}) {

    val snackbarHostState = remember { SnackbarHostState() }
    val listaClientes by viewModel.listaClientes.collectAsState()

    // --- ESTADOS DE RESERVA ---
    var tipoEvento by remember { mutableStateOf("") }

    var clienteSeleccionado by remember { mutableStateOf<Cliente?>(null) }
    var expandirClientes by remember { mutableStateOf(false) }

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
            ServicioEvento("1", "Banquete Estándar (100 pers.)", "Plato fuerte de ave, guarnición y pan.", 15000f, false),
            ServicioEvento("2", "Horario Extendido", "Agrega 2 horas adicionales al evento.", 2500f, false),
            ServicioEvento("3", "Permiso de Alcohol", "Trámite y permiso del ayuntamiento.", 1500f, false),
            ServicioEvento("4", "Música y DJ", "DJ por 5 horas con equipo de sonido.", 3500f, false)
        )
    }

    // --- ESTADOS PARA LOS POPUPS (DIALOGS) ---
    var mostrarDialogoServicio by remember { mutableStateOf(false) }
    var servicioEditando by remember { mutableStateOf<ServicioEvento?>(null) }
    var tempNombre by remember { mutableStateOf("") }
    var tempDesc by remember { mutableStateOf("") }
    var tempPrecio by remember { mutableStateOf("") }

    var servicioAEliminar by remember { mutableStateOf<ServicioEvento?>(null) }
    


    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // --- CÁLCULO DE SUBTOTAL ---
    val subtotal = listaServicios.filter { it.seleccionado }.sumOf { it.precio.toDouble() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_empresa),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agenda y Presupuesto") 
                    }
                },
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
                        onClick = {
                            val cliente = clienteSeleccionado
                            if (cliente != null) {
                                viewModel.agendarEvento(
                                    tipoEvento = tipoEvento,
                                    clienteId = cliente.id,
                                    clienteNombre = cliente.nombre,
                                    fecha = fechaSeleccionada,
                                    hora = horaSeleccionada,
                                    notas = notasExtras,
                                    servicios = listaServicios.filter { it.seleccionado }
                                )
                            }
                        },
                        // VALIDACIÓN ACTUALIZADA INCLUYENDO EL TIPO DE EVENTO
                        enabled = tipoEvento.isNotEmpty() && clienteSeleccionado != null && fechaSeleccionada.isNotEmpty() && horaSeleccionada.isNotEmpty() && subtotal > 0 && !viewModel.estaGuardando
                    ) {
                        if (viewModel.estaGuardando) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Agendar Cita")
                        }
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
            // 1. TIPO DE EVENTO
            item {
                Text("1. Detalles del Evento", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = tipoEvento,
                    onValueChange = { tipoEvento = it },
                    label = { Text("Tipo de evento (Boda, XV, etc.)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )
            }

            // 2. DATOS DEL CLIENTE
            item {
                Text("2. Datos del Cliente", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandirClientes, onExpandedChange = { expandirClientes = it }) {
                    OutlinedTextField(
                        value = clienteSeleccionado?.nombre ?: "Seleccione un cliente",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandirClientes) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandirClientes, onDismissRequest = { expandirClientes = false }) {
                        listaClientes.forEach { cliente ->
                            DropdownMenuItem(text = { Text(cliente.nombre) }, onClick = { clienteSeleccionado = cliente; expandirClientes = false })
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("➕ Registrar nuevo cliente", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                            onClick = { expandirClientes = false; onNavigateToCliente() }
                        )
                    }
                }
            }

            // 3. FECHA Y HORA
            item {
                Text("3. Cuándo será el evento", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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

            // 4. TÍTULO DE SERVICIOS Y BOTÓN DE AGREGAR (+)
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("4. Catálogo de Servicios", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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

            // 5. NOTAS EXTRAS
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("5. Notas Extras", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                            val nuevoId = (listaServicios.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1
                            listaServicios.add(ServicioEvento(nuevoId.toString(), tempNombre, tempDesc.ifBlank { "Sin descripción" }, precioFloat, false))
                        } else {
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
        if (viewModel.guardadoExitoso) {
            AlertDialog(
                onDismissRequest = { viewModel.resetEstadoExito() },
                title = { Text("¡Cita Agendada!", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold) },
                text = { Text("La cita para ${clienteSeleccionado?.nombre} el $fechaSeleccionada a las $horaSeleccionada para el evento de tipo '$tipoEvento' ha sido registrada exitosamente.") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.resetEstadoExito()
                        // Reset forms
                        tipoEvento = ""
                        clienteSeleccionado = null
                        fechaSeleccionada = ""
                        horaSeleccionada = ""
                        notasExtras = ""
                        listaServicios.forEachIndexed { i, s -> listaServicios[i] = s.copy(seleccionado = false) }
                    }) { Text("Aceptar") }
                }
            )
        }
    }
}

@Composable
fun ServicioCard(
    servicio: ServicioEvento,
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