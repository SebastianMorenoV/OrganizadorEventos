package mx.edu.itson.organizadoreventos.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

/**
 * ViewModel para el módulo de autenticación.
 * Gestiona el estado de sesión del usuario, el proceso de login y registro,
 * y expone el estado de carga y mensajes de error a la UI.
 */
class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    /** Usuario de Firebase actualmente autenticado. Null si no hay sesión activa. */
    var usuarioActual by mutableStateOf<FirebaseUser?>(repository.obtenerUsuarioActual())
        private set

    /** Indica si hay una operación de autenticación en curso. */
    var isLoading by mutableStateOf(false)
        private set

    /** Mensaje de error de autenticación para mostrar en la UI. Null si no hay error. */
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** Bandera que indica que la autenticación fue exitosa. Usada para disparar navegación. */
    var authSuccess by mutableStateOf(false)
        private set

    /**
     * Inicia sesión con correo y contraseña.
     * Actualiza [usuarioActual] y activa [authSuccess] al completarse correctamente.
     *
     * @param correo Correo electrónico del usuario.
     * @param contrasena Contraseña del usuario.
     */
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

    /**
     * Registra una nueva cuenta de usuario con correo, nombre y contraseña.
     * Activa [authSuccess] al completarse correctamente.
     *
     * @param nombre Nombre del usuario (validación local).
     * @param correo Correo electrónico para la nueva cuenta.
     * @param contrasena Contraseña de la nueva cuenta.
     */
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

    /**
     * Cierra la sesión del usuario actual y limpia el estado del ViewModel.
     */
    fun cerrarSesion() {
        repository.cerrarSesion()
        usuarioActual = null
        authSuccess = false
    }

    /** Limpia el mensaje de error actual. */
    fun clearError() {
        errorMessage = null
    }

    /** Resetea la bandera [authSuccess] después de que la navegación fue procesada. */
    fun resetAuthSuccess() {
        authSuccess = false
    }
}
