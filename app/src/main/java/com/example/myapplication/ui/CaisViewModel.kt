package com.example.myapplication.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.Categoria
import com.example.myapplication.data.FiltroRapido
import com.example.myapplication.data.ItemCarrinho
import com.example.myapplication.data.MockData
import com.example.myapplication.data.Pedido
import com.example.myapplication.data.Produto
import com.example.myapplication.data.Usuario
import com.example.myapplication.data.Vendedor
import com.example.myapplication.data.Zona
import com.example.myapplication.data.reais

const val PRECO_LIMITE = 100f

/**
 * Guarda todo o estado do app (login, catálogo, filtros, carrinho, pedidos).
 * As funções que podem falhar devolvem uma mensagem de erro (String) ou null se deu certo.
 */
class CaisViewModel : ViewModel() {

    // ---------- dados ----------
    private val usuarios = mutableListOf<Usuario>().apply { addAll(MockData.usuarios) }
    val vendedores = mutableStateListOf<Vendedor>().apply { addAll(MockData.vendedores) }
    val produtos = mutableStateListOf<Produto>().apply { addAll(MockData.produtos) }
    val pedidos = mutableStateListOf<Pedido>().apply { addAll(MockData.pedidos) }
    val carrinho = mutableStateListOf<ItemCarrinho>()
    val favoritos = mutableStateListOf<Int>()

    // ---------- sessão ----------
    var usuario by mutableStateOf<Usuario?>(null)
        private set

    fun entrar(email: String, senha: String): String? {
        val encontrado = usuarios.firstOrNull {
            it.email.equals(email.trim(), ignoreCase = true) && it.senha == senha
        } ?: return "E-mail ou senha incorretos."
        usuario = encontrado
        limparFiltros()
        return null
    }

    fun cadastrar(nome: String, doc: String, email: String, senha: String, vendedor: Boolean): String? {
        if (nome.isBlank()) return "Digite seu nome."
        if (doc.isBlank()) return "Digite seu CPF ou CNPJ."
        if (!email.contains("@") || !email.contains(".")) return "Digite um e-mail válido."
        if (senha.length < 6) return "A senha precisa ter pelo menos 6 caracteres."
        if (usuarios.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return "Esse e-mail já tem conta."
        }
        var vendedorId: Int? = null
        if (vendedor) {
            val novoId = (vendedores.maxOfOrNull { it.id } ?: 0) + 1
            vendedores.add(Vendedor(novoId, nome.trim(), "Vendedor no Cais", "Centro", 0.0))
            vendedorId = novoId
        }
        val novo = Usuario(nome.trim(), doc.trim(), email.trim(), senha, vendedorId)
        usuarios.add(novo)
        usuario = novo
        limparFiltros()
        return null
    }

    fun sair() {
        usuario = null
        carrinho.clear()
        limparFiltros()
    }

    fun excluirConta() {
        usuario?.let { usuarios.remove(it) }
        sair()
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
        val u = usuario ?: return "Entre na sua conta."
        if (carrinho.isEmpty()) return "Seu carrinho está vazio."
        if (entrega) {
            if (destino.isBlank()) return "Digite o endereço de entrega."
            if (subtotal < zona.pedidoMinimo) {
                return "Pedido mínimo para ${zona.nome}: ${zona.pedidoMinimo.reais()}."
            }
        }
        val novoId = (pedidos.maxOfOrNull { it.id } ?: 1000) + 1
        pedidos.add(
            0,
            Pedido(
                id = novoId,
                clienteEmail = u.email,
                clienteNome = u.nome,
                vendedorId = carrinho.first().produto.vendedorId,
                itens = carrinho.toList(),
                entrega = entrega,
                destino = destino,
                taxa = if (entrega) zona.taxa else 0.0,
                pagamento = pagamento
            )
        )
        carrinho.clear()
        return null
    }

    fun avancarStatus(pedidoId: Int) {
        val i = pedidos.indexOfFirst { it.id == pedidoId }
        if (i >= 0 && pedidos[i].status < 3) {
            pedidos[i] = pedidos[i].copy(status = pedidos[i].status + 1)
        }
    }

    // ---------- produtos do vendedor ----------
    val produtosDoVendedor: List<Produto>
        get() = produtos.filter { it.vendedorId == usuario?.vendedorId }

    fun alternarDisponibilidade(produtoId: Int) {
        val i = produtos.indexOfFirst { it.id == produtoId }
        if (i >= 0) produtos[i] = produtos[i].copy(disponivel = !produtos[i].disponivel)
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
        val novoId = (produtos.maxOfOrNull { it.id } ?: 0) + 1
        produtos.add(
            Produto(
                id = novoId,
                nome = nome.trim(),
                tipo = tipo.trim().ifEmpty { categoria.rotulo },
                categoria = categoria,
                preco = preco,
                nota = 0.0,
                vendedorId = vendedorId,
                dataPesca = dataPesca,
                disponivel = disponivel
            )
        )
        return null
    }
}
