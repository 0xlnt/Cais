package com.example.myapplication.data.banco

/*
 * Uma classe por tabela do documento "Base do Banco de Dados (MVP)".
 * Os nomes dos campos seguem as colunas do SQL (em camelCase).
 * Quando o banco real ficar pronto, estas classes continuam valendo:
 * só muda quem preenche elas (o repositório).
 */

data class UsuarioRow(
    val id: Int,
    val nome: String,
    val email: String,
    val senhaHash: String,
    val telefone: String,
    /** "cliente" ou "vendedor" */
    val tipo: String,
    val criadoEm: Long
)

data class EnderecoRow(
    val id: Int,
    val usuarioId: Int,
    val rua: String,
    val numero: String,
    val bairro: String,
    val cidade: String,
    val complemento: String? = null
)

data class VendedorRow(
    val id: Int,
    val usuarioId: Int,
    val nomeLoja: String,
    val descricao: String,
    /** nome do arquivo da foto (ou URL, quando houver servidor) */
    val foto: String? = null,
    val avaliacaoMedia: Double = 0.0,
    /** fase futura (B2B) */
    val vendeParaEmpresas: Boolean = false
)

data class CategoriaRow(
    val id: Int,
    val nome: String
)

data class ProdutoRow(
    val id: Int,
    val vendedorId: Int,
    val categoriaId: Int,
    val nome: String,
    val descricao: String,
    val precoKg: Double,
    val foto: String? = null,
    /** data no formato "2026-11-13" */
    val dataPesca: String,
    val disponivel: Boolean = true,
    val ofertaDoDia: Boolean = false
)

data class CarrinhoItemRow(
    val id: Int,
    val usuarioId: Int,
    val produtoId: Int,
    val quantidadeKg: Double
)

data class PedidoRow(
    val id: Int,
    val clienteId: Int,
    val vendedorId: Int,
    /** nulo se for retirada */
    val enderecoId: Int?,
    /** "entrega" ou "retirada" */
    val tipoEntrega: String,
    /** simulada no MVP: "Pix", "Cartão" ou "Dinheiro" */
    val formaPagamento: String,
    val subtotal: Double,
    val frete: Double,
    val total: Double,
    /** um dos valores de [StatusPedido.ORDEM] */
    val status: String,
    val criadoEm: Long
)

data class PedidoItemRow(
    val id: Int,
    val pedidoId: Int,
    val produtoId: Int,
    val quantidadeKg: Double,
    /** preço no momento da compra */
    val precoUnitario: Double
)

data class AvaliacaoRow(
    val id: Int,
    val pedidoId: Int,
    /** 1 a 5 */
    val nota: Int,
    val comentario: String? = null
)

object TipoUsuario {
    const val CLIENTE = "cliente"
    const val VENDEDOR = "vendedor"
}

object TipoEntrega {
    const val ENTREGA = "entrega"
    const val RETIRADA = "retirada"
}

/** realizado → confirmado → preparacao → saiu_entrega → entregue */
object StatusPedido {
    const val REALIZADO = "realizado"
    const val CONFIRMADO = "confirmado"
    const val PREPARACAO = "preparacao"
    const val SAIU_ENTREGA = "saiu_entrega"
    const val ENTREGUE = "entregue"

    val ORDEM = listOf(REALIZADO, CONFIRMADO, PREPARACAO, SAIU_ENTREGA, ENTREGUE)

    fun proximo(atual: String): String? = ORDEM.getOrNull(ORDEM.indexOf(atual) + 1)
}
