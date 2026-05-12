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
import mx.edu.itson.organizadoreventos.model.Cliente

/**
 * Repositorio que gestiona todas las operaciones CRUD de [Cliente]
 * en Firebase Realtime Database bajo la ruta `usuarios/{uid}/clientes`.
 */
class ClienteRepository {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Retorna la referencia de Firebase para los clientes del usuario autenticado.
     * Devuelve `null` si no hay sesión activa.
     */
    private fun getClientesRef() = auth.currentUser?.uid?.let { uid ->
        database.getReference("usuarios").child(uid).child("clientes")
    }

    /**
     * Guarda un nuevo cliente en Firebase (CREATE).
     * Genera un ID único con `push()` y almacena el objeto serializado.
     *
     * @param cliente Objeto [Cliente] a persistir.
     * @return [Result.success] si se guardó correctamente, [Result.failure] en caso de error.
     */
    suspend fun guardarCliente(cliente: Cliente): Result<Unit> {
        val ref = getClientesRef() ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            val key = ref.push().key ?: return Result.failure(Exception("Error al generar ID de cliente"))
            val nuevoCliente = cliente.copy(id = key)
            ref.child(key).setValue(nuevoCliente).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Observa en tiempo real la lista de clientes del usuario autenticado (READ).
     * Emite una nueva lista cada vez que Firebase detecta cambios.
     *
     * @return [Flow] que emite listas de [Cliente] en tiempo real.
     */
    fun obtenerClientes(): Flow<List<Cliente>> = callbackFlow {
        val ref = getClientesRef()
        if (ref == null) {
            close(Exception("Usuario no autenticado"))
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val clientes = mutableListOf<Cliente>()
                for (childSnapshot in snapshot.children) {
                    val cliente = childSnapshot.getValue(Cliente::class.java)
                    if (cliente != null) {
                        clientes.add(cliente)
                    }
                }
                trySend(clientes)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * Actualiza todos los campos de un cliente existente en Firebase (UPDATE).
     * El [Cliente.id] debe corresponder a un nodo existente.
     *
     * @param cliente Objeto [Cliente] con los datos actualizados.
     * @return [Result.success] si se actualizó correctamente, [Result.failure] en caso de error.
     */
    suspend fun actualizarCliente(cliente: Cliente): Result<Unit> {
        val ref = getClientesRef() ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            ref.child(cliente.id).setValue(cliente).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina permanentemente un cliente de Firebase (DELETE).
     *
     * @param clienteId ID único del cliente a eliminar.
     * @return [Result.success] si se eliminó correctamente, [Result.failure] en caso de error.
     */
    suspend fun eliminarCliente(clienteId: String): Result<Unit> {
        val ref = getClientesRef() ?: return Result.failure(Exception("Usuario no autenticado"))
        
        return try {
            ref.child(clienteId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
