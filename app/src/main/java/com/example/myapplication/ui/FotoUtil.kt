package com.example.myapplication.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlin.math.max

/** Ajudas para a foto do produto: ler do celular, endireitar, reduzir e virar JPEG. */
object FotoUtil {

    /**
     * Lê a imagem do [uri] (câmera ou galeria), corrige a rotação da câmera e reduz
     * para que o lado maior tenha no máximo [ladoMax] pixels. Devolve null se não der para ler.
     */
    fun carregar(contexto: Context, uri: Uri, ladoMax: Int): Bitmap? {
        val resolver = contexto.contentResolver
        return try {
            val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
            if (limites.outWidth <= 0 || limites.outHeight <= 0) return null

            // lê já em tamanho menor (potências de 2) para não estourar a memória
            var amostra = 1
            while (max(limites.outWidth, limites.outHeight) / (amostra * 2) >= ladoMax) amostra *= 2
            val opcoes = BitmapFactory.Options().apply { inSampleSize = amostra }
            val original = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, opcoes)
            } ?: return null

            val rotacao = resolver.openInputStream(uri)?.use { lerRotacao(it) } ?: 0
            val escala = ladoMax.toFloat() / max(original.width, original.height)
            val matriz = Matrix().apply {
                if (escala < 1f) postScale(escala, escala)
                if (rotacao != 0) postRotate(rotacao.toFloat())
            }
            Bitmap.createBitmap(original, 0, 0, original.width, original.height, matriz, true)
        } catch (e: Exception) {
            android.util.Log.e("CAIS", "não consegui ler a foto", e)
            null
        }
    }

    /** Foto pronta para enviar: no máximo 1280 px e JPEG com qualidade 85. */
    fun jpeg(contexto: Context, uri: Uri): ByteArray? {
        val bitmap = carregar(contexto, uri, 1280) ?: return null
        val saida = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, saida)
        return saida.toByteArray()
    }

    private fun lerRotacao(entrada: InputStream): Int =
        try {
            when (ExifInterface(entrada).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
}
