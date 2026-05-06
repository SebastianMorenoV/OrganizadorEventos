package mx.edu.itson.organizadoreventos.clientes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.itson.organizadoreventos.data.ClienteRepository
import mx.edu.itson.organizadoreventos.model.Cliente

class ClienteViewModel : ViewModel() {

    private val repository = ClienteRepository()

    // ESTADO DE LA UI PARA LISTA
    private val _listaClientes = MutableStateFlow<List<Cliente>>(emptyList())
    val listaClientes: StateFlow<List<Cliente>> = _listaClientes.asStateFlow()

    // ESTADO DE LA UI PARA FORMULARIO
    var clienteIdEditando: String? = null
    var nombre by mutableStateOf("")
    var telefono by mutableStateOf("")
    var correo by mutableStateOf("")

    // ESTADO DE ERRORES Y CARGA
    var errorNombre by mutableStateOf(false)
    var errorTelefono by mutableStateOf(false)
    var errorCorreo by mutableStateOf(false)
    var estaGuardando by mutableStateOf(false)
    var guardadoExitoso by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        cargarClientes()
    }

    private fun cargarClientes() {
        viewModelScope.launch {
            repository.obtenerClientes().collect { clientes ->
                _listaClientes.value = clientes
            }
        }
    }

    fun guardarCliente() {
        errorNombre = nombre.isBlank()
        errorTelefono = telefono.length != 10
        errorCorreo = correo.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()

        if (!errorNombre && !errorTelefono && !errorCorreo) {
            viewModelScope.launch {
                estaGuardando = true
                errorMessage = null

                val existeTelefono = listaClientes.value.any { it.telefono == telefono && it.id != clienteIdEditando }
                val existeCorreo = correo.isNotBlank() && listaClientes.value.any { it.correo == correo && it.id != clienteIdEditando }

                if (existeTelefono) {
                    errorMessage = "Ya existe un cliente con este teléfono."
                    estaGuardando = false
                    return@launch
                }

                if (existeCorreo) {
                    errorMessage = "Ya existe un cliente con este correo."
                    estaGuardando = false
                    return@launch
                }

                val cliente = Cliente(id = clienteIdEditando ?: "", nombre = nombre, telefono = telefono, correo = correo)
                val result = if (clienteIdEditando == null) {
                    repository.guardarCliente(cliente)
                } else {
                    repository.actualizarCliente(cliente)
                }

                if (result.isSuccess) {
                    limpiarFormulario()
                    guardadoExitoso = true
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al guardar cliente"
                }

                estaGuardando = false
            }
        }
    }

    fun editarCliente(cliente: Cliente) {
        clienteIdEditando = cliente.id
        nombre = cliente.nombre
        telefono = cliente.telefono
        correo = cliente.correo
        errorNombre = false
        errorTelefono = false
        errorCorreo = false
    }

    fun limpiarFormulario() {
        clienteIdEditando = null
        nombre = ""
        telefono = ""
        correo = ""
        errorNombre = false
        errorTelefono = false
        errorCorreo = false
    }

    fun eliminarCliente(clienteId: String) {
        viewModelScope.launch {
            estaGuardando = true
            errorMessage = null
            val result = repository.eliminarCliente(clienteId)
            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al eliminar cliente"
            } else {
                if (clienteIdEditando == clienteId) {
                    limpiarFormulario()
                }
            }
            estaGuardando = false
        }
    }

    fun resetEstadoExito() {
        guardadoExitoso = false
    }
    
    fun clearError() {
        errorMessage = null
    }
}