package mx.edu.uas.radiouas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// Quitamos @OptIn(UnstableApi::class) porque ya no manipulamos el ExoPlayer directo aquí

@Composable
fun RadioPlayerScreen(radioViewModel: RadioViewModel) {
    // ELIMINAMOS: val context, val streamUrl, val exoPlayer...
    // ELIMINAMOS: var isPlaying, var isLoading (locales)
    // ELIMINAMOS: DisposableEffect

    // Todo lo leemos directamente del ViewModel

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. TÍTULO SINCRONIZADO
        // Ahora lee la variable global. Si cambia el podcast o la API, cambia aquí.
        Text(
            text = radioViewModel.currentTitle ?: "Radio UAS",
            style = MaterialTheme.typography.headlineMedium,
            color = AzulUAS,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 2. ESTADO DE CARGA GLOBAL
        if (radioViewModel.isLoading) {
            CircularProgressIndicator(
                color = AzulUAS,
                modifier = Modifier.size(50.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Cargando audio...")
        } else {
            // 3. CONTROLES CONECTADOS AL VIEWMODEL
            Button(
                onClick = {
                    // Usamos la función inteligente que ya creamos
                    radioViewModel.playRadioOAlternar()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AzulUAS),
                modifier = Modifier.size(width = 200.dp, height = 50.dp)
            ) {
                // El texto del botón reacciona al estado real del audio
                Text(if (radioViewModel.isPlaying) "PAUSAR" else "REPRODUCIR")
            }
        }

        // Opcional: Si quieres un botón específico para volver a la Radio en Vivo
        // si estás escuchando un podcast:
        /*
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = {
             radioViewModel.reproducirAudio(radioViewModel.streamURL, radioViewModel.currentTitle ?: "Radio UAS")
        }) {
            Text("Volver a la señal en vivo", color = AzulUAS)
        }
        */
    }
}