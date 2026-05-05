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

class FinanzasViewModel : ViewModel() {
    private val repository = EventoRepository()

    private val _listaEventos = MutableStateFlow<List<Evento>>(emptyList())
    val listaEventos: StateFlow<List<Evento>> = _listaEventos.asStateFlow()

    var isUpdating by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        cargarEventos()
    }

    private fun cargarEventos() {
        viewModelScope.launch {
            repository.obtenerEventos().collect { eventos ->
                _listaEventos.value = eventos
            }
        }
    }

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

    fun clearError() {
        errorMessage = null
    }
}
