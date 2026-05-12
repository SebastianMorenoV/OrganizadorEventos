package mx.edu.itson.organizadoreventos.finanzas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.Calendar

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import mx.edu.itson.organizadoreventos.R
import mx.edu.itson.organizadoreventos.model.Abono
import mx.edu.itson.organizadoreventos.model.Evento

// Extensión para calcular el total estimado de un evento sumando sus servicios
val Evento.totalEstimado: Float
    get() = servicios.sumOf { it.precio.toDouble() }.toFloat()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanzasScreen(viewModel: FinanzasViewModel = viewModel()) {
    val eventosRegistrados by viewModel.listaEventos.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    var eventoSeleccionado by remember { mutableStateOf<Evento?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (eventoSeleccionado == null) {
            // VISTA 1: LISTA DE EVENTOS Y DASHBOARD
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_empresa),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Dashboard Financiero", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- RESUMEN MENSUAL ---
                ResumenMensualCard(eventosRegistrados)

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar evento o fecha (ej. 15/05/2026)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                val eventosFiltrados = eventosRegistrados.filter {
                    it.tipoEvento.contains(searchQuery, ignoreCase = true) ||
                            it.fecha.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(eventosFiltrados) { evento ->
                        TarjetaEvento(
                            evento = evento,
                            onClick = { eventoSeleccionado = evento },
                            onUpdateEstado = { nuevoEstado ->
                                viewModel.actualizarEstadoEvento(evento.id, nuevoEstado)
                            },
                            onEliminar = {
                                viewModel.eliminarEvento(evento.id)
                            }
                        )
                    }
                }
            }
        } else {
            DetalleFinanzasView(
                evento = eventoSeleccionado!!,
                onBack = { eventoSeleccionado = null },
                onRegistrarAbono = { fecha, monto ->
                    viewModel.registrarAbono(eventoSeleccionado!!.id, fecha, monto)
                    // Actualización optimista para reactividad inmediata
                    eventoSeleccionado = eventoSeleccionado!!.copy(abonos = eventoSeleccionado!!.abonos + (System.currentTimeMillis().toString() to Abono(fecha = fecha, monto = monto)))
                },
                onActualizarEvento = { eventoEditado ->
                    viewModel.actualizarEvento(eventoEditado)
                    eventoSeleccionado = eventoEditado
                }
            )
        }
    }
}

// --- COMPONENTE: RESUMEN MENSUAL ---
@Composable
fun ResumenMensualCard(eventos: List<Evento>) {
    val nombresMeses = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val calendar = Calendar.getInstance()
    val mesActualIndex = calendar.get(Calendar.MONTH)
    val nombreMesActual = nombresMeses[mesActualIndex]

    val mesFiltroStr = String.format("%02d", mesActualIndex + 1)
    val anioFiltroStr = calendar.get(Calendar.YEAR).toString()
    val filtroFechaAbono = "/$mesFiltroStr/$anioFiltroStr"

    // Ganancia Mensual: Abonos de TODOS los eventos que se hicieron en este mes
    val gananciaMensual = eventos.filter { it.estado != "Cancelado" }.sumOf { evento ->
        val abonosDelEvento = evento.abonos.values.toList()
        abonosDelEvento.filter { it.fecha.endsWith(filtroFechaAbono) }
            .sumOf { it.monto.toDoubleOrNull() ?: 0.0 }
    }.toFloat()

    // Falta Por Abonar: Deuda total de TODOS los eventos activos
    val faltaPorAbonarGlobal = eventos.filter { it.estado != "Cancelado" }.sumOf { evento ->
        val abonosDelEvento = evento.abonos.values.toList()
        val pagado = abonosDelEvento.sumOf { it.monto.toDoubleOrNull() ?: 0.0 }
        (evento.totalEstimado - pagado).coerceAtLeast(0.0)
    }.toFloat()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Ganancia Mensual", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Text("($nombreMesActual)", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$${gananciaMensual.toInt()}", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF388E3C), fontWeight = FontWeight.ExtraBold)
                }

                Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color.LightGray).align(Alignment.CenterVertically))

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text("Falta Por Abonar", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Text("($nombreMesActual)", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$${faltaPorAbonarGlobal.toInt()}", style = MaterialTheme.typography.headlineSmall, color = Color(0xFFD32F2F), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// --- COMPONENTE: TARJETA DE EVENTO CON MENÚ DE ESTADO ---
@Composable
fun TarjetaEvento(
    evento: Evento,
    onClick: () -> Unit,
    onUpdateEstado: (String) -> Unit,
    onEliminar: () -> Unit
) {
    var mostrarMenu by remember { mutableStateOf(false) }
    var mostrarConfirmacionEliminar by remember { mutableStateOf(false) }

    val colorEstado = when (evento.estado) {
        "Terminado" -> Color(0xFF388E3C)
        "Cancelado" -> Color(0xFFD32F2F)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(evento.tipoEvento, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = colorEstado.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text(evento.estado, color = colorEstado, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Cliente: ${evento.clienteNombre}", style = MaterialTheme.typography.bodyMedium)
                Text("Fecha de Evento: ${evento.fecha}", style = MaterialTheme.typography.bodyMedium)
            }

            Box {
                IconButton(onClick = { mostrarMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                }
                DropdownMenu(expanded = mostrarMenu, onDismissRequest = { mostrarMenu = false }) {
                    DropdownMenuItem(text = { Text("Marcar como Activo") }, onClick = { onUpdateEstado("Activo"); mostrarMenu = false })
                    DropdownMenuItem(text = { Text("Marcar como Terminado") }, onClick = { onUpdateEstado("Terminado"); mostrarMenu = false })
                    DropdownMenuItem(text = { Text("Marcar como Cancelado") }, onClick = { onUpdateEstado("Cancelado"); mostrarMenu = false })
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Eliminar Evento", color = MaterialTheme.colorScheme.error) },
                        onClick = { mostrarMenu = false; mostrarConfirmacionEliminar = true }
                    )
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (mostrarConfirmacionEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionEliminar = false },
            title = { Text("Eliminar Evento") },
            text = { Text("¿Estás seguro de que deseas eliminar el evento '${evento.tipoEvento}' de ${evento.clienteNombre}? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = { onEliminar(); mostrarConfirmacionEliminar = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Sí, Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionEliminar = false }) { Text("Cancelar") }
            }
        )
    }
}

// --- VISTA DETALLE DE FINANZAS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleFinanzasView(
    evento: Evento,
    onBack: () -> Unit,
    onRegistrarAbono: (String, String) -> Unit,
    onActualizarEvento: (Evento) -> Unit
) {
    var showSheet by remember { mutableStateOf(false) }
    var mostrarTicket by remember { mutableStateOf(false) }
    var mostrarEditar by remember { mutableStateOf(false) }

    val pagado = evento.abonos.values.sumOf { it.monto.toIntOrNull() ?: 0 }.toFloat()
    val progreso = if (evento.totalEstimado > 0) (pagado / evento.totalEstimado).coerceAtMost(1f) else 0f

    Scaffold(
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
                        Text(evento.tipoEvento)
                    }
                }, 
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Regresar") } }
            )
        },
        floatingActionButton = {
            if (evento.estado != "Cancelado" && (evento.totalEstimado - pagado) > 0) {
                FloatingActionButton(onClick = { showSheet = true }, containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Default.Add, contentDescription = "Registrar Pago", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 16.dp)) {
            // Botones de acción
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { mostrarTicket = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) { Text("Ver Ticket") }

                OutlinedButton(
                    onClick = { mostrarEditar = true },
                    modifier = Modifier.weight(1f)
                ) { Text("Editar Evento") }
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(progress = { progreso }, modifier = Modifier.size(150.dp), strokeWidth = 12.dp, color = MaterialTheme.colorScheme.primary, trackColor = Color.LightGray)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(progreso * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Liquidado", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Anticipo Realizado", style = MaterialTheme.typography.labelMedium); Text("$$pagado", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) }
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Saldo Restante", style = MaterialTheme.typography.labelMedium); Text("$${(evento.totalEstimado - pagado).coerceAtLeast(0f)}", style = MaterialTheme.typography.titleLarge, color = Color.Gray) }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Historial de Abonos", fontWeight = FontWeight.Bold)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(evento.abonos.values.toList().reversed()) { pago ->
                    ListItem(headlineContent = { Text("Abono recibido") }, supportingContent = { Text("Fecha: ${pago.fecha}") }, trailingContent = { Text("$${pago.monto}", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold) })
                    HorizontalDivider()
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(onDismissRequest = { showSheet = false }) {
                FormularioPago(onConfirm = { fecha, monto -> 
                    onRegistrarAbono(fecha, monto)
                    showSheet = false 
                })
            }
        }

        if (mostrarTicket) { Dialog(onDismissRequest = { mostrarTicket = false }) { TicketDialogView(evento) } }

        if (mostrarEditar) {
            Dialog(onDismissRequest = { mostrarEditar = false }) {
                EditarEventoDialog(
                    evento = evento,
                    onConfirm = { eventoEditado ->
                        onActualizarEvento(eventoEditado)
                        mostrarEditar = false
                    },
                    onDismiss = { mostrarEditar = false }
                )
            }
        }
    }
}

// --- DIÁLOGO PARA EDITAR UN EVENTO (UPDATE COMPLETO) ---
@Composable
fun EditarEventoDialog(evento: Evento, onConfirm: (Evento) -> Unit, onDismiss: () -> Unit) {
    var tipoEvento by remember { mutableStateOf(evento.tipoEvento) }
    var fecha by remember { mutableStateOf(evento.fecha) }
    var hora by remember { mutableStateOf(evento.hora) }
    var notas by remember { mutableStateOf(evento.notas) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Editar Evento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider()

            OutlinedTextField(
                value = tipoEvento,
                onValueChange = { tipoEvento = it },
                label = { Text("Tipo de Evento") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (DD/MM/YYYY)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = hora,
                onValueChange = { hora = it },
                label = { Text("Hora") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(
                    onClick = {
                        onConfirm(evento.copy(tipoEvento = tipoEvento, fecha = fecha, hora = hora, notas = notas))
                    },
                    modifier = Modifier.weight(1f),
                    enabled = tipoEvento.isNotBlank() && fecha.isNotBlank() && hora.isNotBlank()
                ) { Text("Guardar") }
            }
        }
    }
}

// --- TICKET DINÁMICO ---
@Composable
fun TicketDialogView(evento: Evento) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("TICKET DE EVENTO", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("Cliente: ${evento.clienteNombre}", style = MaterialTheme.typography.bodyLarge)
            Text("Fecha del Evento: ${evento.fecha}", style = MaterialTheme.typography.bodyMedium)
            Text("Estado: ${evento.estado}", style = MaterialTheme.typography.bodyMedium, color = if(evento.estado=="Cancelado") Color.Red else Color.Black)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Text("DESGLOSE DE SERVICIOS:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            evento.servicios.forEach { servicio ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(servicio.nombre, style = MaterialTheme.typography.bodyMedium)
                    Text("$${servicio.precio}", fontWeight = FontWeight.Medium)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TOTAL ESTIMADO", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("$${evento.totalEstimado}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// --- FORMULARIO DE PAGOS ---
@Composable
fun FormularioPago(onConfirm: (String, String) -> Unit) {
    var monto by remember { mutableStateOf("") }

    val cal = Calendar.getInstance()
    val hoy = String.format("%02d/%02d/%04d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    var fecha by remember { mutableStateOf(hoy) }

    Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp).fillMaxWidth()) {
        Text("Registrar Nuevo Abono", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = monto, onValueChange = { if (it.all { char -> char.isDigit() }) monto = it }, label = { Text("¿Cuánto desea abonar?") }, modifier = Modifier.fillMaxWidth(), prefix = { Text("$ ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = fecha, onValueChange = { fecha = it }, label = { Text("Fecha (DD/MM/YYYY)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onConfirm(fecha, monto) },
            modifier = Modifier.fillMaxWidth(),
            enabled = monto.isNotEmpty() && monto.toIntOrNull()?.let { it > 0 } == true
        ) {
            Text("Confirmar y Actualizar")
        }
    }
}