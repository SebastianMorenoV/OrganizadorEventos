package mx.edu.itson.organizadoreventos.cotizacion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CotizacionScreen() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Resumen de Cotización",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("TICKET DE EVENTO", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                // Datos del Cliente (Simulados)
                Text("Cliente: Luciano Barceló", style = MaterialTheme.typography.bodyLarge)
                Text("Fecha: 25 de marzo, 2026", style = MaterialTheme.typography.bodyMedium)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Desglose de servicios
                FilaTicket("Renta de Salón", "$10,000.00")
                FilaTicket("Banquete (100 pers.)", "$15,000.00")
                FilaTicket("Servicio de Bebidas", "$5,000.00")
                FilaTicket("Música y DJ", "$3,500.00")
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("TOTAL ESTIMADO", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Text("$33,500.00", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                }
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