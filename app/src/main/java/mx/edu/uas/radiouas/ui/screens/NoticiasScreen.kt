package mx.edu.uas.radiouas.ui.screens

import android.text.Html
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import coil.compose.AsyncImage
import mx.edu.uas.radiouas.model.WPPost
import mx.edu.uas.radiouas.network.WordPressClient
import mx.edu.uas.radiouas.utils.AppConfig

@Composable
fun NoticiasScreen() {
    // ESTADO
    var noticias by remember { mutableStateOf<List<WPPost>>(emptyList()) }
    var noticiaSeleccionada by remember { mutableStateOf<WPPost?>(null) } // Controla si vemos lista o detalle
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // CARGA DE DATOS
    LaunchedEffect(Unit) {
        try {
            if (noticias.isEmpty()) { // Evita recargar si ya tenemos datos al volver
                noticias = WordPressClient.api.getUltimasNoticias()
            }
            isLoading = false
        } catch (e: Exception) {
            errorMsg = "No se pudieron cargar las noticias."
            isLoading = false
        }
    }

    // NAVEGACIÓN SIMPLE (Tu lógica + Mi diseño)
    if (noticiaSeleccionada != null) {
        // MODO LECTURA (Detalle)
        DetalleNoticiaScreen(
            noticia = noticiaSeleccionada!!,
            onVolver = { noticiaSeleccionada = null } // Al volver, limpiamos la selección
        )
    } else {
        // MODO LISTA (Menú Principal)
        ListaNoticiasView(
            noticias = noticias,
            isLoading = isLoading,
            errorMsg = errorMsg,
            onNoticiaClick = { post -> noticiaSeleccionada = post }
        )
    }
}

// --- VISTA 1: LA LISTA DE NOTICIAS ---
@Composable
fun ListaNoticiasView(
    noticias: List<WPPost>,
    isLoading: Boolean,
    errorMsg: String?,
    onNoticiaClick: (WPPost) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = AppConfig.WP_GENERAL_TITLE,
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF002D56),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF002D56))
            }
        } else if (errorMsg != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = errorMsg, color = Color.Red)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(noticias) { post ->
                    NoticiaItemCard(post = post, onClick = { onNoticiaClick(post) })
                }
            }
        }
    }
}

@Composable
fun NoticiaItemCard(post: WPPost, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column {
            // Imagen
            if (!post.featuredMediaUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = post.featuredMediaUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.LightGray)
                )
            }

            // Contenido Texto
            Column(modifier = Modifier.padding(16.dp)) {
                // TÍTULO
                Text(
                    text = parseHtml(post.title.rendered),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // RESUMEN GENERADO (Primeras 25 palabras del contenido)
                Text(
                    text = generarResumen(post.content.rendered), // <--- AQUÍ ESTÁ LA MAGIA
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- VISTA 2: EL DETALLE (MODO LECTURA) ---
@Composable
fun DetalleNoticiaScreen(noticia: WPPost, onVolver: () -> Unit) {
    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // CONTENIDO CON SCROLL
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Imagen Gigante
            if (!noticia.featuredMediaUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = noticia.featuredMediaUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), // Imagen grande impactante
                    contentScale = ContentScale.Crop
                )
            } else {
                Spacer(modifier = Modifier.height(60.dp)) // Espacio para la flecha si no hay imagen
            }

            // Cuerpo del texto
            Column(modifier = Modifier.padding(24.dp)) {
                // Título
                Text(
                    text = parseHtml(noticia.title.rendered),
                    style = MaterialTheme.typography.headlineSmall, // Fuente Grande
                    color = Color(0xFF002D56),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                // Contenido HTML parseado
                Text(
                    text = parseHtml(noticia.content.rendered),
                    style = MaterialTheme.typography.bodyLarge, // Fuente legible para lectura
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3, // Buen espaciado
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // FLECHA DE REGRESAR (FLOTANTE)
        IconButton(
            onClick = onVolver,
            modifier = Modifier
                .padding(top = 40.dp, start = 16.dp) // Top 40dp para evitar la barra de estado
                .align(Alignment.TopStart)
                .size(48.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f), // Círculo semitransparente oscuro
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Regresar",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// --- UTILERÍA ---
// Función limpia para quitar etiquetas HTML
fun parseHtml(html: String): String {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
}

// Función para crear un extracto a partir del contenido completo HTML
fun generarResumen(htmlContent: String): String {
    // 1. Convertimos el HTML a texto plano usando tu función existente
    val textoPlano = parseHtml(htmlContent)

    // 2. Dividimos por espacios para obtener las palabras
    val palabras = textoPlano.split("\\s+".toRegex())

    // 3. Tomamos las primeras 25 palabras y agregamos "..."
    return if (palabras.size > 25) {
        palabras.take(25).joinToString(" ") + "..."
    } else {
        textoPlano
    }
}