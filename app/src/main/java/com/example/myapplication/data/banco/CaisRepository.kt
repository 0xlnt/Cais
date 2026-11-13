package com.example.myapplication.data.banco

/** Um item que o cliente quer comprar (vem do carrinho do celular). */
data class ItemDeCompra(val produtoId: Int, val quantidadeKg: Double)

/**
 * Contrato de acesso aos dados do Cais.
 *
 * O app só conversa com esta interface. Hoje quem responde é o [BancoFalso]
 * (dados em memória). Quando o banco real ficar pronto, basta criar outra classe
 * que implemente [CaisRepository] e trocar uma linha no CaisViewModel.
 *
 * Funções que podem falhar devolvem [Resultado] com a mensagem de erro.
 */
interface CaisRepository {

    // ---------- usuario ----------
    fun autenticar(email: String, senha: String): UsuarioRow?
    fun criarUsuario(
        nome: String,
        email: String,
        senha: String,
        telefone: String,
        tipo: String
    ): Resultado<UsuarioRow>
    fun excluirUsuario(usuarioId: Int)
    fun usuarioPorId(id: Int): UsuarioRow?

    // ---------- endereco ----------
    fun enderecosDoUsuario(usuarioId: Int): List<EnderecoRow>
    fun salvarEndereco(
        usuarioId: Int,
        rua: String,
        numero: String,
        bairro: String,
        cidade: String,
        complemento: String? = null
    ): EnderecoRow
    fun enderecoPorId(id: Int): EnderecoRow?

    // ---------- vendedor ----------
    fun listarVendedores(): List<VendedorRow>
    fun vendedorPorId(id: Int): VendedorRow?
    fun vendedorDoUsuario(usuarioId: Int): VendedorRow?
    fun criarVendedor(usuarioId: Int, nomeLoja: String, descricao: String): VendedorRow

    // ---------- categoria ----------
    fun listarCategorias(): List<CategoriaRow>

    // ---------- produto ----------
    fun listarProdutos(): List<ProdutoRow>
    fun produtoPorId(id: Int): ProdutoRow?
    fun produtosDoVendedor(vendedorId: Int): List<ProdutoRow>
    fun criarProduto(
        vendedorId: Int,
        categoriaId: Int,
        nome: String,
        descricao: String,
        precoKg: Double,
        dataPesca: String,
        disponivel: Boolean
    ): Resultado<ProdutoRow>
    fun mudarDisponibilidade(produtoId: Int, disponivel: Boolean)

    // ---------- carrinho_item (carrinho do usuário LOGADO) ----------
    fun carrinhoDoUsuario(usuarioId: Int): List<CarrinhoItemRow>

    /** Troca o carrinho salvo do usuário pelo carrinho atual do celular. */
    fun salvarCarrinho(usuarioId: Int, itens: List<ItemDeCompra>)

    // ---------- pedido / pedido_item ----------
    /**
     * Cria os pedidos a partir dos itens. Regra da equipe: um pedido por vendedor,
     * então itens de vendedores diferentes geram mais de um pedido.
     * O frete é cobrado em cada pedido. Limpa o carrinho salvo do usuário.
     */
    fun criarPedidos(
        clienteId: Int,
        itens: List<ItemDeCompra>,
        tipoEntrega: String,
        enderecoId: Int?,
        formaPagamento: String,
        frete: Double
    ): Resultado<List<PedidoRow>>
    fun listarPedidosRecentes(): List<PedidoRow>
    fun pedidosDoCliente(clienteId: Int): List<PedidoRow>
    fun pedidosDoVendedor(vendedorId: Int): List<PedidoRow>
    fun pedidoPorId(id: Int): PedidoRow?
    fun itensDoPedido(pedidoId: Int): List<PedidoItemRow>

    /** Passa o pedido para a próxima etapa. Devolve o pedido atualizado (ou null se já era o último). */
    fun avancarStatus(pedidoId: Int): PedidoRow?

    // ---------- avaliacao ----------
    fun avaliar(pedidoId: Int, nota: Int, comentario: String?): Resultado<AvaliacaoRow>
    fun avaliacaoDoPedido(pedidoId: Int): AvaliacaoRow?
}

/** Sucesso com valor ou erro com mensagem pronta para mostrar na tela. */
sealed class Resultado<out T> {
    data class Ok<T>(val valor: T) : Resultado<T>()
    data class Erro(val mensagem: String) : Resultado<Nothing>()

    /** Mensagem de erro, ou null se deu certo. */
    val erro: String? get() = (this as? Erro)?.mensagem
}
