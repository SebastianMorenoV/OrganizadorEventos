package mx.edu.itson.organizadoreventos.agenda

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
import mx.edu.itson.organizadoreventos.data.EventoRepository
import mx.edu.itson.organizadoreventos.data.ServicioRepository
import mx.edu.itson.organizadoreventos.model.Cliente
import mx.edu.itson.organizadoreventos.model.Evento
import mx.edu.itson.organizadoreventos.model.ServicioEvento

class AgendaViewModel : ViewModel() {
    private val clienteRepository = ClienteRepository()
    private val eventoRepository = EventoRepository()
    private val servicioRepository = ServicioRepository()

    private val _listaClientes = MutableStateFlow<List<Cliente>>(emptyList())
    val listaClientes: StateFlow<List<Cliente>> = _listaClientes.asStateFlow()

    private val _listaServicios = MutableStateFlow<List<ServicioEvento>>(emptyList())
    val listaServicios: StateFlow<List<ServicioEvento>> = _listaServicios.asStateFlow()

    var estaGuardando by mutableStateOf(false)
        private set
    var guardadoExitoso by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        cargarClientes()
        cargarServicios()
    }

    private fun cargarClientes() {
        viewModelScope.launch {
            clienteRepository.obtenerClientes().collect { clientes ->
                _listaClientes.value = clientes
            }
        }
    }

    private fun cargarServicios() {
        viewModelScope.launch {
            servicioRepository.obtenerServicios().collect { servicios ->
                _listaServicios.value = servicios
            }
        }
    }

    fun guardarServicio(servicio: ServicioEvento) {
        viewModelScope.launch {
            val result = servicioRepository.guardarServicio(servicio)
            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al guardar servicio"
            }
        }
    }

    fun actualizarServicio(servicio: ServicioEvento) {
        viewModelScope.launch {
            val result = servicioRepository.actualizarServicio(servicio)
            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al actualizar servicio"
            }
        }
    }

    fun eliminarServicio(servicioId: String) {
        viewModelScope.launch {
            val result = servicioRepository.eliminarServicio(servicioId)
            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al eliminar servicio"
            }
        }
    }

    fun agendarEvento(
        tipoEvento: String,
        clienteId: String,
        clienteNombre: String,
        fecha: String,
        hora: String,
        notas: String,
        servicios: List<ServicioEvento>
    ) {
        viewModelScope.launch {
            estaGuardando = true
            errorMessage = null

            val evento = Evento(
                tipoEvento = tipoEvento,
                clienteId = clienteId,
                clienteNombre = clienteNombre,
                fecha = fecha,
                hora = hora,
                notas = notas,
                servicios = servicios,
                estado = "Activo"
            )

            val result = eventoRepository.guardarEvento(evento)

            if (result.isSuccess) {
                guardadoExitoso = true
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al guardar evento"
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
