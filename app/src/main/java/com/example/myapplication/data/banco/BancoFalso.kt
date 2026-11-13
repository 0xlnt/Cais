package com.example.myapplication.data.banco

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * "Banco de dados falso": cada tabela é uma lista em memória.
 * Fechou o app, os dados voltam ao início (iguais ao de [DadosIniciais]).
 */
class BancoFalso(
    comDadosIniciais: Boolean = true,
    /** Se tiver, o banco lê daqui ao abrir e grava aqui a cada mudança. */
    private val armazenamento: Armazenamento? = null
) : CaisRepository {

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
        val salvo = armazenamento?.ler(ARQUIVO_DO_BANCO)
        val carregou = salvo != null && try {
            importar(salvo)
            true
        } catch (e: Exception) {
            limparTudo() // arquivo estragado: começa de novo
            false
        }
        if (!carregou && comDadosIniciais) {
            DadosIniciais.preencher(this)
            salvar()
        }
    }

    // ---------- guardar e ler do arquivo ----------
    private fun salvar() {
        armazenamento?.gravar(ARQUIVO_DO_BANCO, exportar())
    }

    private fun limparTudo() {
        usuarios.clear(); enderecos.clear(); vendedores.clear(); categorias.clear()
        produtos.clear(); carrinhoItens.clear(); pedidos.clear(); pedidoItens.clear(); avaliacoes.clear()
    }

    private fun exportar(): String = buildString {
        append("cais-banco v1\n")
        usuarios.forEach {
            append(linhaDoArquivo("usuario", it.id, it.nome, it.email, it.senhaHash, it.telefone, it.tipo, it.criadoEm)).append('\n')
        }
        enderecos.forEach {
            append(linhaDoArquivo("endereco", it.id, it.usuarioId, it.rua, it.numero, it.bairro, it.cidade, it.complemento)).append('\n')
        }
        vendedores.forEach {
            append(linhaDoArquivo("vendedor", it.id, it.usuarioId, it.nomeLoja, it.descricao, it.foto, it.avaliacaoMedia, it.vendeParaEmpresas)).append('\n')
        }
        categorias.forEach { append(linhaDoArquivo("categoria", it.id, it.nome)).append('\n') }
        produtos.forEach {
            append(linhaDoArquivo("produto", it.id, it.vendedorId, it.categoriaId, it.nome, it.descricao, it.precoKg, it.foto, it.dataPesca, it.disponivel, it.ofertaDoDia)).append('\n')
        }
        carrinhoItens.forEach {
            append(linhaDoArquivo("carrinho_item", it.id, it.usuarioId, it.produtoId, it.quantidadeKg)).append('\n')
        }
        pedidos.forEach {
            append(linhaDoArquivo("pedido", it.id, it.clienteId, it.vendedorId, it.enderecoId, it.tipoEntrega, it.formaPagamento, it.subtotal, it.frete, it.total, it.status, it.criadoEm)).append('\n')
        }
        pedidoItens.forEach {
            append(linhaDoArquivo("pedido_item", it.id, it.pedidoId, it.produtoId, it.quantidadeKg, it.precoUnitario)).append('\n')
        }
        avaliacoes.forEach {
            append(linhaDoArquivo("avaliacao", it.id, it.pedidoId, it.nota, it.comentario)).append('\n')
        }
    }

    private fun importar(texto: String) {
        limparTudo()
        val linhas = texto.split("\n").filter { it.isNotEmpty() }
        require(linhas.firstOrNull() == "cais-banco v1") { "formato desconhecido" }
        for (linha in linhas.drop(1)) {
            val partes = linha.split("\t")
            val c = CamposLidos(partes.drop(1).map { desescapar(it) })
            when (partes[0]) {
                "usuario" -> usuarios.add(UsuarioRow(c.int(0), c.texto(1), c.texto(2), c.texto(3), c.texto(4), c.texto(5), c.long(6)))
                "endereco" -> enderecos.add(EnderecoRow(c.int(0), c.int(1), c.texto(2), c.texto(3), c.texto(4), c.texto(5), c.textoOuNulo(6)))
                "vendedor" -> vendedores.add(VendedorRow(c.int(0), c.int(1), c.texto(2), c.texto(3), c.textoOuNulo(4), c.double(5), c.booleano(6)))
                "categoria" -> categorias.add(CategoriaRow(c.int(0), c.texto(1)))
                "produto" -> produtos.add(ProdutoRow(c.int(0), c.int(1), c.int(2), c.texto(3), c.texto(4), c.double(5), c.textoOuNulo(6), c.texto(7), c.booleano(8), c.booleano(9)))
                "carrinho_item" -> carrinhoItens.add(CarrinhoItemRow(c.int(0), c.int(1), c.int(2), c.double(3)))
                "pedido" -> pedidos.add(PedidoRow(c.int(0), c.int(1), c.int(2), c.intOuNulo(3), c.texto(4), c.texto(5), c.double(6), c.double(7), c.double(8), c.texto(9), c.long(10)))
                "pedido_item" -> pedidoItens.add(PedidoItemRow(c.int(0), c.int(1), c.int(2), c.double(3), c.double(4)))
                "avaliacao" -> avaliacoes.add(AvaliacaoRow(c.int(0), c.int(1), c.int(2), c.textoOuNulo(3)))
                else -> error("tabela desconhecida: ${partes[0]}")
            }
        }
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
        salvar()
        return Resultado.Ok(novo)
    }

    override fun usuarioPorId(id: Int): UsuarioRow? = usuarios.firstOrNull { it.id == id }

    override fun excluirUsuario(usuarioId: Int) {
        usuarios.removeAll { it.id == usuarioId }
        enderecos.removeAll { it.usuarioId == usuarioId }
        carrinhoItens.removeAll { it.usuarioId == usuarioId }
        // pedidos antigos ficam guardados (histórico do vendedor)
        salvar()
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
        salvar()
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
        salvar()
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
        salvar()
        return Resultado.Ok(novo)
    }

    override fun mudarDisponibilidade(produtoId: Int, disponivel: Boolean) {
        val i = produtos.indexOfFirst { it.id == produtoId }
        if (i >= 0) {
            produtos[i] = produtos[i].copy(disponivel = disponivel)
            salvar()
        }
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
        salvar()
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
        salvar()
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
        salvar()
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
        salvar()
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

private const val ARQUIVO_DO_BANCO = "banco.txt"

/** Banco único do app. Chame [iniciar] uma vez, na MainActivity, para os dados ficarem guardados no celular. */
object CaisBanco {
    var armazenamento: Armazenamento? = null
        private set
    private var banco: CaisRepository? = null

    fun iniciar(armazenamento: Armazenamento) {
        if (banco != null) return
        this.armazenamento = armazenamento
        banco = BancoFalso(armazenamento = armazenamento)
    }

    val repositorio: CaisRepository
        get() = banco ?: BancoFalso().also { banco = it }
}

/** "2026-11-13" com [diasAtras] dias antes de hoje. */
fun dataIso(diasAtras: Int = 0): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, -diasAtras)
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
}
