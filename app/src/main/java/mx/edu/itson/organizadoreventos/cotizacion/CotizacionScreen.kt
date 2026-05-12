package mx.edu.itson.organizadoreventos.cotizacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.itson.organizadoreventos.finanzas.totalEstimado
import mx.edu.itson.organizadoreventos.model.Evento

@Composable
fun CotizacionScreen(viewModel: CotizacionViewModel = viewModel()) {
    val eventos by viewModel.listaEventos.collectAsState()
    val isLoading = viewModel.isLoading

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Resumen de Cotizaciones",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (eventos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay eventos activos para cotizar.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(eventos) { evento ->
                    TicketCotizacion(evento = evento)
                }
            }
        }
    }
}

@Composable
fun TicketCotizacion(evento: Evento) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("TICKET DE EVENTO - ${evento.tipoEvento.uppercase()}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            
            Text("Cliente: ${evento.clienteNombre}", style = MaterialTheme.typography.bodyLarge)
            Text("Fecha: ${evento.fecha}", style = MaterialTheme.typography.bodyMedium)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            Text("DESGLOSE DE SERVICIOS:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            
            evento.servicios.forEach { servicio ->
                FilaTicket(servicio.nombre, "$${servicio.precio}")
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("TOTAL ESTIMADO", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("$${evento.totalEstimado}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun FilaTicket(concepto: String, precio: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(concepto, style = MaterialTheme.typography.bodyMedium)
        Text(precio, fontWeight = FontWeight.Medium)
    }
}