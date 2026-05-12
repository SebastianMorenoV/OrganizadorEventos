package mx.edu.itson.organizadoreventos.finanzas

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.itson.organizadoreventos.data.EventoRepository
import mx.edu.itson.organizadoreventos.model.Abono
import mx.edu.itson.organizadoreventos.model.Evento

/**
 * ViewModel para el módulo de Finanzas.
 * Administra la lista de eventos en tiempo real y expone operaciones para
 * registrar abonos, actualizar el estado y eliminar eventos.
 */
class FinanzasViewModel : ViewModel() {
    private val repository = EventoRepository()

    /** Lista de eventos obtenida en tiempo real desde Firebase. */
    private val _listaEventos = MutableStateFlow<List<Evento>>(emptyList())
    val listaEventos: StateFlow<List<Evento>> = _listaEventos.asStateFlow()

    /** Indica si hay una operación de escritura en curso para bloquear la UI. */
    var isUpdating by mutableStateOf(false)
        private set

    /** Mensaje de error para mostrar al usuario mediante Snackbar. Null si no hay error. */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        cargarEventos()
    }

    /**
     * Inicia la observación en tiempo real de los eventos del usuario autenticado.
     * Se llama automáticamente al crear el ViewModel.
     */
    private fun cargarEventos() {
        viewModelScope.launch {
            repository.obtenerEventos().collect { eventos ->
                _listaEventos.value = eventos
            }
        }
    }

    /**
     * Registra un nuevo abono (pago parcial) para un evento.
     *
     * @param eventoId ID del evento al que se le añade el abono.
     * @param fecha Fecha del pago en formato "DD/MM/YYYY".
     * @param monto Cantidad abonada como cadena numérica.
     */
    fun registrarAbono(eventoId: String, fecha: String, monto: String) {
        viewModelScope.launch {
            isUpdating = true
            errorMessage = null

            val abono = Abono(fecha = fecha, monto = monto)
            val result = repository.agregarAbono(eventoId, abono)

            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar abono"
            }

            isUpdating = false
        }
    }

    /**
     * Cambia el estado de un evento (p.ej. "Activo", "Terminado", "Cancelado").
     *
     * @param eventoId ID del evento a actualizar.
     * @param estado Nuevo estado a asignar.
     */
    fun actualizarEstadoEvento(eventoId: String, estado: String) {
        viewModelScope.launch {
            isUpdating = true
            errorMessage = null

            val result = repository.actualizarEstadoEvento(eventoId, estado)

            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al actualizar estado"
            }

            isUpdating = false
        }
    }

    /**
     * Actualiza todos los campos de un evento existente (UPDATE completo).
     * Reemplaza el nodo del evento en Firebase con el objeto proporcionado.
     *
     * @param evento Objeto [Evento] con los campos editados.
     */
    fun actualizarEvento(evento: Evento) {
        viewModelScope.launch {
            isUpdating = true
            errorMessage = null

            val result = repository.actualizarEvento(evento)

            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al actualizar evento"
            }

            isUpdating = false
        }
    }

    /**
     * Elimina permanentemente un evento de Firebase (DELETE).
     *
     * @param eventoId ID único del evento a eliminar.
     */
    fun eliminarEvento(eventoId: String) {
        viewModelScope.launch {
            isUpdating = true
            errorMessage = null

            val result = repository.eliminarEvento(eventoId)

            if (result.isFailure) {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al eliminar evento"
            }

            isUpdating = false
        }
    }

    /** Limpia el mensaje de error actual. */
    fun clearError() {
        errorMessage = null
    }
}
