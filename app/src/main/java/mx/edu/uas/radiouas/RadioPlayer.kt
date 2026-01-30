package mx.edu.uas.radiouas

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
@Composable
fun RadioPlayerScreen(radioViewModel: RadioViewModel) {

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
        Text(
            text = radioViewModel.currentSubtitle, // <--- USA LA VARIABLE
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
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
    }
}