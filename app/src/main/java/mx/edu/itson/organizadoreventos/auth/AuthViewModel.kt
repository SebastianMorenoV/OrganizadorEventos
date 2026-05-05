package mx.edu.itson.organizadoreventos.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    var usuarioActual by mutableStateOf<FirebaseUser?>(repository.obtenerUsuarioActual())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var authSuccess by mutableStateOf(false)
        private set

    fun iniciarSesion(correo: String, contrasena: String) {
        if (correo.isBlank() || contrasena.isBlank()) {
            errorMessage = "Por favor ingresa correo y contraseña"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val result = repository.iniciarSesion(correo, contrasena)
            if (result.isSuccess) {
                usuarioActual = result.getOrNull()
                authSuccess = true
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al iniciar sesión"
            }
            isLoading = false
        }
    }

    fun registrar(nombre: String, correo: String, contrasena: String) {
        if (nombre.isBlank() || correo.isBlank() || contrasena.isBlank()) {
            errorMessage = "Por favor llena todos los campos"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            val result = repository.registrar(correo, contrasena)
            if (result.isSuccess) {
                usuarioActual = result.getOrNull()
                authSuccess = true
            } else {
                errorMessage = result.exceptionOrNull()?.message ?: "Error al registrar"
            }
            isLoading = false
        }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
        usuarioActual = null
        authSuccess = false
    }

    fun clearError() {
        errorMessage = null
    }

    fun resetAuthSuccess() {
        authSuccess = false
    }
}
