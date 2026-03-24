package mx.edu.itson.organizadoreventos.finanzas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanzasScreen() {
    var showSheet by remember { mutableStateOf(false) }

    // Historial dinámico
    val listaPagos = remember {
        mutableStateListOf(
            Pair("20/03/2026", "5000"),
            Pair("05/03/2026", "7000")
        )
    }

    val totalEvento = 33500f
    val pagado = listaPagos.sumOf { it.second.toIntOrNull() ?: 0 }.toFloat()
    val progreso = (pagado / totalEvento).coerceAtMost(1f)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                // Toma el color Dorado (Primary) de tu Theme.kt automáticamente
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                // Error de "tint" corregido aquí
                Icon(Icons.Default.Add, contentDescription = "Registrar Pago", tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Control de Pagos", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(20.dp))

            // Gráfico dinámico con los colores de tu aplicación
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(
                    progress = { progreso },
                    modifier = Modifier.size(150.dp),
                    strokeWidth = 12.dp,
                    color = MaterialTheme.colorScheme.primary, // Usa el Dorado
                    trackColor = Color.LightGray // Gris de fondo
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${(progreso * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Liquidado", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Anticipo Realizado", style = MaterialTheme.typography.labelMedium)
                    // Texto en Dorado
                    Text("$$pagado", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Saldo Restante", style = MaterialTheme.typography.labelMedium)
                    Text("$${(totalEvento - pagado).coerceAtLeast(0f)}", style = MaterialTheme.typography.titleLarge, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Historial de Abonos", fontWeight = FontWeight.Bold)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(listaPagos.reversed()) { pago ->
                    ListItem(
                        headlineContent = { Text("Abono recibido") },
                        supportingContent = { Text("Fecha: ${pago.first}") },
                        trailingContent = {
                            // Este verde (SuccessGreen) sí estaba en tu paleta para indicar éxito
                            Text("$${pago.second}", color = Color(0xFF388E3C), fontWeight = FontWeight.Bold)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(onDismissRequest = { showSheet = false }) {
                FormularioPago(onConfirm = { fecha, monto ->
                    if (monto.isNotEmpty()) {
                        listaPagos.add(Pair(fecha, monto))
                    }
                    showSheet = false
                })
            }
        }
    }
}

@Composable
fun FormularioPago(onConfirm: (String, String) -> Unit) {
    var monto by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("24/03/2026") }

    Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp).fillMaxWidth()) {
        Text("Registrar Nuevo Abono", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = monto,
            onValueChange = { if (it.all { char -> char.isDigit() }) monto = it },
            label = { Text("¿Cuánto desea abonar?") },
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("$ ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = fecha,
            onValueChange = { fecha = it },
            label = { Text("Fecha del abono") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onConfirm(fecha, monto) },
            modifier = Modifier.fillMaxWidth(),
            enabled = monto.isNotEmpty()
            // Al quitar el 'colors' fijo de aquí, Compose usará automáticamente tu color primario (Dorado)
        ) {
            Text("Confirmar y Actualizar")
        }
    }
}