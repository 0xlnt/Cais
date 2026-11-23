package com.example.myapplication.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.example.myapplication.data.FaltaDeEstoque
import com.example.myapplication.data.emKg
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Categoria
import com.example.myapplication.data.Pedido
import com.example.myapplication.data.reais
import java.util.Calendar
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// =====================================================================
// PAINEL DO VENDEDOR
// =====================================================================

@Composable
fun PainelVendedorScreen(vm: CaisViewModel, aoNovoProduto: () -> Unit) {
    var aba by remember { mutableStateOf(vm.pegarAbaDoPainel()) }
    val pedidos = vm.pedidosDoVendedor
    val produtos = vm.produtosDoVendedor
    // produto cujo estoque está aberto na janela
    var estoqueDe by remember { mutableStateOf<Int?>(null) }
    // pedido acima do estoque esperando o vendedor confirmar
    var avisoDe by remember { mutableStateOf<Int?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Text(
            "Olá, ${vm.usuario?.nome ?: ""}",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
        )
        Text(
            "Veja os pedidos recebidos e o que você tem à venda.",
            fontSize = 13.sp,
            color = Muted,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        Spacer(Modifier.height(8.dp))
        Seletor(
            opcoes = listOf("Pedidos (${pedidos.size})", "Meus produtos (${produtos.size})"),
            selecionada = aba,
            aoSelecionar = { aba = it },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))

        Box(Modifier.weight(1f)) {
            if (aba == 0) {
                if (pedidos.isEmpty()) {
                    EstadoVazio("📭", "Nenhum pedido ainda", "Quando um cliente comprar de você, o pedido aparece aqui.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(pedidos, key = { it.id }) { p ->
                            val faltas = vm.faltasDeEstoque(p)
                            CartaoPedidoVendedor(p, acimaDoEstoque = faltas.isNotEmpty()) {
                                if (faltas.isNotEmpty()) avisoDe = p.id else vm.avancarStatus(p.id)
                            }
                        }
                    }
                }
            } else {
                if (produtos.isEmpty()) {
                    EstadoVazio("🐟", "Você ainda não cadastrou produtos", "Toque em \"Cadastrar produto\" para começar.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(produtos, key = { it.id }) { p ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Ice)
                                    .clickable { estoqueDe = p.id }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FotoProduto(
                                    categoria = p.categoria,
                                    seed = p.id,
                                    foto = p.foto,
                                    fotoUrl = p.fotoUrl,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    tamanhoEmoji = 24.sp
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(p.nome, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                    Text(p.preco.reais() + "/kg • " + p.dataPesca, fontSize = 12.sp, color = Muted)
                                    Spacer(Modifier.height(4.dp))
                                    EtiquetaEstoque(p.estoqueKg)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Switch(
                                        checked = p.disponivel,
                                        onCheckedChange = { vm.alternarDisponibilidade(p.id) },
                                        colors = SwitchDefaults.colors(checkedTrackColor = Teal)
                                    )
                                    Text(
                                        if (p.disponivel) "Disponível" else "Indisponível",
                                        fontSize = 10.sp,
                                        color = Muted
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            Modifier
                .padding(16.dp)
        ) {
            BotaoPrimario("+ Cadastrar produto", onClick = aoNovoProduto)
        }
    }

    pedidos.firstOrNull { it.id == avisoDe }?.let { p ->
        DialogoAcimaDoEstoque(
            pedidoId = p.id,
            faltas = vm.faltasDeEstoque(p),
            aoCancelar = { avisoDe = null },
            aoAceitar = {
                vm.avancarStatus(p.id)
                avisoDe = null
            }
        )
    }

    produtos.firstOrNull { it.id == estoqueDe }?.let { p ->
        DialogoEstoque(
            nome = p.nome,
            estoqueKg = p.estoqueKg,
            aoFechar = { estoqueDe = null },
            aoSalvar = { kg ->
                vm.mudarEstoque(p.id, kg)
                estoqueDe = null
            }
        )
    }
}

/** Estoque em destaque: verde com folga, amarelo acabando (menos de 5 kg), vermelho zerado. */
@Composable
private fun EtiquetaEstoque(kg: Double) {
    val (fundo, cor) = when {
        kg <= 0.0 -> Color(0xFFFDE4E4) to Perigo
        kg < 5.0 -> Color(0xFFFFF1D6) to Color(0xFF9A5B00)
        else -> Color(0xFFDDF3E8) to Verde
    }
    Text(
        if (kg <= 0.0) "Sem estoque" else "Estoque: ${kg.emKg()}",
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = cor,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fundo)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    )
}

/** Aviso antes de aceitar um pedido maior que o estoque; o vendedor pode aceitar mesmo assim. */
@Composable
private fun DialogoAcimaDoEstoque(
    pedidoId: Int,
    faltas: List<FaltaDeEstoque>,
    aoCancelar: () -> Unit,
    aoAceitar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = aoCancelar,
        containerColor = Color.White,
        title = { Text("Pedido acima do estoque", fontWeight = FontWeight.ExtraBold) },
        text = {
            Column {
                Text("O pedido #$pedidoId pede mais do que o app tem no seu estoque:")
                Spacer(Modifier.height(10.dp))
                faltas.forEach { f ->
                    Text(
                        "• ${f.nome}: pedido ${f.pedidoKg.emKg()}, estoque ${f.estoqueKg.emKg()}",
                        fontWeight = FontWeight.Bold,
                        color = Perigo
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "Se você tem mais do que o app mostra, pode aceitar mesmo assim. " +
                        "O estoque desses produtos vai ficar em 0 kg.",
                    fontSize = 13.sp,
                    color = Muted
                )
            }
        },
        confirmButton = {
            TextButton(onClick = aoAceitar) {
                Text("Aceitar mesmo assim", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = aoCancelar) { Text("Cancelar", color = Muted) }
        }
    )
}

/** Mostra o estoque do produto; muda com - e + (1 kg) ou digitando o número. */
@Composable
private fun DialogoEstoque(
    nome: String,
    estoqueKg: Double,
    aoFechar: () -> Unit,
    aoSalvar: (Double) -> Unit
) {
    fun formatar(kg: Double): String =
        if (kg % 1.0 == 0.0) kg.toLong().toString() else String.format(Locale("pt", "BR"), "%.1f", kg)

    var texto by remember { mutableStateOf(formatar(estoqueKg)) }
    val valor = texto.replace(',', '.').toDoubleOrNull()

    AlertDialog(
        onDismissRequest = aoFechar,
        containerColor = Color.White,
        title = { Text(nome, fontWeight = FontWeight.ExtraBold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Em estoque agora: ${estoqueKg.emKg()}",
                    fontSize = 13.sp,
                    color = Muted
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BotaoDoEstoque("−", ativo = (valor ?: 0.0) > 0.0) {
                        texto = formatar(((valor ?: 0.0) - 1.0).coerceAtLeast(0.0))
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedTextField(
                        value = texto,
                        onValueChange = { novo ->
                            if (novo.all { it.isDigit() || it == ',' || it == '.' }) texto = novo
                        },
                        singleLine = true,
                        isError = valor == null,
                        suffix = { Text("kg", color = Muted) },
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.width(120.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    BotaoDoEstoque("+", ativo = true) {
                        texto = formatar((valor ?: 0.0) + 1.0)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { valor?.let(aoSalvar) }, enabled = valor != null) {
                Text("Salvar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = aoFechar) { Text("Cancelar", color = Muted) }
        }
    )
}

@Composable
private fun BotaoDoEstoque(texto: String, ativo: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (ativo) Navy else Ice)
            .clickable(enabled = ativo, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            texto,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (ativo) Color.White else Muted
        )
    }
}

@Composable
private fun CartaoPedidoVendedor(pedido: Pedido, acimaDoEstoque: Boolean, aoAvancar: () -> Unit) {
    val ultimo = pedido.status >= pedido.passos.lastIndex
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Ice)
            .padding(14.dp)
    ) {
        Text("#${pedido.id} · ${pedido.clienteNome}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        Text(pedido.resumo, fontSize = 13.sp, color = Muted)
        Text(pedido.destino, fontSize = 12.sp, color = Muted)
        if (acimaDoEstoque) {
            Text(
                "⚠ Pedido acima do estoque",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Perigo,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pilula(pedido.passos[pedido.status])
            Spacer(Modifier.weight(1f))
            Text(pedido.total.reais(), fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(8.dp))
        BotaoPequeno(
            texto = if (ultimo) "Entregue ✓" else "Mudar para: " + pedido.passos[pedido.status + 1],
            cor = if (ultimo) Verde else Teal,
            ativo = !ultimo,
            onClick = aoAvancar
        )
    }
}

// =====================================================================
// CADASTRO DE PRODUTO
// =====================================================================

@Composable
fun NovoProdutoScreen(vm: CaisViewModel, aoVoltar: () -> Unit) {
    val contexto = LocalContext.current

    fun hoje(): String {
        val c = Calendar.getInstance()
        return "%02d/%02d/%04d".format(c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR))
    }

    var nome by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(Categoria.PEIXE) }
    var tipo by remember { mutableStateOf("") }
    var preco by remember { mutableStateOf("") }
    var data by remember { mutableStateOf(hoje()) }
    var disponivel by remember { mutableStateOf(true) }
    var erro by remember { mutableStateOf<String?>(null) }
    var salvando by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()

    // foto escolhida (da galeria) ou tirada (câmera); fica guardada se a tela for recriada
    var fotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var uriDaCamera by rememberSaveable { mutableStateOf<Uri?>(null) }

    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) fotoUri = uri
    }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tirou ->
        if (tirou) fotoUri = uriDaCamera
    }

    fun abrirCamera() {
        val pasta = File(contexto.cacheDir, "fotos").apply { mkdirs() }
        val arquivo = File(pasta, "produto_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", arquivo)
        uriDaCamera = uri
        camera.launch(uri)
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = aoVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
            }
            Text("Novo produto", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // foto do produto: tirar na hora (câmera) ou escolher da galeria
            val fotoEscolhida = fotoUri
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Ice),
                contentAlignment = Alignment.Center
            ) {
                if (fotoEscolhida != null) {
                    ImagemLocal(fotoEscolhida, Modifier.fillMaxSize())
                } else {
                    Text(
                        "Adicione uma foto do produto",
                        color = Muted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipFiltro("Tirar foto", false) { abrirCamera() }
                ChipFiltro("Escolher da galeria", false) {
                    galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                if (fotoEscolhida != null) {
                    ChipFiltro("Remover", false) { fotoUri = null }
                }
            }
            Spacer(Modifier.height(8.dp))

            CampoTexto(nome, { nome = it }, "Nome do peixe", dica = "Ex.: Tambaqui")

            Text(
                "Categoria",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Muted,
                modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 6.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Categoria.entries.toList()) { c ->
                    ChipFiltro(c.rotulo, categoria == c) { categoria = c }
                }
            }
            Spacer(Modifier.height(6.dp))

            CampoTexto(tipo, { tipo = it }, "Tipo (opcional)", dica = "Ex.: Peixe de rio")
            CampoTexto(
                preco, { preco = it }, "Preço por kg (R$)",
                dica = "38,90", teclado = KeyboardType.Decimal
            )

            CampoClicavel("Data da pesca", data) {
                val c = Calendar.getInstance()
                DatePickerDialog(
                    contexto,
                    { _, ano, mes, dia -> data = "%02d/%02d/%04d".format(dia, mes + 1, ano) },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Disponível", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Seletor(
                    opcoes = listOf("Sim", "Não"),
                    selecionada = if (disponivel) 0 else 1,
                    aoSelecionar = { disponivel = it == 0 },
                    modifier = Modifier.width(150.dp)
                )
            }

            erro?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Perigo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
        }

        Column(
            Modifier
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            BotaoPrimario(if (salvando) "Salvando..." else "Salvar produto", ativo = !salvando) {
                escopo.launch {
                    salvando = true
                    erro = null
                    val uri = fotoUri
                    // reduz a foto fora da tela principal, para não travar o app
                    val bytes = uri?.let { withContext(Dispatchers.IO) { FotoUtil.jpeg(contexto, it) } }
                    val resultado = if (uri != null && bytes == null) {
                        "Não consegui ler a foto. Escolha ou tire outra."
                    } else {
                        vm.cadastrarProduto(nome, categoria, tipo, preco, data, disponivel, bytes)
                    }
                    salvando = false
                    if (resultado == null) {
                        Toast.makeText(contexto, "Produto salvo!", Toast.LENGTH_SHORT).show()
                        aoVoltar()
                    } else {
                        erro = resultado
                    }
                }
            }
        }
    }
}
