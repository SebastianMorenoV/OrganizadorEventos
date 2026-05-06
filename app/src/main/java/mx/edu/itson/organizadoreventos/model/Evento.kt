package mx.edu.itson.organizadoreventos.model

data class ServicioEvento(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Float = 0f,
    val seleccionado: Boolean = false
)

data class Evento(
    val id: String = "",
    val tipoEvento: String = "",
    val clienteId: String = "",
    val clienteNombre: String = "",
    val fecha: String = "",
    val hora: String = "",
    val notas: String = "",
    val servicios: List<ServicioEvento> = emptyList(),
    val abonos: Map<String, Abono> = emptyMap(),
    val estado: String = "Activo"
)

data class Abono(
    val id: String = "",
    val fecha: String = "",
    val monto: String = ""
)
