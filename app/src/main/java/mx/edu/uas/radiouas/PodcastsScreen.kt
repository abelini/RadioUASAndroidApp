package mx.edu.uas.radiouas

import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.text.style.TextOverflow

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableApi::class)
@Composable
fun PodcastsScreen(radioViewModel: RadioViewModel) {

    // NAVEGACIÓN SIMPLE:
    if (radioViewModel.programaSeleccionado == null) {
        // VISTA 1: GRID DE PROGRAMAS (ÁLBUMES)
        VistaListaProgramas(radioViewModel)
    } else {
        // VISTA 2: LISTA DE EPISODIOS (PISTAS)
        VistaDetallePrograma(radioViewModel)
    }
}

// --- SUB-VISTA: LISTA DE PROGRAMAS ---
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableApi::class)
@Composable
fun VistaListaProgramas(radioViewModel: RadioViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Programas",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp),
            color = AzulUAS
        )

        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(radioViewModel.listaPodcasts) { programa ->
                PodcastCard(
                    podcast = programa,
                    radioViewModel = radioViewModel,
                    onClick = {
                        // AQUÍ CAMBIA LA LÓGICA:
                        // En lugar de reproducir, "abrimos" el folder
                        radioViewModel.abrirPrograma(programa)
                    }
                )
            }
        }
    }
}

// --- SUB-VISTA: DETALLE DE EPISODIOS ---
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableApi::class)
@Composable
fun VistaDetallePrograma(radioViewModel: RadioViewModel) {
    val programa = radioViewModel.programaSeleccionado!!

    Column(modifier = Modifier.fillMaxSize()) {

        // ENCABEZADO CON BOTÓN ATRÁS
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { radioViewModel.cerrarPrograma() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = AzulUAS)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = programa.titulo, // Título del Programa arriba
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // LISTA DE EPISODIOS
        // ... dentro de VistaDetallePrograma ...

        LazyColumn(
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. LOS EPISODIOS
            items(radioViewModel.listaEpisodios) { episodio ->
                PodcastCard(
                    podcast = episodio,
                    radioViewModel = radioViewModel,
                    onClick = {
                        if (radioViewModel.currentTitle == episodio.titulo && radioViewModel.isPlaying) {
                            radioViewModel.player.pause()
                        } else {
                            // AQUÍ PASAMOS LOS 3 DATOS:
                            radioViewModel.reproducirAudio(
                                url = episodio.streamUrl,
                                titulo = episodio.titulo,
                                subtitulo = episodio.descripcion
                            )
                        }
                    }
                )
            }

            // 2. EL BOTÓN "CARGAR MÁS" (Solo aparece si hay más)
            if (radioViewModel.hayMasEpisodios && radioViewModel.listaEpisodios.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { radioViewModel.cargarMasEpisodios() },
                            enabled = !radioViewModel.cargandoMas // Desactiva si ya está cargando
                        ) {
                            if (radioViewModel.cargandoMas) {
                                Text("Cargando...")
                            } else {
                                Text("Cargar más episodios")
                            }
                        }
                    }
                }
            }
        }
    }
}