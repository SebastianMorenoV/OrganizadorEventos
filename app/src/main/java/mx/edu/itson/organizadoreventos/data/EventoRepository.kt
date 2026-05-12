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

/**
 * Repositorio que gestiona todas las operaciones CRUD de [Evento]
 * en Firebase Realtime Database bajo la ruta `usuarios/{uid}/eventos`.
 */
class EventoRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Retorna la referencia de Firebase para los eventos del usuario autenticado.
     * Devuelve `null` si no hay sesión activa.
     */
    private fun getEventosRef() = auth.currentUser?.uid?.let { uid ->
        database.getReference("usuarios").child(uid).child("eventos")
    }

    /**
     * Guarda un nuevo evento en Firebase (CREATE).
     * Genera un ID único con `push()` y almacena el objeto serializado.
     *
     * @param evento Objeto [Evento] a persistir.
     * @return [Result.success] si se guardó correctamente, [Result.failure] en caso de error.
     */
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

    /**
     * Observa en tiempo real la lista de eventos del usuario autenticado (READ).
     * Emite una nueva lista cada vez que Firebase detecta cambios.
     *
     * @return [Flow] que emite listas de [Evento] en tiempo real.
     */
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

    /**
     * Actualiza todos los campos de un evento existente en Firebase (UPDATE).
     * Reemplaza el nodo completo con los nuevos valores del objeto.
     *
     * @param evento Objeto [Evento] con los datos actualizados. Su [Evento.id] debe existir.
     * @return [Result.success] si se actualizó correctamente, [Result.failure] en caso de error.
     */
    suspend fun actualizarEvento(evento: Evento): Result<Unit> {
        val ref = getEventosRef() ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            ref.child(evento.id).setValue(evento).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza únicamente el campo `estado` de un evento (UPDATE parcial).
     * Usado para marcar un evento como "Activo", "Terminado" o "Cancelado".
     *
     * @param eventoId ID único del evento a actualizar.
     * @param estado Nuevo estado a asignar.
     * @return [Result.success] si se actualizó correctamente, [Result.failure] en caso de error.
     */
    suspend fun actualizarEstadoEvento(eventoId: String, estado: String): Result<Unit> {
        val ref = getEventosRef() ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            ref.child(eventoId).child("estado").setValue(estado).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina permanentemente un evento de Firebase (DELETE).
     *
     * @param eventoId ID único del evento a eliminar.
     * @return [Result.success] si se eliminó correctamente, [Result.failure] en caso de error.
     */
    suspend fun eliminarEvento(eventoId: String): Result<Unit> {
        val ref = getEventosRef() ?: return Result.failure(Exception("Usuario no autenticado"))

        return try {
            ref.child(eventoId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Agrega un abono al historial de pagos de un evento (UPDATE anidado).
     * Genera un ID único para el abono con `push()`.
     *
     * @param eventoId ID del evento al que pertenece el abono.
     * @param abono Objeto [Abono] con fecha y monto del pago.
     * @return [Result.success] si se guardó correctamente, [Result.failure] en caso de error.
     */
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
