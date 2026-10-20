package com.example.myapplication.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.MockData
import com.example.myapplication.data.emKg
import com.example.myapplication.data.reais

// =====================================================================
// Peças compartilhadas
// =====================================================================

@Composable
private fun CabecalhoLoja(qtdCarrinho: Int, aoCarrinho: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CaisLogo()
        Spacer(Modifier.weight(1f))
        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Navy, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(2.dp))
        Text("Manaus - AM", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(4.dp))
        IconButton(onClick = aoCarrinho) {
            BadgedBox(
                badge = {
                    if (qtdCarrinho > 0) Badge(containerColor = Teal) { Text("$qtdCarrinho") }
                }
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = "Abrir carrinho")
            }
        }
    }
}

@Composable
private fun TopoComVoltar(titulo: String, aoVoltar: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = aoVoltar) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
        }
        Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    }
}

// =====================================================================
// HOME
// =====================================================================

@Composable
fun HomeScreen(
    vm: CaisViewModel,
    aoBuscar: () -> Unit,
    aoAbrirProduto: (Int) -> Unit,
    aoCarrinho: () -> Unit
) {
    fun irParaBusca(configurar: () -> Unit = {}) {
        vm.limparFiltros()
        configurar()
        aoBuscar()
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item { CabecalhoLoja(vm.carrinho.size, aoCarrinho) }

        // barra de busca (toque abre a tela de Busca)
        item {
            Row(
                Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Ice)
                        .clickable { irParaBusca() }
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Muted)
                    Spacer(Modifier.width(8.dp))
                    Text("Buscar peixe, camarão, vendedor...", fontSize = 13.sp, color = Muted)
                }
                Spacer(Modifier.width(8.dp))
                BotaoQuadradoClaro { irParaBusca() }
            }
        }

        // chips de categoria
        item {
            Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(com.example.myapplication.data.FiltroRapido.entries.toList()) { f ->
                    ChipFiltro(f.rotulo, selecionado = false) { irParaBusca { vm.filtroRapido = f } }
                }
            }
        }

        // banner
        item {
            Box(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.horizontalGradient(listOf(Navy, Color(0xFF0E6E95), Teal)))
            ) {
                Column(
                    Modifier
                        .fillMaxWidth(0.68f)
                        .padding(18.dp)
                ) {
                    Text(
                        "Tambaqui fresco\nchegando hoje.",
                        color = Color.White,
                        fontSize = 21.sp,
                        lineHeight = 25.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Peixe de rio direto dos mercados locais para a sua mesa.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Teal)
                            .clickable { irParaBusca { vm.busca = "Tambaqui" } }
                            .padding(horizontal = 14.dp, vertical = 9.dp)
                    ) {
                        Text("Comprar agora →", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                Text(
                    "🐟",
                    fontSize = 64.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                )
            }
        }

        item { SecaoTitulo("Mais procurados") { irParaBusca() } }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(vm.maisProcurados, key = { it.id }) { p ->
                    CartaoProdutoMini(
                        produto = p,
                        vendedor = vm.vendedorDe(p.vendedorId),
                        favorito = p.id in vm.favoritos,
                        aoFavoritar = { vm.alternarFavorito(p.id) },
                        aoClicar = { aoAbrirProduto(p.id) }
                    )
                }
            }
        }

        item { SecaoTitulo("Vendedores em destaque") { irParaBusca() } }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(vm.vendedores, key = { it.id }) { v ->
                    CartaoVendedor(v) { irParaBusca { vm.vendedorFiltro = v.id } }
                }
            }
        }

        item { SecaoTitulo("Recomendados para você") { irParaBusca() } }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(vm.recomendados, key = { it.id }) { p ->
                    CartaoProdutoMini(
                        produto = p,
                        vendedor = vm.vendedorDe(p.vendedorId),
                        favorito = p.id in vm.favoritos,
                        aoFavoritar = { vm.alternarFavorito(p.id) },
                        aoClicar = { aoAbrirProduto(p.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BotaoQuadradoClaro(onClick: () -> Unit) {
    Box(
        Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Ice)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Tune, contentDescription = "Filtros", tint = Navy)
    }
}

// =====================================================================
// BUSCA
// =====================================================================

@Composable
fun BuscaScreen(
    vm: CaisViewModel,
    aoAbrirProduto: (Int) -> Unit,
    aoCarrinho: () -> Unit
) {
    var mostrarFiltros by remember { mutableStateOf(false) }
    val resultados = vm.produtosFiltrados

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        CabecalhoLoja(vm.carrinho.size, aoCarrinho)

        Row(
            Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = vm.busca,
                onValueChange = { vm.busca = it },
                placeholder = { Text("Buscar peixe, camarão, vendedor...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (vm.busca.isNotEmpty()) {
                        IconButton(onClick = { vm.busca = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Limpar busca")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Ice,
                    unfocusedContainerColor = Ice,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            BotaoQuadradoClaro { mostrarFiltros = true }
        }

        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(com.example.myapplication.data.FiltroRapido.entries.toList()) { f ->
                ChipFiltro(f.rotulo, selecionado = vm.filtroRapido == f) { vm.filtroRapido = f }
            }
        }

        // filtros ativos (vendedor, bairro, preço)
        val vendedorAtivo = vm.vendedorFiltro?.let { vm.vendedorDe(it) }
        if (vendedorAtivo != null || vm.bairroFiltro != null || vm.precoMax < PRECO_LIMITE) {
            Spacer(Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (vendedorAtivo != null) {
                    item { EtiquetaFiltro("Vendedor: ${vendedorAtivo.nome}") { vm.vendedorFiltro = null } }
                }
                vm.bairroFiltro?.let { b ->
                    item { EtiquetaFiltro("Bairro: $b") { vm.bairroFiltro = null } }
                }
                if (vm.precoMax < PRECO_LIMITE) {
                    item {
                        EtiquetaFiltro("Até ${vm.precoMax.toDouble().reais()}/kg") { vm.precoMax = PRECO_LIMITE }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                .background(Pagina)
        ) {
            if (resultados.isEmpty()) {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    EstadoVazio(
                        emoji = "🎣",
                        titulo = "Nada por aqui",
                        subtitulo = "Nenhum produto encontrado com esses filtros.",
                        textoBotao = "Limpar filtros",
                        aoClicar = { vm.limparFiltros() }
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            "${resultados.size} ${if (resultados.size == 1) "resultado" else "resultados"}",
                            fontSize = 12.sp,
                            color = Muted
                        )
                    }
                    items(resultados, key = { it.id }) { p ->
                        CartaoProduto(
                            produto = p,
                            vendedor = vm.vendedorDe(p.vendedorId),
                            favorito = p.id in vm.favoritos,
                            aoFavoritar = { vm.alternarFavorito(p.id) },
                            aoClicar = { aoAbrirProduto(p.id) }
                        )
                    }
                }
            }
        }
    }

    if (mostrarFiltros) {
        DialogoFiltros(vm) { mostrarFiltros = false }
    }
}

@Composable
private fun EtiquetaFiltro(texto: String, aoRemover: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(FaixaTeal)
            .clickable(onClick = aoRemover)
            .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texto, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Filled.Close, contentDescription = "Remover filtro", modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun DialogoFiltros(vm: CaisViewModel, aoFechar: () -> Unit) {
    var preco by remember { mutableStateOf(vm.precoMax) }
    var bairro by remember { mutableStateOf(vm.bairroFiltro) }

    AlertDialog(
        onDismissRequest = aoFechar,
        containerColor = Color.White,
        title = { Text("Filtros", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column {
                val rotuloPreco =
                    if (preco >= PRECO_LIMITE) "Preço máximo: sem limite"
                    else "Preço máximo: ${preco.toDouble().reais()}/kg"
                Text(rotuloPreco, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = preco,
                    onValueChange = { preco = Math.round(it / 5f) * 5f },
                    valueRange = 10f..PRECO_LIMITE,
                    colors = SliderDefaults.colors(thumbColor = Navy, activeTrackColor = Teal)
                )
                Spacer(Modifier.height(8.dp))
                Text("Bairro do vendedor", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { ChipFiltro("Todos", bairro == null) { bairro = null } }
                    items(vm.bairros) { b ->
                        ChipFiltro(b, bairro == b) { bairro = b }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                vm.precoMax = preco
                vm.bairroFiltro = bairro
                aoFechar()
            }) { Text("Aplicar", color = Navy, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = {
                preco = PRECO_LIMITE
                bairro = null
            }) { Text("Limpar", color = Muted) }
        }
    )
}

// =====================================================================
// DETALHE DO PRODUTO
// =====================================================================

@Composable
fun ProdutoScreen(
    vm: CaisViewModel,
    produtoId: Int,
    aoVoltar: () -> Unit,
    aoCarrinho: () -> Unit,
    aoVerVendedor: (Int) -> Unit
) {
    val contexto = LocalContext.current
    val produto = vm.produtos.firstOrNull { it.id == produtoId }

    if (produto == null) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            TopoComVoltar("Produto", aoVoltar)
            EstadoVazio("🤷", "Produto não encontrado", "Ele pode ter sido removido pelo vendedor.")
        }
        return
    }

    val vendedor = vm.vendedorDe(produto.vendedorId)
    var kg by remember { mutableStateOf(1.0) }
    var conflito by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Box {
                FotoProduto(
                    categoria = produto.categoria,
                    seed = produto.id,
                    foto = produto.foto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    tamanhoEmoji = 96.sp
                )
                Row(
                    Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BotaoCircular(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", onClick = aoVoltar)
                    Spacer(Modifier.weight(1f))
                    BotaoCircular(Icons.Filled.ShoppingCart, "Abrir carrinho", onClick = aoCarrinho)
                    Spacer(Modifier.width(8.dp))
                    val favorito = produto.id in vm.favoritos
                    BotaoCircular(
                        if (favorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        if (favorito) "Remover dos favoritos" else "Favoritar",
                        tinta = if (favorito) Color(0xFFE5484D) else Navy,
                        onClick = { vm.alternarFavorito(produto.id) }
                    )
                    Spacer(Modifier.width(8.dp))
                    BotaoCircular(Icons.Filled.Share, "Compartilhar") {
                        val envio = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${produto.nome} fresco no Cais: ${produto.preco.reais()}/kg"
                            )
                        }
                        contexto.startActivity(Intent.createChooser(envio, "Compartilhar"))
                    }
                }
            }

            Column(Modifier.padding(16.dp)) {
                Text(produto.nome, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                Row {
                    Text(produto.tipo + " • ", fontSize = 15.sp, color = Muted)
                    Text("Fresco", fontSize = 15.sp, color = Verde, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(4.dp))
                Nota(produto.nota)
                Spacer(Modifier.height(4.dp))
                Text(produto.preco.reais() + "/kg", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
                Text("Pesca/chegada: ${produto.dataPesca}", fontSize = 14.sp, color = Muted)

                Spacer(Modifier.height(18.dp))
                Text("Vendedor", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                FaixaVendedor(
                    vendedor,
                    Modifier.clickable { aoVerVendedor(produto.vendedorId) }
                )

                Spacer(Modifier.height(18.dp))
                Text("Quantidade", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(8.dp))
                StepperKg(
                    kg = kg,
                    aoDiminuir = { kg = (kg - 0.5).coerceAtLeast(0.5) },
                    aoAumentar = { kg += 0.5 }
                )
            }
        }

        LinhaSeparadora()
        Column(
            Modifier
                .background(Color.White)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            BotaoPrimario("Adicionar ao carrinho • " + (produto.preco * kg).reais()) {
                if (vm.adicionarAoCarrinho(produto, kg)) {
                    Toast.makeText(contexto, "Adicionado ao carrinho", Toast.LENGTH_SHORT).show()
                } else {
                    conflito = true
                }
            }
        }
    }

    if (conflito) {
        AlertDialog(
            onDismissRequest = { conflito = false },
            containerColor = Color.White,
            title = { Text("Carrinho de outro vendedor", fontWeight = FontWeight.ExtraBold) },
            text = {
                Text("Seu carrinho tem itens de outro vendedor. Quer esvaziar o carrinho e adicionar este produto?")
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.trocarCarrinho(produto, kg)
                    conflito = false
                    Toast.makeText(contexto, "Adicionado ao carrinho", Toast.LENGTH_SHORT).show()
                }) { Text("Esvaziar e adicionar", color = Navy, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { conflito = false }) { Text("Cancelar", color = Muted) }
            }
        )
    }
}

// =====================================================================
// CARRINHO
// =====================================================================

@Composable
fun CarrinhoScreen(
    vm: CaisViewModel,
    aoVoltar: () -> Unit,
    aoVerPeixes: () -> Unit,
    aoContinuar: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        TopoComVoltar("Carrinho", aoVoltar)

        if (vm.carrinho.isEmpty()) {
            Column(Modifier.weight(1f)) {
                EstadoVazio(
                    emoji = "🛒",
                    titulo = "Seu carrinho está vazio",
                    subtitulo = "Escolha um peixe fresquinho para começar.",
                    textoBotao = "Ver peixes",
                    aoClicar = aoVerPeixes
                )
            }
        } else {
            val vendedor = vm.carrinho.firstOrNull()?.let { vm.vendedorDe(it.produto.vendedorId) }
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (vendedor != null) {
                    item {
                        Text(
                            "Vendedor: ${vendedor.nome} (${vendedor.descricao})",
                            fontSize = 13.sp,
                            color = Muted
                        )
                    }
                }
                items(vm.carrinho.toList(), key = { it.produto.id }) { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Ice)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FotoProduto(
                            categoria = item.produto.categoria,
                            seed = item.produto.id,
                            foto = item.produto.foto,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            tamanhoEmoji = 28.sp
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.produto.nome, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            Text(item.produto.preco.reais() + "/kg", fontSize = 12.sp, color = Muted)
                            Spacer(Modifier.height(6.dp))
                            StepperKg(
                                kg = item.kg,
                                aoDiminuir = { vm.alterarQuantidade(item.produto.id, -0.5) },
                                aoAumentar = { vm.alterarQuantidade(item.produto.id, 0.5) }
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(item.subtotal.reais(), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            IconButton(onClick = { vm.removerDoCarrinho(item.produto.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remover item", tint = Perigo)
                            }
                        }
                    }
                }
            }

            LinhaSeparadora()
            Column(
                Modifier
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth()) {
                    Text("Subtotal", fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text(vm.subtotal.reais(), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(Modifier.height(12.dp))
                BotaoPrimario("Continuar", onClick = aoContinuar)
            }
        }
    }
}

// =====================================================================
// CHECKOUT
// =====================================================================

@Composable
fun CheckoutScreen(
    vm: CaisViewModel,
    aoVoltar: () -> Unit,
    aoConfirmar: () -> Unit
) {
    val contexto = LocalContext.current
    var modo by remember { mutableStateOf(0) } // 0 = entrega, 1 = retirada
    var endereco by remember { mutableStateOf("") }
    var zonaIdx by remember { mutableStateOf(0) }
    var pontoIdx by remember { mutableStateOf(0) }
    var pagIdx by remember { mutableStateOf(0) }
    var erro by remember { mutableStateOf<String?>(null) }

    val entrega = modo == 0
    val zona = MockData.zonas[zonaIdx]
    val taxa = if (entrega) zona.taxa else 0.0
    val subtotal = vm.subtotal
    val faltaMinimo = entrega && subtotal < zona.pedidoMinimo

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
    ) {
        TopoComVoltar("Finalizar pedido", aoVoltar)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Seletor(listOf("Entrega", "Retirada"), modo, { modo = it; erro = null })
            Spacer(Modifier.height(8.dp))

            if (entrega) {
                CampoTexto(
                    valor = endereco,
                    aoMudar = { endereco = it },
                    rotulo = "Endereço (rua e número)",
                    dica = "Rua das Palmeiras, 120"
                )
                SeletorLista(
                    rotulo = "Zona de entrega",
                    opcoes = MockData.zonas.map { it.rotulo },
                    selecionada = zonaIdx,
                    aoSelecionar = { zonaIdx = it; erro = null }
                )
                if (zona.pedidoMinimo > 0) {
                    Text(
                        "Pedido mínimo nessa zona: ${zona.pedidoMinimo.reais()}",
                        fontSize = 12.sp,
                        color = if (faltaMinimo) Perigo else Muted,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }
            } else {
                SeletorLista(
                    rotulo = "Ponto de retirada",
                    opcoes = MockData.pontosRetirada,
                    selecionada = pontoIdx,
                    aoSelecionar = { pontoIdx = it }
                )
                Text(
                    "Retirada sem taxa.",
                    fontSize = 12.sp,
                    color = Muted,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )
            }

            Spacer(Modifier.height(14.dp))
            Text("Pagamento (simulado)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Muted)
            Spacer(Modifier.height(6.dp))
            Seletor(MockData.pagamentos, pagIdx, { pagIdx = it })

            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("Subtotal", modifier = Modifier.weight(1f))
                Text(subtotal.reais())
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(if (entrega) "Taxa da zona" else "Taxa", modifier = Modifier.weight(1f))
                Text(if (entrega) taxa.reais() else "Grátis")
            }
            LinhaSeparadora()
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text("Total", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                Text((subtotal + taxa).reais(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }

            erro?.let {
                Text(it, color = Perigo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
        }

        LinhaSeparadora()
        Column(
            Modifier
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            BotaoPrimario("Confirmar pedido", cor = Teal) {
                val destino =
                    if (entrega) "${endereco.trim()} – ${zona.nome}"
                    else "Retirada: ${MockData.pontosRetirada[pontoIdx]}"
                val resultado = vm.confirmarPedido(entrega, zona, destino, MockData.pagamentos[pagIdx])
                if (resultado == null) {
                    Toast.makeText(contexto, "Pedido confirmado!", Toast.LENGTH_SHORT).show()
                    aoConfirmar()
                } else {
                    erro = resultado
                }
            }
        }
    }
}

// =====================================================================
// PEDIDOS (STATUS DO PEDIDO DO CLIENTE)
// =====================================================================

@Composable
fun PedidosScreen(vm: CaisViewModel, aoVerPeixes: () -> Unit) {
    val lista = vm.meusPedidos
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Text(
            "Meus pedidos",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(16.dp)
        )
        if (lista.isEmpty()) {
            EstadoVazio(
                emoji = "🧾",
                titulo = "Você ainda não fez pedidos",
                subtitulo = "Quando comprar, o status aparece aqui.",
                textoBotao = "Ver peixes",
                aoClicar = aoVerPeixes
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(lista, key = { it.id }) { pedido ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Pedido #${pedido.id}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.height(6.dp))
                            Pilula(pedido.resumo)
                            Spacer(Modifier.height(4.dp))
                            Text(pedido.destino, fontSize = 12.sp, color = Muted)
                            Spacer(Modifier.height(14.dp))
                            LinhaDoTempo(pedido.passos, pedido.status)
                            Spacer(Modifier.height(12.dp))
                            LinhaSeparadora()
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth()) {
                                Text("Total (${pedido.pagamento})", modifier = Modifier.weight(1f), color = Muted)
                                Text(pedido.total.reais(), fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =====================================================================
// PERFIL
// =====================================================================

@Composable
fun PerfilScreen(vm: CaisViewModel, aoSair: () -> Unit) {
    val u = vm.usuario ?: return
    var confirmarExclusao by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Perfil",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(24.dp))
        Avatar(u.nome, 84.dp)
        Spacer(Modifier.height(12.dp))
        Text(u.nome, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(u.email, fontSize = 14.sp, color = Muted)
        Spacer(Modifier.height(8.dp))
        Pilula(if (u.ehVendedor) "Conta de vendedor" else "Conta de cliente")
        Spacer(Modifier.height(32.dp))
        BotaoPrimario("Sair") {
            vm.sair()
            aoSair()
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { confirmarExclusao = true }) {
            Text("Excluir minha conta", color = Perigo, fontWeight = FontWeight.SemiBold)
        }
    }

    if (confirmarExclusao) {
        AlertDialog(
            onDismissRequest = { confirmarExclusao = false },
            containerColor = Color.White,
            title = { Text("Excluir conta?", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Seus dados de acesso serão apagados. Essa ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarExclusao = false
                    vm.excluirConta()
                    aoSair()
                }) { Text("Excluir", color = Perigo, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarExclusao = false }) { Text("Cancelar", color = Muted) }
            }
        )
    }
}
