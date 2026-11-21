package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Pedido
import com.example.myapplication.data.reais
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//----Telas da Minha loja (vendedor)----
// Por enquanto só visuais: salvar mostra "Em breve".

@Composable
private fun RotuloDaSecao(texto: String) {
    Text(
        texto,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Navy,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

//----Dados da loja----

@Composable
fun DadosLojaScreen(vm: CaisViewModel, aoVoltar: () -> Unit) {
    val u = vm.usuario ?: return
    val emBreve = avisoEmBreve()
    DadosLojaConteudo(
        telefone = vm.telefoneDoUsuario,
        email = u.email,
        aoVoltar = aoVoltar,
        aoApresentacao = emBreve,
        aoSalvar = emBreve
    )
}

@Composable
fun DadosLojaConteudo(
    telefone: String,
    email: String,
    aoVoltar: () -> Unit,
    aoApresentacao: () -> Unit,
    aoSalvar: () -> Unit
) {
    // Nome da loja ainda não vem do banco
    var nomeDigitado by rememberSaveable { mutableStateOf("") }
    var telefoneDigitado by rememberSaveable { mutableStateOf(telefone) }
    var emailDigitado by rememberSaveable { mutableStateOf(email) }

    TelaDaConta(
        titulo = "Dados da loja",
        descricao = "Confira como sua loja aparece para os clientes.",
        aoVoltar = aoVoltar,
        rodape = { BotaoPrimario("Salvar alterações", onClick = aoSalvar) }
    ) {
        CampoDaConta("Nome da loja", nomeDigitado, { nomeDigitado = it }, dica = "Nome da loja")

        RotuloDaSecao("Contato")
        CampoDaConta(
            "Telefone", telefoneDigitado, { telefoneDigitado = it },
            teclado = KeyboardType.Phone, dica = "(92) 99999-9999"
        )
        CampoDaConta("E-mail", emailDigitado, { emailDigitado = it }, teclado = KeyboardType.Email)

        RotuloDaSecao("Apresentação")
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Ice)
        ) {
            LinhaDoCartao("Apresentação da loja", "Em breve", aoApresentacao)
        }
        Spacer(Modifier.height(20.dp))
    }
}

//----Horários de funcionamento----

private val DiasDaSemana = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")

@Composable
fun HorariosScreen(aoVoltar: () -> Unit) {
    HorariosConteudo(aoVoltar = aoVoltar, aoSalvar = avisoEmBreve())
}

@Composable
fun HorariosConteudo(aoVoltar: () -> Unit, aoSalvar: () -> Unit) {
    // Um caractere por dia (Dom..Sáb): '1' = aberta. Começa de segunda a sábado.
    var dias by rememberSaveable { mutableStateOf("0111111") }
    var abre by rememberSaveable { mutableStateOf("08:00") }
    var fecha by rememberSaveable { mutableStateOf("18:00") }

    TelaDaConta(
        titulo = "Horários",
        descricao = "Escolha os dias e o horário em que sua loja recebe pedidos.",
        aoVoltar = aoVoltar,
        rodape = { BotaoPrimario("Salvar alterações", onClick = aoSalvar) }
    ) {
        RotuloDaSecao("Dias de funcionamento")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DiasDaSemana.forEachIndexed { i, dia ->
                val marcado = dias[i] == '1'
                ChipDoDia(dia, marcado, Modifier.weight(1f)) {
                    dias = dias.substring(0, i) + (if (marcado) '0' else '1') + dias.substring(i + 1)
                }
            }
        }
        Spacer(Modifier.height(24.dp))

        RotuloDaSecao("Horário de funcionamento")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                CampoDaConta("Abre às", abre, { abre = it }, teclado = KeyboardType.Number, dica = "08:00")
            }
            Column(Modifier.weight(1f)) {
                CampoDaConta("Fecha às", fecha, { fecha = it }, teclado = KeyboardType.Number, dica = "18:00")
            }
        }

        val abertos = DiasDaSemana.filterIndexed { i, _ -> dias[i] == '1' }
        Text(
            if (abertos.isEmpty()) "Nenhum dia selecionado: a loja não recebe pedidos."
            else "Aberta ${abertos.joinToString(", ")}, das $abre às $fecha.",
            fontSize = 13.sp,
            color = Muted
        )
    }
}

@Composable
private fun ChipDoDia(dia: String, marcado: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (marcado) Navy else Color.White)
            .border(1.dp, if (marcado) Navy else Linha, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            dia,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (marcado) Color.White else Navy,
            textAlign = TextAlign.Center
        )
    }
}

//----Vendas e recebimentos----

@Composable
fun VendasScreen(vm: CaisViewModel, aoVoltar: () -> Unit) {
    // Só entram os pedidos que chegaram ao último passo ("Entregue")
    val entregues = vm.pedidosDoVendedor.filter { it.status >= it.passos.lastIndex }
    VendasConteudo(entregues = entregues, aoVoltar = aoVoltar)
}

@Composable
fun VendasConteudo(entregues: List<Pedido>, aoVoltar: () -> Unit) {
    TelaDaConta(
        titulo = "Vendas",
        descricao = "Histórico dos pedidos entregues e quanto você recebeu.",
        aoVoltar = aoVoltar,
        rodape = {}
    ) {
        if (entregues.isEmpty()) {
            EstadoVazio(
                "🧾",
                "Nenhuma venda ainda",
                "Ainda não houve pedidos entregues até o momento."
            )
        } else {
            ResumoDasVendas(entregues)
            Spacer(Modifier.height(20.dp))
            RotuloDaSecao("Histórico")
            entregues.sortedByDescending { it.criadoEm }.forEach { CartaoVenda(it) }
        }
    }
}

@Composable
private fun ResumoDasVendas(entregues: List<Pedido>) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Ice)
            .padding(16.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text("Pedidos entregues", fontSize = 12.sp, color = Muted)
            Text("${entregues.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Total recebido", fontSize = 12.sp, color = Muted)
            Text(
                entregues.sumOf { it.total }.reais(),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Navy
            )
        }
    }
}

@Composable
private fun CartaoVenda(p: Pedido) {
    Column(
        Modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Ice)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "#${p.id} · ${p.clienteNome}",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Navy,
                modifier = Modifier.weight(1f)
            )
            Pilula("Entregue")
        }
        Spacer(Modifier.height(6.dp))
        Text(p.resumo, fontSize = 13.sp, color = Muted)
        if (p.criadoEm > 0L) {
            Text(
                SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(p.criadoEm)),
                fontSize = 12.sp,
                color = Muted
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (p.entrega) "Entrega · ${p.pagamento}" else "Retirada · ${p.pagamento}",
                fontSize = 12.sp,
                color = Muted,
                modifier = Modifier.weight(1f)
            )
            Text(p.total.reais(), fontWeight = FontWeight.ExtraBold, color = Navy)
        }
    }
}

//----Previews----

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewDadosLoja() {
    DadosLojaConteudo("(92) 99999-1234", "vendedor@cais.com", {}, {}, {})
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewHorarios() {
    HorariosConteudo({}, {})
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewVendasVazio() {
    VendasConteudo(emptyList()) {}
}
