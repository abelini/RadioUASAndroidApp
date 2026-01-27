package mx.edu.uas.radiouas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage


@Composable
fun NoticiasScreen() {
    var noticias by remember { mutableStateOf<List<WPPost>>(emptyList()) }
    var noticiaSeleccionada by remember { mutableStateOf<WPPost?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Si el usuario seleccionó una noticia, mostramos el detalle nativo
    if (noticiaSeleccionada != null) {
        DetalleNoticiaScreen(noticia = noticiaSeleccionada!!) {
            noticiaSeleccionada = null // Al dar clic en volver, regresamos a la lista
        }
    } else {
        // Pantalla Principal: Lista de Noticias
        LaunchedEffect(Unit) {
            try {
                noticias = RetrofitInstance.api.getUltimasNoticias()
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(noticias.size) { index ->
                        val noticia = noticias[index]
                        NoticiaCard(
                            titulo = noticia.title.rendered,
                            imagenUrl = noticia.jetpack_featured_media_url ?: "",
                            onClick = { noticiaSeleccionada = noticia }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoticiaCard(titulo: String, imagenUrl: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            if (imagenUrl.isNotEmpty()) {
                AsyncImage(
                    model = imagenUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = HtmlCompat.fromHtml(titulo, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun DetalleNoticiaScreen(noticia: WPPost, onVolver: () -> Unit) {
    // Usamos ScrollState para que el usuario pueda bajar a leer toda la nota
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Imagen de Cabecera
        if (noticia.jetpack_featured_media_url != null) {
            AsyncImage(
                model = noticia.jetpack_featured_media_url,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Título Nativo
            Text(
                text = HtmlCompat.fromHtml(noticia.title.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cuerpo de la noticia (Renderizado de HTML a Texto Nativo)
            Text(
                text = HtmlCompat.fromHtml(noticia.content.rendered, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2 // Un poco más de espacio entre líneas
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón para cerrar la nota
            Button(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Volver a la lista")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}