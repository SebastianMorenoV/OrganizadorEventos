package mx.edu.itson.organizadoreventos.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Repositorio que gestiona la autenticación de usuarios mediante Firebase Authentication.
 * Soporta registro, inicio de sesión y cierre de sesión con correo y contraseña.
 */
class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

    /**
     * Inicia sesión con correo electrónico y contraseña en Firebase Authentication.
     *
     * @param correo Correo electrónico del usuario registrado.
     * @param contrasena Contraseña del usuario.
     * @return [Result.success] con el [FirebaseUser] autenticado, o [Result.failure] si las
     *         credenciales son incorrectas o hay un error de red.
     */
    suspend fun iniciarSesion(correo: String, contrasena: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(correo, contrasena).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario nulo después del inicio de sesión"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registra un nuevo usuario en Firebase Authentication con correo y contraseña.
     *
     * @param correo Correo electrónico para la nueva cuenta.
     * @param contrasena Contraseña de la nueva cuenta (mínimo 6 caracteres).
     * @return [Result.success] con el [FirebaseUser] creado, o [Result.failure] si el correo
     *         ya existe o la contraseña no cumple los requisitos.
     */
    suspend fun registrar(correo: String, contrasena: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(correo, contrasena).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Usuario nulo después del registro"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cierra la sesión del usuario actualmente autenticado en Firebase.
     */
    fun cerrarSesion() {
        auth.signOut()
    }

    /**
     * Retorna el [FirebaseUser] actualmente autenticado, o `null` si no hay sesión activa.
     */
    fun obtenerUsuarioActual(): FirebaseUser? {
        return auth.currentUser
    }
}
