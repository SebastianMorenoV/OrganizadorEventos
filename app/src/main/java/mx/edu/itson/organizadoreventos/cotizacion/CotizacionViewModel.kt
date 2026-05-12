package mx.edu.itson.organizadoreventos.cotizacion

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
import mx.edu.itson.organizadoreventos.model.Evento

/**
 * ViewModel para el módulo de Cotización.
 * Carga en tiempo real los eventos activos desde Firebase para generar
 * tickets de cotización con datos reales del cliente y los servicios contratados.
 */
class CotizacionViewModel : ViewModel() {
    private val repository = EventoRepository()

    /** Lista de eventos obtenida en tiempo real desde Firebase. */
    private val _listaEventos = MutableStateFlow<List<Evento>>(emptyList())
    val listaEventos: StateFlow<List<Evento>> = _listaEventos.asStateFlow()

    /** Indica si los datos están siendo cargados por primera vez. */
    var isLoading by mutableStateOf(true)
        private set

    init {
        cargarEventos()
    }

    /**
     * Inicia la observación en tiempo real de los eventos del usuario autenticado.
     * Filtra para mostrar solo eventos con al menos un servicio asignado.
     */
    private fun cargarEventos() {
        viewModelScope.launch {
            repository.obtenerEventos().collect { eventos ->
                _listaEventos.value = eventos.filter { it.servicios.isNotEmpty() }
                isLoading = false
            }
        }
    }
}
