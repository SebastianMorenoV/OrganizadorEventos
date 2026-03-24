package mx.edu.itson.organizadoreventos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import mx.edu.itson.organizadoreventos.navigation.AppNavigation
import mx.edu.itson.organizadoreventos.navigation.MainApp
import mx.edu.itson.organizadoreventos.ui.theme.OrganizadorEventosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrganizadorEventosTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation() // <-- Cambiamos MainApp por AppNavigation aquí
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OrganizadorEventosTheme {
        Greeting("Android")
    }
}