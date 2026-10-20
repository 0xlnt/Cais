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

// =====================================================================
// PAINEL DO VENDEDOR
// =====================================================================

@Composable
fun PainelVendedorScreen(vm: CaisViewModel, aoNovoProduto: () -> Unit) {
    var aba by remember { mutableStateOf(0) }
    val pedidos = vm.pedidosDoVendedor
    val produtos = vm.produtosDoVendedor

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
                            CartaoPedidoVendedor(p) { vm.avancarStatus(p.id) }
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
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FotoProduto(
                                    categoria = p.categoria,
                                    seed = p.id,
                                    foto = p.foto,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    tamanhoEmoji = 24.sp
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(p.nome, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                    Text(p.preco.reais() + "/kg • " + p.dataPesca, fontSize = 12.sp, color = Muted)
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
}

@Composable
private fun CartaoPedidoVendedor(pedido: Pedido, aoAvancar: () -> Unit) {
    val ultimo = pedido.status >= 3
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
            // foto: no MVP ainda é só um espaço reservado
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Ice)
                    .clickable {
                        Toast
                            .makeText(contexto, "Envio de foto entra na próxima versão", Toast.LENGTH_SHORT)
                            .show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Toque para adicionar a foto\n(em breve)",
                    color = Muted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
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
            BotaoPrimario("Salvar produto") {
                val resultado = vm.cadastrarProduto(nome, categoria, tipo, preco, data, disponivel)
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
