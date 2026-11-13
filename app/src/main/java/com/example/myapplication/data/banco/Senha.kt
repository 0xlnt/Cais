package com.example.myapplication.data.banco

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Senha nunca é guardada pura (coluna senha_hash). Formato guardado: "sal:hash".
 * SHA-256 com sal serve para o trabalho; num app de verdade o ideal é bcrypt/argon2.
 */
object Senha {
    private val aleatorio = SecureRandom()

    fun gerarHash(senha: String): String {
        val sal = ByteArray(8).also { aleatorio.nextBytes(it) }.toHex()
        return "$sal:${sha256(sal + senha)}"
    }

    fun confere(senha: String, hashGuardado: String): Boolean {
        val partes = hashGuardado.split(":")
        if (partes.size != 2) return false
        return sha256(partes[0] + senha) == partes[1]
    }

    private fun sha256(texto: String): String =
        MessageDigest.getInstance("SHA-256").digest(texto.toByteArray(Charsets.UTF_8)).toHex()

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
