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
    var nombre by mutableStateOf("")
    var telefono by mutableStateOf("")
    var correo by mutableStateOf("")

    // ESTADO DE ERRORES Y CARGA
    var errorNombre by mutableStateOf(false)
    var errorTelefono by mutableStateOf(false)
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
        errorTelefono = telefono.isBlank()

        if (!errorNombre && !errorTelefono) {
            viewModelScope.launch {
                estaGuardando = true
                errorMessage = null

                val cliente = Cliente(nombre = nombre, telefono = telefono, correo = correo)
                val result = repository.guardarCliente(cliente)

                if (result.isSuccess) {
                    nombre = ""
                    telefono = ""
                    correo = ""
                    guardadoExitoso = true
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al guardar cliente"
                }

                estaGuardando = false
            }
        }
    }

    fun resetEstadoExito() {
        guardadoExitoso = false
    }
    
    fun clearError() {
        errorMessage = null
    }
}