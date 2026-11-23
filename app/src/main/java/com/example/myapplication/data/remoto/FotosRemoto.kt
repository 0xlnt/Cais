package com.example.myapplication.data.remoto

import io.github.jan.supabase.storage.storage

/**
 * Envia a foto do produto para o Supabase Storage (bucket público "produtos").
 * Cada vendedor grava só na própria pasta (nome da pasta = id do vendedor).
 */
object FotosRemoto {
    private const val BUCKET = "produtos"

    /** Devolve a URL pública da foto enviada. */
    suspend fun enviar(vendedorId: String, jpeg: ByteArray): String {
        val caminho = "$vendedorId/${System.currentTimeMillis()}.jpg"
        val bucket = SupabaseCliente.cliente.storage.from(BUCKET)
        bucket.upload(caminho, jpeg)
        return bucket.publicUrl(caminho)
    }
}
