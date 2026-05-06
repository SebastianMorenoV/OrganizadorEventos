package mx.edu.itson.organizadoreventos.clientes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import mx.edu.itson.organizadoreventos.R

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import mx.edu.itson.organizadoreventos.model.Cliente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen(viewModel: ClienteViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listaClientes by viewModel.listaClientes.collectAsState()
    var clienteAEliminar by remember { mutableStateOf<Cliente?>(null) }

    if (viewModel.guardadoExitoso) {
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar("Cliente registrado con éxito")
            viewModel.resetEstadoExito()
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

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
                        Text("Nuevo Cliente")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Datos Personales",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                OutlinedTextField(
                    value = viewModel.nombre,
                    onValueChange = { viewModel.nombre = it; viewModel.errorNombre = false },
                    label = { Text("Nombre completo *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.errorNombre,
                    supportingText = { if (viewModel.errorNombre) Text("El nombre es obligatorio") },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    enabled = !viewModel.estaGuardando
                )
            }

            item {
                OutlinedTextField(
                    value = viewModel.telefono,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.length <= 10) {
                            viewModel.telefono = newValue
                            viewModel.errorTelefono = false
                        }
                    },
                    label = { Text("Teléfono *") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.errorTelefono,
                    supportingText = { if (viewModel.errorTelefono) Text("Ingresa exactamente 10 dígitos numéricos") },
                    enabled = !viewModel.estaGuardando
                )
            }

            item {
                OutlinedTextField(
                    value = viewModel.correo,
                    onValueChange = { viewModel.correo = it; viewModel.errorCorreo = false },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.errorCorreo,
                    supportingText = { if (viewModel.errorCorreo) Text("Formato de correo inválido (ej. usuario@dominio.com)") },
                    enabled = !viewModel.estaGuardando
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (viewModel.clienteIdEditando != null) {
                        OutlinedButton(
                            onClick = { viewModel.limpiarFormulario() },
                            modifier = Modifier.weight(1f).height(50.dp),
                            enabled = !viewModel.estaGuardando
                        ) {
                            Text("Cancelar")
                        }
                    }
                    FilledTonalButton(
                        onClick = { viewModel.guardarCliente() },
                        modifier = Modifier.weight(1f).height(50.dp),
                        enabled = !viewModel.estaGuardando
                    ) {
                        Text(if (viewModel.estaGuardando) "Guardando..." else if (viewModel.clienteIdEditando != null) "Actualizar Cliente" else "Registrar Cliente")
                    }
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text(
                    text = "Clientes Registrados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(listaClientes) { cliente ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = cliente.nombre, fontWeight = FontWeight.Bold)
                                Text(text = "Tel: ${cliente.telefono}", style = MaterialTheme.typography.bodyMedium)
                                if (cliente.correo.isNotBlank()) {
                                    Text(text = "Correo: ${cliente.correo}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Row {
                                IconButton(onClick = { viewModel.editarCliente(cliente) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                                }
                                IconButton(onClick = { clienteAEliminar = cliente }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (clienteAEliminar != null) {
            AlertDialog(
                onDismissRequest = { clienteAEliminar = null },
                title = { Text("Eliminar Cliente") },
                text = { Text("¿Estás seguro de que deseas eliminar a '${clienteAEliminar?.nombre}'? Esta acción no se puede deshacer y podría afectar a los eventos asociados.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.eliminarCliente(clienteAEliminar!!.id)
                            clienteAEliminar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Sí, Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { clienteAEliminar = null }) { Text("Cancelar") }
                }
            )
        }
    }
}