package mx.edu.uas.radiouas

import mx.edu.uas.radiouas.ui.theme.RadioUASTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import mx.edu.uas.radiouas.ui.screens.ProgramacionScreen

// Color Azul Marino Institucional UAS
val AzulUAS = Color(0xFF002D56)

class MainActivity : ComponentActivity() {
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtenemos la instancia del ViewModel
        val radioViewModel = ViewModelProvider(this).get(RadioViewModel::class.java)

        setContent {
            RadioUASTheme {
                // Pasamos el viewModel a tu navegación o pantalla principal
                MainScreen(radioViewModel)
            }
        }
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(radioViewModel: RadioViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // Variable para saber qué sección mostrar
    var currentSection by remember { mutableStateOf("Noticias") }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(12.dp))
                Text("  RADIO UAS", style = MaterialTheme.typography.headlineSmall, color = AzulUAS)
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Opciones del Menú
                NavigationDrawerItem(label = { Text("Noticias") }, selected = currentSection == "Noticias",
                    onClick = { currentSection = "Noticias"; scope.launch { drawerState.close() } })
                NavigationDrawerItem(label = { Text("Radio en Vivo") }, selected = currentSection == "Radio",
                    onClick = { currentSection = "Radio"; scope.launch { drawerState.close() } })
                NavigationDrawerItem(label = { Text("TV en Vivo") }, selected = currentSection == "Video",
                    onClick = { currentSection = "Video"; scope.launch { drawerState.close() } })
                NavigationDrawerItem(label = { Text("Programación") }, selected = currentSection == "Programacion",
                    onClick = { currentSection = "Programacion"; scope.launch { drawerState.close() } })
                NavigationDrawerItem(label = { Text("Podcasts (Emby)") }, selected = currentSection == "Podcasts",
                    onClick = { currentSection = "Podcasts"; scope.launch { drawerState.close() } })
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Radio UAS", color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulUAS),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.White)
                        }
                    }
                )

            },
            bottomBar = {
                MiniPlayer(radioViewModel = radioViewModel)
            }
        ) {
            paddingValues ->
            Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                // Aquí llamaremos a cada sección según la selección
                when (currentSection) {
                    "Noticias" -> NoticiasScreen()
                    "Radio" -> RadioPlayerScreen(radioViewModel)
                    "Video" -> VideoPlayerScreen() // <--- Ahora ya tiene funcionalidad real
                    "Programacion" -> ProgramacionScreen()
                    "Podcasts" -> PodcastsScreen(radioViewModel)
                }
            }
        }
    }
}