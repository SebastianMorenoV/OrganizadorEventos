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

class ServicioRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getServiciosRef() = auth.currentUser?.uid?.let { uid ->
        database.getReference("usuarios").child(uid).child("servicios")
    }

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

    suspend fun actualizarServicio(servicio: ServicioEvento): Result<Unit> {
        val ref = getServiciosRef() ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            ref.child(servicio.id).setValue(servicio).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
