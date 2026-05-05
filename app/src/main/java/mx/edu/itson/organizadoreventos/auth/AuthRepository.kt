package mx.edu.itson.organizadoreventos.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()

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

    fun cerrarSesion() {
        auth.signOut()
    }

    fun obtenerUsuarioActual(): FirebaseUser? {
        return auth.currentUser
    }
}
