package com.example.myapplication.data.banco

import java.io.File

/**
 * Onde o app guarda arquivos de texto entre uma abertura e outra.
 * O banco falso usa isso para não perder contas, pedidos e produtos quando o app fecha.
 */
interface Armazenamento {
    fun ler(nome: String): String?
    fun gravar(nome: String, texto: String)
    fun apagar(nome: String)
}

/** Guarda em arquivos dentro de uma pasta (no app: context.filesDir). */
class ArmazenamentoEmArquivo(private val pasta: File) : Armazenamento {

    private fun arquivo(nome: String) = File(pasta, nome)

    override fun ler(nome: String): String? = try {
        arquivo(nome).takeIf { it.exists() }?.readText(Charsets.UTF_8)
    } catch (e: Exception) {
        null
    }

    override fun gravar(nome: String, texto: String) {
        try {
            pasta.mkdirs()
            // grava num arquivo temporário e troca no fim, para um arquivo não ficar pela metade
            val temporario = File(pasta, "$nome.tmp")
            temporario.writeText(texto, Charsets.UTF_8)
            if (!temporario.renameTo(arquivo(nome))) {
                arquivo(nome).writeText(texto, Charsets.UTF_8)
                temporario.delete()
            }
        } catch (e: Exception) {
            // sem espaço ou sem permissão: o app segue funcionando só em memória
        }
    }

    override fun apagar(nome: String) {
        try {
            arquivo(nome).delete()
        } catch (e: Exception) {
        }
    }
}

/** Mesma ideia, mas só em memória. Serve para os testes. */
class ArmazenamentoEmMemoria : Armazenamento {
    private val arquivos = mutableMapOf<String, String>()
    override fun ler(nome: String): String? = arquivos[nome]
    override fun gravar(nome: String, texto: String) { arquivos[nome] = texto }
    override fun apagar(nome: String) { arquivos.remove(nome) }
}

// ---------- formato do arquivo do banco ----------
// Uma linha por registro: tabela<TAB>campo<TAB>campo...
// Tab, quebra de linha e barra são escritos como \t, \n e \\ ; valor nulo vira \N.

internal const val NULO = "\\N"

internal fun escapar(valor: Any?): String {
    if (valor == null) return NULO
    return valor.toString()
        .replace("\\", "\\\\")
        .replace("\t", "\\t")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
}

internal fun desescapar(texto: String): String? {
    if (texto == NULO) return null
    val sb = StringBuilder()
    var i = 0
    while (i < texto.length) {
        val c = texto[i]
        if (c == '\\' && i + 1 < texto.length) {
            when (val proximo = texto[i + 1]) {
                't' -> sb.append('\t')
                'n' -> sb.append('\n')
                'r' -> sb.append('\r')
                else -> sb.append(proximo)
            }
            i += 2
        } else {
            sb.append(c)
            i++
        }
    }
    return sb.toString()
}

internal fun linhaDoArquivo(tabela: String, vararg campos: Any?): String =
    (listOf(tabela) + campos.map { escapar(it) }).joinToString("\t")

/** Campos de uma linha lida do arquivo, já sem o nome da tabela. */
internal class CamposLidos(private val campos: List<String?>) {
    fun texto(i: Int): String = campos[i] ?: ""
    fun textoOuNulo(i: Int): String? = campos[i]
    fun int(i: Int): Int = campos[i]!!.toInt()
    fun intOuNulo(i: Int): Int? = campos[i]?.toInt()
    fun long(i: Int): Long = campos[i]!!.toLong()
    fun double(i: Int): Double = campos[i]!!.toDouble()
    fun booleano(i: Int): Boolean = campos[i]!!.toBoolean()
}
