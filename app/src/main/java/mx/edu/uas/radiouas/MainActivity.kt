package mx.edu.uas.radiouas

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.launch
import mx.edu.uas.radiouas.ui.theme.RadioUASTheme

//import mx.edu.uas.radiouas.ui.screens.ProgramacionScreen

// Color Azul Marino Institucional UAS
val AzulUAS = Color(0xFF002D56)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
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
                    "Programacion" -> ProgramacionScreen(
                        // Pasamos el ViewModel de programación
                        //viewModel = programacionViewModel,

                        // Conectamos el clic de la tarjeta azul
                        onPlayRadio = {
                            // 1. Obtenemos qué está sonando actualmente
                            val urlActual = radioViewModel.player?.currentMediaItem?.localConfiguration?.uri?.toString()

                            // 2. Verificamos si es la Radio
                            val esLaRadio = urlActual == radioViewModel.streamURL

                            // 3. Lógica de protección:
                            // Solo mandamos la orden si NO está sonando, O si está sonando otra cosa (podcast)
                            if (!radioViewModel.isPlaying || !esLaRadio) {
                                radioViewModel.playRadioOAlternar()
                            }
                            // Si ya está sonando la radio (isPlaying && esLaRadio), no hacemos nada.
                            // Así evitamos que se pause por error.
                        }
                    )
                    "Podcasts" -> PodcastsScreen(radioViewModel)
                }
            }
        }
    }
}