package com.example.myapplication.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapplication.data.Categoria
import com.example.myapplication.data.FaltaDeEstoque
import com.example.myapplication.data.FiltroRapido
import com.example.myapplication.data.ItemCarrinho
import com.example.myapplication.data.banco.Armazenamento
import com.example.myapplication.data.banco.CaisBanco
import com.example.myapplication.data.banco.CaisRepository
import com.example.myapplication.data.banco.EnderecoRow
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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.myapplication.data.remoto.CatalogoRemoto
import com.example.myapplication.data.remoto.paraUi
import com.example.myapplication.data.remoto.AuthRemoto
import com.example.myapplication.data.remoto.CategoriaDto
import com.example.myapplication.data.remoto.EnderecoNovoDto
import com.example.myapplication.data.remoto.ItemPedidoNovoDto
import com.example.myapplication.data.remoto.PedidoNovoDto
import com.example.myapplication.data.remoto.PedidosRemoto
import com.example.myapplication.data.remoto.ZonaDto
import com.example.myapplication.data.remoto.passoParaStatus
import com.example.myapplication.data.remoto.FotosRemoto
import com.example.myapplication.data.remoto.ProdutoNovoDto
import com.example.myapplication.data.remoto.idDe

const val PRECO_LIMITE = 100f

/** Estoque inicial de um produto novo, até a tela de cadastro ter um campo para isso. */
private const val ESTOQUE_INICIAL_KG = 50.0

/** Arquivo onde fica guardado quem está logado, para o app abrir já na conta. */
private const val ARQUIVO_SESSAO = "sessao.txt"
private const val ARQUIVO_CARRINHO =  "Carrinho_visitante.txt"

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
    var carregandoCatalogo by mutableStateOf(true)
    var erroCatalogo by mutableStateOf<String?>(null)

    /** Categorias do Supabase; o cadastro de produto precisa do id delas. Declarada antes do init. */
    private var categoriasRemotas: List<CategoriaDto> = emptyList()

    /** Zonas de entrega do Supabase; o endereço do pedido guarda o id da zona. */
    private var zonasRemotas: List<ZonaDto> = emptyList()

    /** Id (uuid) de quem está logado no Supabase; null se ninguém entrou. */
    private var usuarioId: String? = null

    init {
        carregarCatalogo()
        recarregarPedidos()
    }

    fun carregarCatalogo(){
        viewModelScope.launch {
            carregandoCatalogo = true
            erroCatalogo = null
            try{
                val categorias = CatalogoRemoto.categorias()
                val vends = CatalogoRemoto.vendedores()
                val prods = CatalogoRemoto.produtos()
                categoriasRemotas = categorias
                zonasRemotas = PedidosRemoto.zonas()
                vendedores.clear()
                vendedores.addAll(vends.map {it.paraUi()})
                produtos.clear()
                produtos.addAll(prods.map{it.paraUi(categorias)})
                // os itens dos pedidos usam os produtos do catálogo: remonta a lista
                if (usuarioId != null) recarregarPedidos()
            } catch (e: Exception){
                android.util.Log.e("CAIS","catálogo falhou", e )
                erroCatalogo = "Não foi possivel carregar os produtos. verifique a conexão"
            }
            carregandoCatalogo = false
        }
    }
    // O catálogo agora vem do Supabase: recarregar é buscar tudo de novo lá.
    private fun recarregarVendedores() = carregarCatalogo()

    private fun recarregarProdutos() = carregarCatalogo()

    /** Busca no Supabase os pedidos de quem está logado (do mais novo para o mais antigo). */
    private fun recarregarPedidos() {
        val meuId = usuarioId
        if (meuId == null) {
            pedidos.clear()
            return
        }
        viewModelScope.launch {
            try {
                val dtos = PedidosRemoto.pedidos()
                val itens = PedidosRemoto.itens(dtos.map { it.id })
                val enderecos = PedidosRemoto.enderecos(dtos.mapNotNull { it.enderecoId }.distinct())
                val nomes = PedidosRemoto.nomes(dtos.map { it.clienteId }.distinct())
                    .associate { it.id to (it.nome ?: "Cliente") }
                val catalogo = produtos.toList()
                val prontos = dtos.map { d ->
                    d.paraUi(
                        itens = itens,
                        produtos = catalogo,
                        nomeDoCliente = nomes[d.clienteId] ?: "Cliente",
                        emailDoCliente = if (d.clienteId == meuId) usuario?.email ?: "" else "",
                        endereco = enderecos.firstOrNull { it.id == d.enderecoId }
                    )
                }
                pedidos.clear()
                pedidos.addAll(prontos)
            } catch (e: Exception) {
                android.util.Log.e("CAIS", "pedidos falharam", e)
            }
        }
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

    suspend fun entrar(email: String, senha: String): String? {
        return try {
            val perfil = AuthRemoto.entrar(email.trim(), senha)
            val vendedor = AuthRemoto.ehVendedor(perfil.id)
            usuarioId = perfil.id
            usuario = Usuario(
                    nome = perfil.nome?:email.trim(),
                    doc = perfil.cpfCnpj ?: "",
                    email = email.trim(),
                    senha = "",
                    vendedorId = if (vendedor) perfil.id else null
            )
            limparFiltros()
            recarregarPedidos()
            null
        }catch (e: Exception){
            android.util.Log.e("CAIS", "Login Falhou", e)
            "E-mail ou senha incorretos."
        }
    }

    /** Cria a conta no Supabase e já deixa a pessoa logada (com o id real, que o pedido exige). */
    suspend fun cadastrar(nome: String, doc: String, email: String, senha: String, vendedor: Boolean): String? {
        if (nome.isBlank()) return "Digite seu nome."
        if (doc.isBlank()) return "Digite seu CPF ou CNPJ."
        if (!email.contains("@") || !email.contains(".")) return "Digite um e-mail válido."
        if (senha.length < 6) return "A senha precisa ter pelo menos 6 caracteres."

        return try {
            val perfil = AuthRemoto.cadastrar(nome.trim(), doc.trim(), email.trim(), senha, vendedor)
            usuarioId = perfil.id
            usuario = Usuario(
                nome = nome.trim(),
                doc = doc.trim(),
                email = email.trim(),
                senha = "",
                vendedorId = if (vendedor) perfil.id else null
            )
            limparFiltros()
            if (vendedor) carregarCatalogo() // a loja nova passa a aparecer na lista de vendedores
            recarregarPedidos()
            null
        } catch (e: Exception) {
            android.util.Log.e("CAIS", "cadastro falhou", e)
            val jaExiste = e.message?.contains("already", ignoreCase = true) == true ||
                e.message?.contains("registered", ignoreCase = true) == true
            if (jaExiste) "Esse e-mail já tem cadastro. Toque em \"Já tem conta? Entrar\"."
            else "Não foi possível criar a conta. Confira a conexão e tente de novo."
        }
    }

    fun sair() {
        viewModelScope.launch { try { AuthRemoto.sair()} catch (_: Exception) {}}
        usuarioRow?.let { row ->
            repo.salvarCarrinho(row.id, carrinho.map { ItemDeCompra(it.produto.id, it.kg) })
        }
        usuarioRow = null
        usuarioId = null
        usuario = null
        pedidos.clear()
        armazenamento?.apagar(ARQUIVO_SESSAO)
        carrinho.clear()
        limparFiltros()
    }

    /** Telefone e endereços ficam só na tabela; as telas de "Minha conta" leem daqui. */
    val telefoneDoUsuario: String
        get() = usuarioRow?.telefone ?: ""

    fun meusEnderecos(): List<EnderecoRow> =
        usuarioRow?.let { repo.enderecosDoUsuario(it.id) } ?: emptyList()

    fun excluirConta() {
        usuarioRow?.let { repo.excluirUsuario(it.id) }
        carrinho.clear() // não salva o carrinho de uma conta que acabou de ser excluída
        sair()
        recarregarVendedores()
    }

    // ---------- busca e filtros ----------
    var busca by mutableStateOf("")
    var filtroRapido by mutableStateOf(FiltroRapido.TODOS)
    var vendedorFiltro by mutableStateOf<String?>(null)
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

    fun vendedorDe(id: String): Vendedor? = vendedores.firstOrNull { it.id == id }

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
    /** Aviso de itens que saíram do carrinho ("Produto não está mais disponivel e saiu do carrinho")*/
    val avisosCarrinho = mutableStateListOf<String>()

    /**Tira do carrinho os produtos que acabaram. Devolver true algum*/
    fun removerIndisponiveis(): Boolean{
        val acabaram = carrinho.filter { item -> produtos.firstOrNull(){it.id == item.produto.id} ?.disponivel != true }
        if (acabaram.isEmpty()) return false
        acabaram.forEach { avisosCarrinho.add("${it.produto.nome} não está mais disponivel e saiu do seu carrinho")}
        carrinho.removeAll(acabaram)
        return true
    }
    fun limparAvisosCarrinho() = avisosCarrinho.clear()

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
        get() = pedidos.filter { it.clienteId.isNotEmpty() && it.clienteId == usuarioId }

    val pedidosDoVendedor: List<Pedido>
        get() = pedidos.filter { it.vendedorId == usuario?.vendedorId }

    fun pedidoPorId(id: Int): Pedido? = pedidos.firstOrNull { it.id == id }

    /**
     * Cria no Supabase um pedido por vendedor (regra da equipe), com os itens e, se for entrega,
     * o endereço. O frete é cobrado em cada pedido. Devolve a mensagem de erro, ou null se deu certo.
     */
    suspend fun confirmarPedido(entrega: Boolean, zona: Zona, destino: String, pagamento: String): String? {
        val meuId = usuarioId ?: return "Entre na sua conta."
        if (removerIndisponiveis()) return "Um item não está mais disponivel e saiu do carrinho. Confira antes de confirmar"
        if (carrinho.isEmpty()) return "Seu carrinho está vazio."
        if (entrega) {
            if (destino.isBlank()) return "Digite o endereço de entrega."
            if (subtotal < zona.pedidoMinimo) {
                return "Pedido mínimo para ${zona.nome}: ${zona.pedidoMinimo.reais()}."
            }
        }

        // a zona escolhida na tela precisa existir no Supabase (o endereço guarda o id dela)
        val zonaRemota = if (entrega) {
            zonasRemotas.firstOrNull { it.bairro.equals(zona.nome, ignoreCase = true) }
                ?: return "Essa zona de entrega ainda não está cadastrada. Escolha outra ou retire com o vendedor."
        } else null

        return try {
            // a tela tem um campo só ("Rua das Palmeiras, 120 – Centro"): separa rua e número
            val enderecoId = if (zonaRemota != null) {
                val digitado = destino.substringBefore(" – ")
                val rua = digitado.substringBeforeLast(",").trim()
                val numero = if (digitado.contains(",")) digitado.substringAfterLast(",").trim() else "s/n"
                PedidosRemoto.criarEndereco(
                    EnderecoNovoDto(
                        clienteId = meuId,
                        zonaId = zonaRemota.id,
                        rua = rua,
                        bairro = zona.nome,
                        numero = numero
                    )
                ).id
            } else null

            val taxa = if (entrega) zona.taxa else 0.0
            // um pedido por vendedor
            carrinho.groupBy { it.produto.vendedorId }.forEach { (vendedorId, itens) ->
                val subtotal = itens.sumOf { it.subtotal }
                val pedido = PedidosRemoto.criarPedido(
                    PedidoNovoDto(
                        clienteId = meuId,
                        vendedorId = vendedorId,
                        enderecoId = enderecoId,
                        tipoEntrega = if (entrega) TipoEntrega.ENTREGA else TipoEntrega.RETIRADA,
                        subtotal = subtotal,
                        taxaEntrega = taxa,
                        total = subtotal + taxa,
                        formaPagamento = pagamento
                    )
                )
                PedidosRemoto.criarItens(
                    itens.map { ItemPedidoNovoDto(pedido.id, it.produto.id, it.kg, it.produto.preco) }
                )
            }

            carrinho.clear()
            recarregarPedidos()
            null
        } catch (e: Exception) {
            android.util.Log.e("CAIS", "pedido falhou", e)
            "Não foi possível confirmar o pedido. Confira a conexão e tente de novo."
        }
    }

    /** Passa o pedido para a próxima etapa no Supabase. Ao aceitar (etapa 1), os kg saem do estoque. */
    fun avancarStatus(pedidoId: Int) {
        val pedido = pedidos.firstOrNull { it.id == pedidoId } ?: return
        val proximo = pedido.status + 1
        if (proximo > 4) return
        viewModelScope.launch {
            try {
                PedidosRemoto.mudarStatus(pedidoId, passoParaStatus(proximo, pedido.entrega))
                if (proximo == 1) {
                    pedido.itens.forEach { item ->
                        val atual = produtos.firstOrNull { it.id == item.produto.id } ?: return@forEach
                        CatalogoRemoto.definirEstoque(atual.id, (atual.estoqueKg - item.kg).coerceAtLeast(0.0))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CAIS", "não consegui avançar o pedido", e)
            }
            recarregarPedidos()
            carregarCatalogo()
        }
    }

    /**
     * Itens do pedido que pedem mais kg do que há no estoque agora.
     * Só importa enquanto o pedido ainda não foi aceito (o estoque baixa ao aceitar).
     */
    fun faltasDeEstoque(pedido: Pedido): List<FaltaDeEstoque> {
        if (pedido.status != 0) return emptyList()
        return pedido.itens.groupBy { it.produto.id }.mapNotNull { (id, itens) ->
            val produto = produtos.firstOrNull { it.id == id } ?: return@mapNotNull null
            val pedidoKg = itens.sumOf { it.kg }
            if (pedidoKg > produto.estoqueKg) FaltaDeEstoque(produto.nome, pedidoKg, produto.estoqueKg) else null
        }
    }

    // ---------- produtos do vendedor ----------
    val produtosDoVendedor: List<Produto>
        get() = produtos.filter { it.vendedorId == usuario?.vendedorId }

    fun alternarDisponibilidade(produtoId: Int) {
        val atual = produtos.firstOrNull { it.id == produtoId } ?: return
        viewModelScope.launch {
            try {
                CatalogoRemoto.definirDisponibilidade(produtoId, !atual.disponivel)
            } catch (e: Exception) {
                android.util.Log.e("CAIS", "não consegui mudar a disponibilidade", e)
            }
            carregarCatalogo()
        }
    }

    fun mudarEstoque(produtoId: Int, estoqueKg: Double) {
        viewModelScope.launch {
            try {
                CatalogoRemoto.definirEstoque(produtoId, estoqueKg)
            } catch (e: Exception) {
                android.util.Log.e("CAIS", "não consegui mudar o estoque", e)
            }
            carregarCatalogo()
        }
    }

    /** Aba que o Painel abre na próxima vez (0 Pedidos, 1 Meus produtos). Vale uma vez só. */
    private var abaDoPainel = 0

    fun abrirPainelEm(aba: Int) {
        abaDoPainel = aba
    }

    fun pegarAbaDoPainel(): Int = abaDoPainel.also { abaDoPainel = 0 }

    /**
     * Cadastra o produto no Supabase. Se [foto] (JPEG já reduzido) vier preenchida, envia
     * primeiro ao Storage e grava a URL no produto. Devolve a mensagem de erro, ou null se deu certo.
     */
    suspend fun cadastrarProduto(
        nome: String,
        categoria: Categoria,
        tipo: String,
        precoTexto: String,
        dataPesca: String,
        disponivel: Boolean,
        foto: ByteArray? = null
    ): String? {
        val vendedorId = usuario?.vendedorId ?: return "Apenas vendedores cadastram produtos."
        if (nome.isBlank()) return "Digite o nome do produto."
        val preco = precoTexto.trim().replace(',', '.').toDoubleOrNull()
        if (preco == null || preco <= 0.0) return "Digite um preço válido (ex.: 38,90)."
        val categoriaId = categoriasRemotas.idDe(categoria)
            ?: return "Não achei a categoria no servidor. Feche e abra o app e tente de novo."

        return try {
            val urlDaFoto = foto?.let { FotosRemoto.enviar(vendedorId, it) }
            CatalogoRemoto.criarProduto(
                ProdutoNovoDto(
                    vendedorId = vendedorId,
                    categoriaId = categoriaId,
                    nome = nome.trim(),
                    descricao = tipo.trim().ifEmpty { categoria.rotulo },
                    precoKg = preco,
                    foto = urlDaFoto,
                    dataPesca = dataBrParaIso(dataPesca),
                    disponivel = disponivel,
                    estoqueKg = ESTOQUE_INICIAL_KG
                )
            )
            carregarCatalogo()
            null
        } catch (e: Exception) {
            android.util.Log.e("CAIS", "cadastro de produto falhou", e)
            "Não foi possível salvar o produto. Confira a conexão e tente de novo."
        }
    }

    // Fica no fim da classe de propósito: só roda depois de todas as propriedades existirem.
    init {
        // O login agora é do Supabase. Apaga o arquivo de sessão do banco falso, que
        // reabriria um usuário de mentira (sem id no Supabase) e quebraria os pedidos.
        armazenamento?.apagar(ARQUIVO_SESSAO)
    }
}
