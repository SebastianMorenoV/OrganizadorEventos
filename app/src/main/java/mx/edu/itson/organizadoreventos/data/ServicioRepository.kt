package mx.edu.itson.organizadoreventos.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.edu.itson.organizadoreventos.model.ServicioEvento

/**
 * Repositorio que gestiona todas las operaciones CRUD de [ServicioEvento]
 * (catálogo de servicios) en Firebase Realtime Database
 * bajo la ruta `usuarios/{uid}/servicios`.
 */
class ServicioRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Retorna la referencia de Firebase para los servicios del usuario autenticado.
     * Devuelve `null` si no hay sesión activa.
     */
    private fun getServiciosRef() = auth.currentUser?.uid?.let { uid ->
        database.getReference("usuarios").child(uid).child("servicios")
    }

    /**
     * Guarda un nuevo servicio en el catálogo de Firebase (CREATE).
     * Genera un ID único con `push()` y almacena el objeto serializado.
     *
     * @param servicio Objeto [ServicioEvento] a persistir.
     * @return [Result.success] si se guardó correctamente, [Result.failure] en caso de error.
     */
    suspend fun guardarServicio(servicio: ServicioEvento): Result<Unit> {
        val ref = getServiciosRef() ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            val key = ref.push().key ?: return Result.failure(Exception("Error al generar ID de servicio"))
            val nuevoServicio = servicio.copy(id = key)
            ref.child(key).setValue(nuevoServicio).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observa en tiempo real el catálogo de servicios del usuario autenticado (READ).
     * Emite una nueva lista cada vez que Firebase detecta cambios.
     *
     * @return [Flow] que emite listas de [ServicioEvento] en tiempo real.
     */
    fun obtenerServicios(): Flow<List<ServicioEvento>> = callbackFlow {
        val ref = getServiciosRef()
        if (ref == null) {
            close(Exception("Usuario no autenticado"))
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val servicios = mutableListOf<ServicioEvento>()
                for (childSnapshot in snapshot.children) {
                    val servicio = childSnapshot.getValue(ServicioEvento::class.java)
                    if (servicio != null) {
                        servicios.add(servicio)
                    }
                }
                trySend(servicios)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * Actualiza todos los campos de un servicio existente en el catálogo (UPDATE).
     * El [ServicioEvento.id] debe corresponder a un nodo existente en Firebase.
     *
     * @param servicio Objeto [ServicioEvento] con los datos actualizados.
     * @return [Result.success] si se actualizó correctamente, [Result.failure] en caso de error.
     */
    suspend fun actualizarServicio(servicio: ServicioEvento): Result<Unit> {
        val ref = getServiciosRef() ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            ref.child(servicio.id).setValue(servicio).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina permanentemente un servicio del catálogo en Firebase (DELETE).
     *
     * @param servicioId ID único del servicio a eliminar.
     * @return [Result.success] si se eliminó correctamente, [Result.failure] en caso de error.
     */
    suspend fun eliminarServicio(servicioId: String): Result<Unit> {
        val ref = getServiciosRef() ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            ref.child(servicioId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
