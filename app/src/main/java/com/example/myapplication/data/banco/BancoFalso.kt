package com.example.myapplication.data.banco

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * "Banco de dados falso": cada tabela é uma lista em memória.
 * Fechou o app, os dados voltam ao início (iguais ao de [DadosIniciais]).
 */
class BancoFalso(comDadosIniciais: Boolean = true) : CaisRepository {

    private val usuarios = mutableListOf<UsuarioRow>()
    private val enderecos = mutableListOf<EnderecoRow>()
    private val vendedores = mutableListOf<VendedorRow>()
    private val categorias = mutableListOf<CategoriaRow>()
    private val produtos = mutableListOf<ProdutoRow>()
    private val carrinhoItens = mutableListOf<CarrinhoItemRow>()
    private val pedidos = mutableListOf<PedidoRow>()
    private val pedidoItens = mutableListOf<PedidoItemRow>()
    private val avaliacoes = mutableListOf<AvaliacaoRow>()

    init {
        if (comDadosIniciais) DadosIniciais.preencher(this)
    }

    // ---------- usado só pelos dados iniciais ----------
    internal fun inserirUsuario(u: UsuarioRow) { usuarios.add(u) }
    internal fun inserirEndereco(e: EnderecoRow) { enderecos.add(e) }
    internal fun inserirVendedor(v: VendedorRow) { vendedores.add(v) }
    internal fun inserirCategoria(c: CategoriaRow) { categorias.add(c) }
    internal fun inserirProduto(p: ProdutoRow) { produtos.add(p) }
    internal fun inserirPedido(p: PedidoRow, itens: List<PedidoItemRow>) {
        pedidos.add(p)
        pedidoItens.addAll(itens)
    }

    // ---------- usuario ----------
    override fun autenticar(email: String, senha: String): UsuarioRow? =
        usuarios.firstOrNull {
            it.email.equals(email.trim(), ignoreCase = true) && Senha.confere(senha, it.senhaHash)
        }

    override fun criarUsuario(
        nome: String,
        email: String,
        senha: String,
        telefone: String,
        tipo: String
    ): Resultado<UsuarioRow> {
        if (usuarios.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return Resultado.Erro("Esse e-mail já tem conta.")
        }
        val novo = UsuarioRow(
            id = (usuarios.maxOfOrNull { it.id } ?: 0) + 1,
            nome = nome.trim(),
            email = email.trim(),
            senhaHash = Senha.gerarHash(senha),
            telefone = telefone.trim(),
            tipo = tipo,
            criadoEm = System.currentTimeMillis()
        )
        usuarios.add(novo)
        return Resultado.Ok(novo)
    }

    override fun usuarioPorId(id: Int): UsuarioRow? = usuarios.firstOrNull { it.id == id }

    override fun excluirUsuario(usuarioId: Int) {
        usuarios.removeAll { it.id == usuarioId }
        enderecos.removeAll { it.usuarioId == usuarioId }
        carrinhoItens.removeAll { it.usuarioId == usuarioId }
        // pedidos antigos ficam guardados (histórico do vendedor)
    }

    // ---------- endereco ----------
    override fun enderecosDoUsuario(usuarioId: Int): List<EnderecoRow> =
        enderecos.filter { it.usuarioId == usuarioId }

    override fun salvarEndereco(
        usuarioId: Int,
        rua: String,
        numero: String,
        bairro: String,
        cidade: String,
        complemento: String?
    ): EnderecoRow {
        val novo = EnderecoRow(
            id = (enderecos.maxOfOrNull { it.id } ?: 0) + 1,
            usuarioId = usuarioId,
            rua = rua.trim(),
            numero = numero.trim(),
            bairro = bairro.trim(),
            cidade = cidade.trim(),
            complemento = complemento?.trim()?.ifEmpty { null }
        )
        enderecos.add(novo)
        return novo
    }

    override fun enderecoPorId(id: Int): EnderecoRow? = enderecos.firstOrNull { it.id == id }

    // ---------- vendedor ----------
    override fun listarVendedores(): List<VendedorRow> = vendedores.toList()

    override fun vendedorPorId(id: Int): VendedorRow? = vendedores.firstOrNull { it.id == id }

    override fun vendedorDoUsuario(usuarioId: Int): VendedorRow? =
        vendedores.firstOrNull { it.usuarioId == usuarioId }

    override fun criarVendedor(usuarioId: Int, nomeLoja: String, descricao: String): VendedorRow {
        val novo = VendedorRow(
            id = (vendedores.maxOfOrNull { it.id } ?: 0) + 1,
            usuarioId = usuarioId,
            nomeLoja = nomeLoja.trim(),
            descricao = descricao.trim()
        )
        vendedores.add(novo)
        return novo
    }

    // ---------- categoria ----------
    override fun listarCategorias(): List<CategoriaRow> = categorias.toList()

    // ---------- produto ----------
    override fun listarProdutos(): List<ProdutoRow> = produtos.toList()

    override fun produtoPorId(id: Int): ProdutoRow? = produtos.firstOrNull { it.id == id }

    override fun produtosDoVendedor(vendedorId: Int): List<ProdutoRow> =
        produtos.filter { it.vendedorId == vendedorId }

    override fun criarProduto(
        vendedorId: Int,
        categoriaId: Int,
        nome: String,
        descricao: String,
        precoKg: Double,
        dataPesca: String,
        disponivel: Boolean
    ): Resultado<ProdutoRow> {
        if (nome.isBlank()) return Resultado.Erro("Digite o nome do produto.")
        if (precoKg <= 0.0) return Resultado.Erro("Digite um preço válido (ex.: 38,90).")
        if (vendedorPorId(vendedorId) == null) return Resultado.Erro("Vendedor não encontrado.")
        if (categorias.none { it.id == categoriaId }) return Resultado.Erro("Categoria não encontrada.")
        val novo = ProdutoRow(
            id = (produtos.maxOfOrNull { it.id } ?: 0) + 1,
            vendedorId = vendedorId,
            categoriaId = categoriaId,
            nome = nome.trim(),
            descricao = descricao.trim(),
            precoKg = precoKg,
            dataPesca = dataPesca,
            disponivel = disponivel
        )
        produtos.add(novo)
        return Resultado.Ok(novo)
    }

    override fun mudarDisponibilidade(produtoId: Int, disponivel: Boolean) {
        val i = produtos.indexOfFirst { it.id == produtoId }
        if (i >= 0) produtos[i] = produtos[i].copy(disponivel = disponivel)
    }

    // ---------- carrinho_item ----------
    override fun carrinhoDoUsuario(usuarioId: Int): List<CarrinhoItemRow> =
        carrinhoItens.filter { it.usuarioId == usuarioId }

    override fun salvarCarrinho(usuarioId: Int, itens: List<ItemDeCompra>) {
        carrinhoItens.removeAll { it.usuarioId == usuarioId }
        var id = carrinhoItens.maxOfOrNull { it.id } ?: 0
        itens.forEach {
            id += 1
            carrinhoItens.add(CarrinhoItemRow(id, usuarioId, it.produtoId, it.quantidadeKg))
        }
    }

    // ---------- pedido / pedido_item ----------
    override fun criarPedidos(
        clienteId: Int,
        itens: List<ItemDeCompra>,
        tipoEntrega: String,
        enderecoId: Int?,
        formaPagamento: String,
        frete: Double
    ): Resultado<List<PedidoRow>> {
        if (itens.isEmpty()) return Resultado.Erro("Seu carrinho está vazio.")
        if (usuarios.none { it.id == clienteId }) return Resultado.Erro("Entre na sua conta.")
        if (tipoEntrega == TipoEntrega.ENTREGA && (enderecoId == null || enderecoPorId(enderecoId) == null)) {
            return Resultado.Erro("Escolha o endereço de entrega.")
        }

        // acha o produto de cada item e confere se ainda está à venda
        val comProduto = itens.map { item ->
            val produto = produtoPorId(item.produtoId)
                ?: return Resultado.Erro("Um produto do carrinho não existe mais.")
            if (!produto.disponivel) return Resultado.Erro("${produto.nome} não está mais disponível.")
            item to produto
        }

        // um pedido por vendedor
        val criados = comProduto.groupBy { (_, produto) -> produto.vendedorId }.map { (vendedorId, lista) ->
            val subtotal = lista.sumOf { (item, produto) -> produto.precoKg * item.quantidadeKg }
            val taxa = if (tipoEntrega == TipoEntrega.ENTREGA) frete else 0.0
            val pedido = PedidoRow(
                id = (pedidos.maxOfOrNull { it.id } ?: 1000) + 1,
                clienteId = clienteId,
                vendedorId = vendedorId,
                enderecoId = if (tipoEntrega == TipoEntrega.ENTREGA) enderecoId else null,
                tipoEntrega = tipoEntrega,
                formaPagamento = formaPagamento,
                subtotal = subtotal,
                frete = taxa,
                total = subtotal + taxa,
                status = StatusPedido.REALIZADO,
                criadoEm = System.currentTimeMillis()
            )
            pedidos.add(0, pedido)
            var idItem = pedidoItens.maxOfOrNull { it.id } ?: 0
            lista.forEach { (item, produto) ->
                idItem += 1
                pedidoItens.add(
                    PedidoItemRow(idItem, pedido.id, produto.id, item.quantidadeKg, produto.precoKg)
                )
            }
            pedido
        }

        carrinhoItens.removeAll { it.usuarioId == clienteId }
        return Resultado.Ok(criados)
    }

    override fun listarPedidosRecentes(): List<PedidoRow> = pedidos.sortedByDescending { it.id }

    override fun pedidosDoCliente(clienteId: Int): List<PedidoRow> =
        pedidos.filter { it.clienteId == clienteId }

    override fun pedidosDoVendedor(vendedorId: Int): List<PedidoRow> =
        pedidos.filter { it.vendedorId == vendedorId }

    override fun pedidoPorId(id: Int): PedidoRow? = pedidos.firstOrNull { it.id == id }

    override fun itensDoPedido(pedidoId: Int): List<PedidoItemRow> =
        pedidoItens.filter { it.pedidoId == pedidoId }

    override fun avancarStatus(pedidoId: Int): PedidoRow? {
        val i = pedidos.indexOfFirst { it.id == pedidoId }
        if (i < 0) return null
        val proximo = StatusPedido.proximo(pedidos[i].status) ?: return null
        pedidos[i] = pedidos[i].copy(status = proximo)
        return pedidos[i]
    }

    // ---------- avaliacao ----------
    override fun avaliar(pedidoId: Int, nota: Int, comentario: String?): Resultado<AvaliacaoRow> {
        val pedido = pedidoPorId(pedidoId) ?: return Resultado.Erro("Pedido não encontrado.")
        if (pedido.status != StatusPedido.ENTREGUE) return Resultado.Erro("Só dá para avaliar pedido entregue.")
        if (nota !in 1..5) return Resultado.Erro("A nota vai de 1 a 5.")
        if (avaliacaoDoPedido(pedidoId) != null) return Resultado.Erro("Esse pedido já foi avaliado.")

        val nova = AvaliacaoRow(
            id = (avaliacoes.maxOfOrNull { it.id } ?: 0) + 1,
            pedidoId = pedidoId,
            nota = nota,
            comentario = comentario?.trim()?.ifEmpty { null }
        )
        avaliacoes.add(nova)
        recalcularMediaDoVendedor(pedido.vendedorId)
        return Resultado.Ok(nova)
    }

    override fun avaliacaoDoPedido(pedidoId: Int): AvaliacaoRow? =
        avaliacoes.firstOrNull { it.pedidoId == pedidoId }

    private fun recalcularMediaDoVendedor(vendedorId: Int) {
        val ids = pedidos.filter { it.vendedorId == vendedorId }.map { it.id }.toSet()
        val notas = avaliacoes.filter { it.pedidoId in ids }.map { it.nota }
        val i = vendedores.indexOfFirst { it.id == vendedorId }
        if (i >= 0 && notas.isNotEmpty()) {
            vendedores[i] = vendedores[i].copy(avaliacaoMedia = notas.average())
        }
    }
}

/** Banco único do app (os dados ficam vivos enquanto o app estiver aberto). */
object CaisBanco {
    val repositorio: CaisRepository by lazy { BancoFalso() }
}

/** "2026-11-13" com [diasAtras] dias antes de hoje. */
fun dataIso(diasAtras: Int = 0): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -diasAtras)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}
