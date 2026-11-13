package com.example.myapplication.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.Categoria
import com.example.myapplication.data.FiltroRapido
import com.example.myapplication.data.ItemCarrinho
import com.example.myapplication.data.banco.Armazenamento
import com.example.myapplication.data.banco.CaisBanco
import com.example.myapplication.data.banco.CaisRepository
import com.example.myapplication.data.banco.ItemDeCompra
import com.example.myapplication.data.banco.Resultado
import com.example.myapplication.data.banco.TipoEntrega
import com.example.myapplication.data.banco.TipoUsuario
import com.example.myapplication.data.banco.UsuarioRow
import com.example.myapplication.data.banco.categoriaIdDe
import com.example.myapplication.data.banco.dataBrParaIso
import com.example.myapplication.data.banco.paraUi
import com.example.myapplication.data.Pedido
import com.example.myapplication.data.Produto
import com.example.myapplication.data.Usuario
import com.example.myapplication.data.Vendedor
import com.example.myapplication.data.Zona
import com.example.myapplication.data.reais

const val PRECO_LIMITE = 100f

/** Arquivo onde fica guardado quem está logado, para o app abrir já na conta. */
private const val ARQUIVO_SESSAO = "sessao.txt"

/**
 * Guarda todo o estado do app (login, catálogo, filtros, carrinho, pedidos).
 * Os dados vêm do [CaisRepository] (hoje o banco falso). As funções que podem falhar
 * devolvem uma mensagem de erro (String) ou null se deu certo.
 */
class CaisViewModel(
    private val repo: CaisRepository = CaisBanco.repositorio,
    private val armazenamento: Armazenamento? = CaisBanco.armazenamento
) : ViewModel() {

    // ---------- dados (cópias para as telas; a fonte é o banco) ----------
    val vendedores = mutableStateListOf<Vendedor>()
    val produtos = mutableStateListOf<Produto>()
    private val pedidos = mutableStateListOf<Pedido>()

    /** O carrinho fica no celular; só vai para o banco depois do login (decisão da equipe). */
    val carrinho = mutableStateListOf<ItemCarrinho>()
    val favoritos = mutableStateListOf<Int>()

    init {
        recarregarVendedores()
        recarregarProdutos()
        recarregarPedidos()
    }

    private fun recarregarVendedores() {
        vendedores.clear()
        vendedores.addAll(repo.listarVendedores().map { it.paraUi(repo) })
    }

    private fun recarregarProdutos() {
        produtos.clear()
        produtos.addAll(repo.listarProdutos().map { it.paraUi(repo) })
    }

    private fun recarregarPedidos() {
        pedidos.clear()
        // do mais novo para o mais antigo
        pedidos.addAll(repo.listarPedidosRecentes().map { it.paraUi(repo) })
    }

    // ---------- sessão ----------
    private var usuarioRow: UsuarioRow? = null

    var usuario by mutableStateOf<Usuario?>(null)
        private set

    private fun iniciarSessao(row: UsuarioRow) {
        usuarioRow = row
        usuario = row.paraUi(repo)
        armazenamento?.gravar(ARQUIVO_SESSAO, row.id.toString())
        // carrinho feito antes do login vai para o banco; se estava vazio, volta o que ficou salvo
        if (carrinho.isNotEmpty()) {
            repo.salvarCarrinho(row.id, carrinho.map { ItemDeCompra(it.produto.id, it.kg) })
        } else {
            carrinho.addAll(repo.carrinhoDoUsuario(row.id).mapNotNull {
                ItemDeCompra(it.produtoId, it.quantidadeKg).paraUi(repo)
            })
        }
        limparFiltros()
    }

    fun entrar(email: String, senha: String): String? {
        val encontrado = repo.autenticar(email, senha) ?: return "E-mail ou senha incorretos."
        iniciarSessao(encontrado)
        return null
    }

    /** [doc] (CPF/CNPJ) ainda é pedido na tela, mas a tabela usuario não tem esse campo. */
    fun cadastrar(nome: String, doc: String, email: String, senha: String, vendedor: Boolean): String? {
        if (nome.isBlank()) return "Digite seu nome."
        if (doc.isBlank()) return "Digite seu CPF ou CNPJ."
        if (!email.contains("@") || !email.contains(".")) return "Digite um e-mail válido."
        if (senha.length < 6) return "A senha precisa ter pelo menos 6 caracteres."

        val tipo = if (vendedor) TipoUsuario.VENDEDOR else TipoUsuario.CLIENTE
        val criado = when (val r = repo.criarUsuario(nome, email, senha, "", tipo)) {
            is Resultado.Erro -> return r.mensagem
            is Resultado.Ok -> r.valor
        }
        if (vendedor) {
            repo.criarVendedor(criado.id, nome.trim(), "Vendedor no Cais")
            recarregarVendedores()
        }
        iniciarSessao(criado)
        return null
    }

    fun sair() {
        usuarioRow?.let { row ->
            repo.salvarCarrinho(row.id, carrinho.map { ItemDeCompra(it.produto.id, it.kg) })
        }
        usuarioRow = null
        usuario = null
        armazenamento?.apagar(ARQUIVO_SESSAO)
        carrinho.clear()
        limparFiltros()
    }

    fun excluirConta() {
        usuarioRow?.let { repo.excluirUsuario(it.id) }
        carrinho.clear() // não salva o carrinho de uma conta que acabou de ser excluída
        sair()
        recarregarVendedores()
    }

    // ---------- busca e filtros ----------
    var busca by mutableStateOf("")
    var filtroRapido by mutableStateOf(FiltroRapido.TODOS)
    var vendedorFiltro by mutableStateOf<Int?>(null)
    var bairroFiltro by mutableStateOf<String?>(null)
    var precoMax by mutableStateOf(PRECO_LIMITE)

    fun limparFiltros() {
        busca = ""
        filtroRapido = FiltroRapido.TODOS
        vendedorFiltro = null
        bairroFiltro = null
        precoMax = PRECO_LIMITE
    }

    val filtrosAtivos: Boolean
        get() = busca.isNotBlank() || filtroRapido != FiltroRapido.TODOS ||
            vendedorFiltro != null || bairroFiltro != null || precoMax < PRECO_LIMITE

    fun vendedorDe(id: Int): Vendedor? = vendedores.firstOrNull { it.id == id }

    val bairros: List<String>
        get() = vendedores.map { it.bairro }.distinct().sorted()

    val produtosFiltrados: List<Produto>
        get() {
            val texto = busca.trim()
            return produtos.filter { p ->
                val v = vendedorDe(p.vendedorId)
                p.disponivel &&
                    (texto.isEmpty() ||
                        p.nome.contains(texto, ignoreCase = true) ||
                        p.tipo.contains(texto, ignoreCase = true) ||
                        v?.nome?.contains(texto, ignoreCase = true) == true) &&
                    (filtroRapido.categoria == null || p.categoria == filtroRapido.categoria) &&
                    (filtroRapido != FiltroRapido.PROMOCOES || p.promocao) &&
                    (vendedorFiltro == null || p.vendedorId == vendedorFiltro) &&
                    (bairroFiltro == null || v?.bairro == bairroFiltro) &&
                    p.preco <= precoMax
            }
        }

    val maisProcurados: List<Produto>
        get() = produtos.filter { it.disponivel }.sortedByDescending { it.nota }.take(6)

    val recomendados: List<Produto>
        get() = produtos.filter { it.disponivel }.sortedByDescending { it.id }.take(6)

    fun alternarFavorito(produtoId: Int) {
        if (!favoritos.remove(produtoId)) favoritos.add(produtoId)
    }

    // ---------- carrinho ----------
    val subtotal: Double
        get() = carrinho.sumOf { it.subtotal }

    /** Devolve false se o carrinho já tem itens de outro vendedor. */
    fun adicionarAoCarrinho(produto: Produto, kg: Double): Boolean {
        if (carrinho.isNotEmpty() && carrinho.first().produto.vendedorId != produto.vendedorId) {
            return false
        }
        val i = carrinho.indexOfFirst { it.produto.id == produto.id }
        if (i >= 0) {
            carrinho[i] = carrinho[i].copy(kg = carrinho[i].kg + kg)
        } else {
            carrinho.add(ItemCarrinho(produto, kg))
        }
        return true
    }

    fun trocarCarrinho(produto: Produto, kg: Double) {
        carrinho.clear()
        carrinho.add(ItemCarrinho(produto, kg))
    }

    fun alterarQuantidade(produtoId: Int, delta: Double) {
        val i = carrinho.indexOfFirst { it.produto.id == produtoId }
        if (i >= 0) {
            val novo = (carrinho[i].kg + delta).coerceAtLeast(0.5)
            carrinho[i] = carrinho[i].copy(kg = novo)
        }
    }

    fun removerDoCarrinho(produtoId: Int) {
        carrinho.removeAll { it.produto.id == produtoId }
    }

    // ---------- pedidos ----------
    val meusPedidos: List<Pedido>
        get() = pedidos.filter { it.clienteEmail.equals(usuario?.email, ignoreCase = true) }

    val pedidosDoVendedor: List<Pedido>
        get() = pedidos.filter { it.vendedorId == usuario?.vendedorId }

    fun confirmarPedido(entrega: Boolean, zona: Zona, destino: String, pagamento: String): String? {
        val u = usuarioRow ?: return "Entre na sua conta."
        if (carrinho.isEmpty()) return "Seu carrinho está vazio."
        if (entrega) {
            if (destino.isBlank()) return "Digite o endereço de entrega."
            if (subtotal < zona.pedidoMinimo) {
                return "Pedido mínimo para ${zona.nome}: ${zona.pedidoMinimo.reais()}."
            }
        }

        // a tela tem um campo só ("Rua das Palmeiras, 120 – Centro"): separa rua e número
        val enderecoId = if (entrega) {
            val digitado = destino.substringBefore(" – ")
            val rua = digitado.substringBeforeLast(",").trim()
            val numero = if (digitado.contains(",")) digitado.substringAfterLast(",").trim() else "s/n"
            repo.salvarEndereco(u.id, rua, numero, zona.nome, "Manaus").id
        } else null

        val resultado = repo.criarPedidos(
            clienteId = u.id,
            itens = carrinho.map { ItemDeCompra(it.produto.id, it.kg) },
            tipoEntrega = if (entrega) TipoEntrega.ENTREGA else TipoEntrega.RETIRADA,
            enderecoId = enderecoId,
            formaPagamento = pagamento,
            frete = zona.taxa
        )
        if (resultado is Resultado.Erro) return resultado.mensagem

        carrinho.clear()
        recarregarPedidos()
        return null
    }

    fun avancarStatus(pedidoId: Int) {
        repo.avancarStatus(pedidoId)
        recarregarPedidos()
    }

    // ---------- produtos do vendedor ----------
    val produtosDoVendedor: List<Produto>
        get() = produtos.filter { it.vendedorId == usuario?.vendedorId }

    fun alternarDisponibilidade(produtoId: Int) {
        val atual = produtos.firstOrNull { it.id == produtoId } ?: return
        repo.mudarDisponibilidade(produtoId, !atual.disponivel)
        recarregarProdutos()
    }

    fun cadastrarProduto(
        nome: String,
        categoria: Categoria,
        tipo: String,
        precoTexto: String,
        dataPesca: String,
        disponivel: Boolean
    ): String? {
        val vendedorId = usuario?.vendedorId ?: return "Apenas vendedores cadastram produtos."
        if (nome.isBlank()) return "Digite o nome do produto."
        val preco = precoTexto.trim().replace(',', '.').toDoubleOrNull()
        if (preco == null || preco <= 0.0) return "Digite um preço válido (ex.: 38,90)."

        val resultado = repo.criarProduto(
            vendedorId = vendedorId,
            categoriaId = repo.categoriaIdDe(categoria),
            nome = nome,
            descricao = tipo.trim().ifEmpty { categoria.rotulo },
            precoKg = preco,
            dataPesca = dataBrParaIso(dataPesca),
            disponivel = disponivel
        )
        if (resultado is Resultado.Erro) return resultado.mensagem
        recarregarProdutos()
        return null
    }

    // Fica no fim da classe de propósito: só roda depois de todas as propriedades existirem.
    init {
        val id = armazenamento?.ler(ARQUIVO_SESSAO)?.trim()?.toIntOrNull()
        val salvo = id?.let { repo.usuarioPorId(it) }
        if (salvo != null) iniciarSessao(salvo) else armazenamento?.apagar(ARQUIVO_SESSAO)
    }
}
