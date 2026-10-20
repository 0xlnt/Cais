package com.example.myapplication.data

import java.util.Locale

private val ptBR: Locale = Locale.forLanguageTag("pt-BR")

/** 38.9 -> "R$ 38,90" */
fun Double.reais(): String = "R$ " + String.format(ptBR, "%.2f", this)

/** 1.5 -> "1,5 kg" */
fun Double.emKg(): String = String.format(ptBR, "%.1f", this) + " kg"

/** 4.97 -> "4,97"; vendedor sem avaliação -> "Novo" */
fun Double.notaTexto(): String = if (this > 0) String.format(ptBR, "%.2f", this) else "Novo"

enum class Categoria(val rotulo: String, val emoji: String) {
    PEIXE("Peixes", "🐟"),
    CAMARAO("Camarão", "🦐"),
    FRUTOS_DO_MAR("Frutos do Mar", "🦀")
}

/** Chips de filtro rápido (Home e Busca). */
enum class FiltroRapido(val rotulo: String, val categoria: Categoria?) {
    TODOS("Todos", null),
    PEIXE("Peixes", Categoria.PEIXE),
    CAMARAO("Camarão", Categoria.CAMARAO),
    FRUTOS("Frutos do Mar", Categoria.FRUTOS_DO_MAR),
    PROMOCOES("Promoções", null)
}

data class Vendedor(
    val id: Int,
    val nome: String,
    val descricao: String,
    val bairro: String,
    val nota: Double
)

data class Produto(
    val id: Int,
    val nome: String,
    val tipo: String,
    val categoria: Categoria,
    val preco: Double,
    val nota: Double,
    val vendedorId: Int,
    val dataPesca: String,
    val disponivel: Boolean = true,
    val promocao: Boolean = false,
    /** id do drawable (R.drawable.xxx); null = usa o emoji de reserva */
    val foto: Int? = null
)

data class ItemCarrinho(val produto: Produto, val kg: Double) {
    val subtotal: Double get() = produto.preco * kg
}

data class Zona(val nome: String, val taxa: Double, val pedidoMinimo: Double) {
    val rotulo: String get() = "$nome – ${taxa.reais()}"
}

data class Usuario(
    val nome: String,
    val doc: String,
    val email: String,
    val senha: String,
    /** null = cliente; preenchido = vendedor (id do Vendedor) */
    val vendedorId: Int? = null
) {
    val ehVendedor: Boolean get() = vendedorId != null
}

/** status: 0 Recebido, 1 Em preparo, 2 Saiu para entrega / Pronto para retirada, 3 Entregue */
data class Pedido(
    val id: Int,
    val clienteEmail: String,
    val clienteNome: String,
    val vendedorId: Int,
    val itens: List<ItemCarrinho>,
    val entrega: Boolean,
    val destino: String,
    val taxa: Double,
    val pagamento: String,
    val status: Int = 0
) {
    val subtotal: Double get() = itens.sumOf { it.subtotal }
    val total: Double get() = subtotal + taxa
    val passos: List<String> get() = passosDoPedido(entrega)
    val resumo: String get() = itens.joinToString(" + ") { "${it.produto.nome} ${it.kg.emKg()}" }
}

fun passosDoPedido(entrega: Boolean): List<String> = listOf(
    "Recebido",
    "Em preparo",
    if (entrega) "Saiu para entrega" else "Pronto para retirada",
    "Entregue"
)
