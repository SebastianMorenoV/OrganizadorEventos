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
import mx.edu.itson.organizadoreventos.model.Abono
import mx.edu.itson.organizadoreventos.model.Evento

class EventoRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getEventosRef() = auth.currentUser?.uid?.let { uid ->
        database.getReference("usuarios").child(uid).child("eventos")
    }

    suspend fun guardarEvento(evento: Evento): Result<Unit> {
        val ref = getEventosRef() ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            val key = ref.push().key ?: return Result.failure(Exception("Error al generar ID de evento"))
            val nuevoEvento = evento.copy(id = key)
            ref.child(key).setValue(nuevoEvento).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun obtenerEventos(): Flow<List<Evento>> = callbackFlow {
        val ref = getEventosRef()
        if (ref == null) {
            close(Exception("Usuario no autenticado"))
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventos = mutableListOf<Evento>()
                for (childSnapshot in snapshot.children) {
                    val evento = childSnapshot.getValue(Evento::class.java)
                    if (evento != null) {
                        eventos.add(evento)
                    }
                }
                trySend(eventos)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun actualizarEstadoEvento(eventoId: String, estado: String): Result<Unit> {
        val ref = getEventosRef() ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            ref.child(eventoId).child("estado").setValue(estado).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun agregarAbono(eventoId: String, abono: Abono): Result<Unit> {
        val ref = getEventosRef() ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            val abonoRef = ref.child(eventoId).child("abonos").push()
            val abonoKey = abonoRef.key ?: return Result.failure(Exception("Error al generar ID de abono"))
            
            val nuevoAbono = abono.copy(id = abonoKey)
            abonoRef.setValue(nuevoAbono).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
