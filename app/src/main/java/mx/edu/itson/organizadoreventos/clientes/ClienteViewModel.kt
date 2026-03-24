package mx.edu.itson.organizadoreventos.clientes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClienteViewModel : ViewModel() {

    // ESTADO DE LA UI
    var nombre by mutableStateOf("")
    var telefono by mutableStateOf("")
    var correo by mutableStateOf("")
    var tipoEvento by mutableStateOf("")

    // ESTADO DE ERRORES Y CARGA
    var errorNombre by mutableStateOf(false)
    var errorTelefono by mutableStateOf(false)
    var estaGuardando by mutableStateOf(false)
    var guardadoExitoso by mutableStateOf(false)

    fun guardarClienteMock() {
        errorNombre = nombre.isBlank()
        errorTelefono = telefono.isBlank()

        if (!errorNombre && !errorTelefono) {
            viewModelScope.launch {
                estaGuardando = true
                
                // Simula el tiempo que tardaría en guardar en una base de datos o servidor
                delay(1000) 
                
                // Limpia el formulario
                nombre = ""
                telefono = ""
                correo = ""
                tipoEvento = ""
                
                estaGuardando = false
                guardadoExitoso = true
                
                println("Mock: El cliente se guardó correctamente para la demostración.")
            }
        }
    }
    
    fun resetEstadoExito() {
        guardadoExitoso = false
    }
}