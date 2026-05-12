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

/**
 * ViewModel para el módulo de Clientes.
 * Gestiona el estado del formulario de registro/edición, la lista en tiempo real
 * y las operaciones CRUD sobre los clientes en Firebase.
 */
class ClienteViewModel : ViewModel() {

    private val repository = ClienteRepository()

    /** Lista de clientes obtenida en tiempo real desde Firebase. */
    private val _listaClientes = MutableStateFlow<List<Cliente>>(emptyList())
    val listaClientes: StateFlow<List<Cliente>> = _listaClientes.asStateFlow()

    // --- Estado del formulario ---
    /** ID del cliente que se está editando. Null si el formulario es de registro nuevo. */
    var clienteIdEditando: String? = null

    /** Nombre del cliente en el formulario. */
    var nombre by mutableStateOf("")

    /** Teléfono del cliente en el formulario. */
    var telefono by mutableStateOf("")

    /** Correo electrónico del cliente en el formulario. */
    var correo by mutableStateOf("")

    // --- Estado de validación ---
    /** Bandera de error para el campo nombre. */
    var errorNombre by mutableStateOf(false)

    /** Bandera de error para el campo teléfono. */
    var errorTelefono by mutableStateOf(false)

    /** Bandera de error para el campo correo. */
    var errorCorreo by mutableStateOf(false)

    // --- Estado de carga y retroalimentación ---
    /** Indica si hay una operación de escritura en curso. */
    var estaGuardando by mutableStateOf(false)

    /** Bandera que indica que el guardado fue exitoso para disparar el Snackbar. */
    var guardadoExitoso by mutableStateOf(false)

    /** Mensaje de error para mostrar en Snackbar. Null si no hay error. */
    var errorMessage by mutableStateOf<String?>(null)

    init {
        cargarClientes()
    }

    /**
     * Inicia la observación en tiempo real de los clientes del usuario autenticado.
     * Se llama automáticamente al crear el ViewModel.
     */
    private fun cargarClientes() {
        viewModelScope.launch {
            repository.obtenerClientes().collect { clientes ->
                _listaClientes.value = clientes
            }
        }
    }

    /**
     * Valida el formulario y guarda o actualiza un cliente en Firebase.
     * Si [clienteIdEditando] es null, crea un cliente nuevo (CREATE).
     * Si [clienteIdEditando] tiene valor, actualiza el cliente existente (UPDATE).
     * Verifica duplicados de teléfono y correo antes de persistir.
     */
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

    /**
     * Carga los datos de un cliente existente en el formulario para su edición.
     *
     * @param cliente Objeto [Cliente] cuyos datos se copiarán en el formulario.
     */
    fun editarCliente(cliente: Cliente) {
        clienteIdEditando = cliente.id
        nombre = cliente.nombre
        telefono = cliente.telefono
        correo = cliente.correo
        errorNombre = false
        errorTelefono = false
        errorCorreo = false
    }

    /**
     * Limpia todos los campos del formulario y restablece el modo de creación nuevo.
     */
    fun limpiarFormulario() {
        clienteIdEditando = null
        nombre = ""
        telefono = ""
        correo = ""
        errorNombre = false
        errorTelefono = false
        errorCorreo = false
    }

    /**
     * Elimina permanentemente un cliente de Firebase (DELETE).
     * Si el cliente eliminado era el que se estaba editando, limpia el formulario.
     *
     * @param clienteId ID único del cliente a eliminar.
     */
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

    /** Resetea la bandera [guardadoExitoso] después de mostrar el Snackbar de éxito. */
    fun resetEstadoExito() {
        guardadoExitoso = false
    }
    
    /** Limpia el mensaje de error actual. */
    fun clearError() {
        errorMessage = null
    }
}