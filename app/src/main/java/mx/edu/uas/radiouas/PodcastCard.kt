package mx.edu.uas.radiouas // Asegúrate que el paquete sea correcto

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import mx.edu.uas.radiouas.model.EmbyItem
import mx.edu.uas.radiouas.network.EmbyClient

@Composable
fun PodcastCard(
    item: EmbyItem,            // Recibe el objeto (Podcast/Album)
    onClick: (EmbyItem) -> Unit // Recibe la acción de clic
) {
    Column(
        modifier = Modifier
            .width(150.dp) // Ancho fijo para que se vea bien en el Grid
            .clickable { onClick(item) } // Al dar clic, avisamos al padre (PodcastsScreen)
            .padding(4.dp)
    ) {
        // Imagen (Portada)
        AsyncImage(
            model = EmbyClient.getImageUrl(item.id),
            contentDescription = item.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Cuadrada
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Título
        Text(
            text = item.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}