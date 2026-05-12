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

/**
 * ViewModel para el módulo de Agenda.
 * Gestiona el catálogo de servicios, la lista de clientes para el selector,
 * y las operaciones CRUD del catálogo de servicios y creación de eventos.
 */
class AgendaViewModel : ViewModel() {
    private val clienteRepository = ClienteRepository()
    private val eventoRepository = EventoRepository()
    private val servicioRepository = ServicioRepository()

    /** Lista de clientes disponibles para asignar a un evento, en tiempo real. */
    private val _listaClientes = MutableStateFlow<List<Cliente>>(emptyList())
    val listaClientes: StateFlow<List<Cliente>> = _listaClientes.asStateFlow()

    /** Catálogo de servicios disponibles para agregar a un evento, en tiempo real. */
    private val _listaServicios = MutableStateFlow<List<ServicioEvento>>(emptyList())
    val listaServicios: StateFlow<List<ServicioEvento>> = _listaServicios.asStateFlow()

    /** Indica si hay una operación de escritura en curso para bloquear el botón de agendar. */
    var estaGuardando by mutableStateOf(false)
        private set

    /** Bandera que indica que el evento fue agendado con éxito para mostrar el diálogo. */
    var guardadoExitoso by mutableStateOf(false)
        private set

    /** Mensaje de error para mostrar en Snackbar. Null si no hay error. */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        cargarClientes()
        cargarServicios()
    }

    /**
     * Inicia la observación en tiempo real de los clientes. Llamado automáticamente al crear el VM.
     */
    private fun cargarClientes() {
        viewModelScope.launch {
            clienteRepository.obtenerClientes().collect { clientes ->
                _listaClientes.value = clientes
            }
        }
    }

    /**
     * Inicia la observación en tiempo real del catálogo de servicios. Llamado automáticamente.
     */
    private fun cargarServicios() {
        viewModelScope.launch {
            servicioRepository.obtenerServicios().collect { servicios ->
                _listaServicios.value = servicios
            }
        }
    }

    /**
     * Agrega un nuevo servicio al catálogo en Firebase (CREATE).
     *
     * @param servicio Objeto [ServicioEvento] a persistir en el catálogo.
     */
    fun guardarServicio(servicio: ServicioEvento) {
        viewModelScope.launch {
            val result = servicioRepository.guardarServicio(servicio)
            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al guardar servicio"
            }
        }
    }

    /**
     * Actualiza los datos de un servicio existente en el catálogo de Firebase (UPDATE).
     *
     * @param servicio Objeto [ServicioEvento] con los datos actualizados.
     */
    fun actualizarServicio(servicio: ServicioEvento) {
        viewModelScope.launch {
            val result = servicioRepository.actualizarServicio(servicio)
            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al actualizar servicio"
            }
        }
    }

    /**
     * Elimina un servicio del catálogo en Firebase (DELETE).
     *
     * @param servicioId ID único del servicio a eliminar.
     */
    fun eliminarServicio(servicioId: String) {
        viewModelScope.launch {
            val result = servicioRepository.eliminarServicio(servicioId)
            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al eliminar servicio"
            }
        }
    }

    /**
     * Crea y guarda un nuevo evento en Firebase con los datos del formulario de Agenda.
     * Activa [guardadoExitoso] para que la UI muestre el diálogo de confirmación.
     *
     * @param tipoEvento Tipo de celebración (p.ej. "Boda", "XV Años").
     * @param clienteId ID del cliente dueño del evento.
     * @param clienteNombre Nombre del cliente para mostrar en listas.
     * @param fecha Fecha del evento en formato "DD/MM/YYYY".
     * @param hora Hora del evento seleccionada del catálogo.
     * @param notas Instrucciones especiales o notas adicionales.
     * @param servicios Lista de [ServicioEvento] seleccionados para el evento.
     */
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

    /** Resetea la bandera [guardadoExitoso] después de procesar el diálogo de confirmación. */
    fun resetEstadoExito() {
        guardadoExitoso = false
    }

    /** Limpia el mensaje de error actual. */
    fun clearError() {
        errorMessage = null
    }
}
