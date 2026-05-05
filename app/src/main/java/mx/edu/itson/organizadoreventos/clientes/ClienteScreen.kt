package mx.edu.itson.organizadoreventos.clientes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import mx.edu.itson.organizadoreventos.model.Cliente

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteScreen(viewModel: ClienteViewModel = viewModel()) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listaClientes by viewModel.listaClientes.collectAsState()

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
                title = { Text("Nuevo Cliente") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Datos Personales",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

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

            OutlinedTextField(
                value = viewModel.telefono,
                onValueChange = { viewModel.telefono = it; viewModel.errorTelefono = false },
                label = { Text("Teléfono *") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.errorTelefono,
                supportingText = { if (viewModel.errorTelefono) Text("El teléfono es obligatorio") },
                enabled = !viewModel.estaGuardando
            )

            OutlinedTextField(
                value = viewModel.correo,
                onValueChange = { viewModel.correo = it },
                label = { Text("Correo electrónico") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth(),
                enabled = !viewModel.estaGuardando
            )

            Spacer(modifier = Modifier.height(24.dp))

            FilledTonalButton(
                onClick = { viewModel.guardarCliente() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !viewModel.estaGuardando
            ) {
                Text(if (viewModel.estaGuardando) "Guardando..." else "Registrar Cliente")
            }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            Text(
                text = "Clientes Registrados",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listaClientes) { cliente ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = cliente.nombre, fontWeight = FontWeight.Bold)
                            Text(text = "Tel: ${cliente.telefono}", style = MaterialTheme.typography.bodyMedium)
                            if (cliente.correo.isNotBlank()) {
                                Text(text = "Correo: ${cliente.correo}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}