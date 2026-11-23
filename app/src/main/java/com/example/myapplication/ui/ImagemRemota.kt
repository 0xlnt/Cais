package com.example.myapplication.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import kotlin.math.max

/** Guarda as últimas fotos baixadas, para a lista não baixar de novo a cada rolagem. */
private val cacheDeFotos = LruCache<String, ImageBitmap>(12)

private const val LADO_MAX_DA_LISTA = 800

/**
 * Mostra uma foto que está na internet (URL). Enquanto baixa, não desenha nada:
 * quem chama deixa o fundo/emoji da categoria por baixo.
 */
@Composable
fun ImagemRemota(url: String, modifier: Modifier = Modifier) {
    val imagem by produceState<ImageBitmap?>(initialValue = cacheDeFotos.get(url), url) {
        if (value == null) {
            val baixada = withContext(Dispatchers.IO) { baixar(url) }
            if (baixada != null) {
                val pronta = baixada.asImageBitmap()
                cacheDeFotos.put(url, pronta)
                value = pronta
            }
        }
    }
    imagem?.let {
        Image(bitmap = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier)
    }
}

/** Mostra uma foto que está no celular (a que o vendedor acabou de escolher ou tirar). */
@Composable
fun ImagemLocal(uri: Uri, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val imagem by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) { FotoUtil.carregar(contexto, uri, LADO_MAX_DA_LISTA)?.asImageBitmap() }
    }
    imagem?.let {
        Image(bitmap = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier)
    }
}

private fun baixar(url: String): Bitmap? =
    try {
        val conexao = URL(url).openConnection().apply {
            connectTimeout = 10_000
            readTimeout = 15_000
        }
        val original = conexao.getInputStream().use { BitmapFactory.decodeStream(it) }
        if (original != null && max(original.width, original.height) > LADO_MAX_DA_LISTA) {
            val escala = LADO_MAX_DA_LISTA.toFloat() / max(original.width, original.height)
            Bitmap.createScaledBitmap(
                original,
                (original.width * escala).toInt(),
                (original.height * escala).toInt(),
                true
            )
        } else {
            original
        }
    } catch (e: Exception) {
        android.util.Log.e("CAIS", "não consegui baixar a foto", e)
        null
    }
