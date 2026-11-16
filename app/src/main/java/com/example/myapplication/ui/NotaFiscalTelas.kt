package com.example.myapplication.ui

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =====================================================================
// NOTA FISCAL (acesso digital) e DANFE NFC-e (cupom de 58 mm e de 80 mm)
// Tudo é demonstração: "SEM VALOR FISCAL", como no modelo do Figma.
// =====================================================================

private val FundoNota = Color(0xFFF3F4FA)
private val Roxo = Color(0xFF5B4BDB)
private val AmareloNota = Color(0xFFFFF4D6)
private val TextoAmarelo = Color(0xFF8A5A00)
private val Tinta = Color(0xFF111111)

// ---------------------------------------------------------------------
// Tela "Nota fiscal"
// ---------------------------------------------------------------------

@Composable
fun NotaFiscalScreen(
    dados: NotaFiscalDados,
    aoVoltar: () -> Unit,
    aoVerDanfe: () -> Unit
) {
    val contexto = LocalContext.current
    fun ilustrativo() {
        Toast.makeText(contexto, "Ação ilustrativa: modelo de demonstração.", Toast.LENGTH_SHORT).show()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(FundoNota)
    ) {
        // cabeçalho azul
        Column(
            Modifier
                .fillMaxWidth()
                .background(Navy)
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 22.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Voltar",
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier
                        .clickable(onClick = aoVoltar)
                        .padding(vertical = 4.dp)
                )
                Spacer(Modifier.weight(1f))
                Text("Pedido #${dados.pedidoId}", fontSize = 12.sp, color = Color.White)
            }
            Spacer(Modifier.height(16.dp))
            Text("Nota fiscal", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text("Seu documento, sempre à mão.", fontSize = 13.sp, color = Color(0xFFC9D9EC))
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // aviso amarelo
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AmareloNota)
                    .padding(12.dp)
            ) {
                Text("PRÉVIA • SEM VALOR FISCAL", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = TextoAmarelo)
                Text("Dados fictícios. Esta nota ainda não foi emitida.", fontSize = 11.sp, color = TextoAmarelo)
            }

            Spacer(Modifier.height(12.dp))

            // cartão da loja e total
            CartaoBranco {
                Text(dados.loja.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Muted)
                Text(
                    "CNPJ: ${dados.cnpj} · IE: ${dados.inscricaoEstadual} · Manaus/AM",
                    fontSize = 10.sp,
                    color = Muted
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "Uma compra fresquinha.\nTudo documentado.",
                    fontSize = 22.sp,
                    lineHeight = 27.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Navy
                )
                Spacer(Modifier.height(14.dp))
                Text("Total da compra", fontSize = 11.sp, color = Muted)
                Text("R$ ${dados.totalPago.numero()}", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
                Spacer(Modifier.height(12.dp))
                LinhaSeparadora()
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text(dataHoraCurta(dados.dataHora), fontSize = 11.sp, color = Muted, modifier = Modifier.weight(1f))
                    Text(dados.pagamento, fontSize = 11.sp, color = Muted)
                }
            }

            TituloDaSecao("Itens da compra")
            CartaoBranco {
                dados.itens.forEach { item ->
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            item.descricao,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Navy,
                            modifier = Modifier.weight(1f)
                        )
                        Text("R$ ${item.total.numero()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Navy)
                    }
                    Text(
                        "${item.kg.kgNota()} × R$ ${item.precoKg.numero()}/kg",
                        fontSize = 11.sp,
                        color = Muted
                    )
                    Spacer(Modifier.height(14.dp))
                }
                LinhaSeparadora()
                Spacer(Modifier.height(10.dp))
                LinhaDeValor("Subtotal", "R$ ${dados.subtotal.numero()}")
                if (dados.desconto > 0) {
                    LinhaDeValor("Desconto", "− R$ ${dados.desconto.numero()}", cor = Roxo)
                }
                if (dados.taxaEntrega > 0) {
                    LinhaDeValor("Taxa de entrega", "R$ ${dados.taxaEntrega.numero()}")
                }
                Spacer(Modifier.height(4.dp))
                LinhaDeValor("Total pago", "R$ ${dados.totalPago.numero()}", forte = true)
            }

            TituloDaSecao("Documento fiscal")
            CartaoBranco {
                LinhaDeDado("Tipo", "NFC-e · Modelo 65")
                LinhaDeDado("Número / Série", "${dados.numero} / ${dados.serie}")
                LinhaDeDado("Consumidor", dados.consumidor)
                LinhaDeDado("CPF", dados.cpf)
                LinhaDeDado("Situação", "Demonstração")
                Spacer(Modifier.height(4.dp))
                LinhaSeparadora()
                Spacer(Modifier.height(10.dp))
                Text(
                    if (dados.entrega != null) "Entrega" else "Retirada",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy
                )
                Text(dados.entrega ?: "Retirada com o vendedor", fontSize = 12.sp, color = Muted)
                Spacer(Modifier.height(10.dp))
                LinhaSeparadora()
                Spacer(Modifier.height(10.dp))
                Text(
                    "DANFE em formato de cupom de 80 mm. Chave, protocolo e QR Code serão preenchidos pela emissão fiscal.",
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    color = Muted
                )
            }

            Spacer(Modifier.height(16.dp))
            BotaoPrimario("Visualizar DANFE • 80 mm", onClick = aoVerDanfe)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BotaoContorno("Baixar PDF", Modifier.weight(1f), ::ilustrativo)
                BotaoContorno("Compartilhar", Modifier.weight(1f), ::ilustrativo)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Ações ilustrativas para o documento emitido.",
                fontSize = 11.sp,
                color = Muted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(18.dp))
            Text("Precisa de ajuda com a nota?", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
            Text("Fale com o estabelecimento responsável pela emissão.", fontSize = 12.sp, color = Muted)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CartaoBranco(conteudo: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) { conteudo() }
}

@Composable
private fun TituloDaSecao(texto: String) {
    Text(
        texto,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Navy,
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun LinhaDeValor(rotulo: String, valor: String, cor: Color = Navy, forte: Boolean = false) {
    Row(Modifier
        .fillMaxWidth()
        .padding(vertical = 3.dp)) {
        Text(
            rotulo,
            fontSize = if (forte) 14.sp else 12.sp,
            fontWeight = if (forte) FontWeight.ExtraBold else FontWeight.Normal,
            color = if (cor == Navy) (if (forte) Navy else Muted) else cor,
            modifier = Modifier.weight(1f)
        )
        Text(
            valor,
            fontSize = if (forte) 14.sp else 12.sp,
            fontWeight = if (forte) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = cor
        )
    }
}

@Composable
private fun LinhaDeDado(rotulo: String, valor: String) {
    Row(Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Text(rotulo, fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
        Text(valor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Navy)
    }
}

@Composable
private fun BotaoContorno(texto: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Linha, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
    }
}

// ---------------------------------------------------------------------
// Tela do DANFE (mostra o cupom de 58 mm ou de 80 mm)
// ---------------------------------------------------------------------

@Composable
fun DanfeScreen(
    dados: NotaFiscalDados,
    larguraInicial: Int,
    aoVoltar: () -> Unit
) {
    var largura by remember { mutableStateOf(if (larguraInicial == 58) 58 else 80) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Pagina)
            .statusBarsPadding()
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
            Text("DANFE NFC-e • $largura mm", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        }
        Seletor(
            opcoes = listOf("58 mm", "80 mm"),
            selecionada = if (largura == 58) 0 else 1,
            aoSelecionar = { largura = if (it == 0) 58 else 80 },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // o cupom de 58 mm é mais estreito que o de 80 mm (58/80 da largura)
            CupomDanfe(
                dados = dados,
                mm = largura,
                modifier = Modifier.fillMaxWidth(if (largura == 58) 0.725f else 1f)
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

/** O cupom em si: papel branco, texto preto, linhas tracejadas. */
@Composable
fun CupomDanfe(dados: NotaFiscalDados, mm: Int, modifier: Modifier = Modifier) {
    val estreito = mm == 58
    val k = if (estreito) 0.92f else 1f
    val corpo = (10.5f * k).sp
    val pequeno = (9f * k).sp
    val destaque = (11.5f * k).sp

    Column(
        modifier
            .background(Color.White)
            .padding(horizontal = if (estreito) 14.dp else 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CTexto("SEM VALOR FISCAL", destaque, FontWeight.ExtraBold)
        CTexto("MODELO DEMONSTRATIVO", pequeno)
        CTexto("DADOS FICTÍCIOS", pequeno)
        Spacer(Modifier.height(10.dp))

        CTexto(dados.loja.uppercase(), (14f * k).sp, FontWeight.ExtraBold)
        CTexto("Estabelecimento de exemplo", pequeno)
        if (estreito) {
            CTexto("CNPJ: ${dados.cnpj}", pequeno)
            CTexto("IE: ${dados.inscricaoEstadual}", pequeno)
        } else {
            CTexto("CNPJ: ${dados.cnpj} | IE: ${dados.inscricaoEstadual}", pequeno)
        }
        CTexto(dados.enderecoLoja, pequeno)
        CTexto(dados.cidadeLoja, pequeno)
        Spacer(Modifier.height(8.dp))

        CTexto("DANFE NFC-e", destaque, FontWeight.ExtraBold)
        CTexto("Documento Auxiliar da Nota Fiscal de Consumidor Eletrônica • Modelo 65", pequeno)
        CTexto("Não permite aproveitamento de crédito de ICMS", pequeno)
        Tracejado()

        CEsquerda("CÓDIGO / DESCRIÇÃO", pequeno, FontWeight.Bold)
        CLinha("QTD. × VL. UNIT. (R$)", "TOTAL (R$)", pequeno, FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        dados.itens.forEach { item ->
            CEsquerda("${item.codigo}  ${item.descricao}", corpo, FontWeight.Bold)
            CLinha("${item.kg.kgNota()} × ${item.precoKg.numero()}", item.total.numero(), corpo)
            Spacer(Modifier.height(4.dp))
        }
        Tracejado()

        CLinha("Qtd. total de itens", "${dados.itens.size}", corpo)
        CLinha("Valor total R$", dados.subtotal.numero(), corpo)
        CLinha("Desconto R$", dados.desconto.numero(), corpo)
        if (dados.taxaEntrega > 0) {
            CLinha("Taxa de entrega R$", dados.taxaEntrega.numero(), corpo)
        }
        Spacer(Modifier.height(4.dp))
        if (estreito) {
            CEsquerda("VALOR A PAGAR R$", corpo, FontWeight.ExtraBold)
            CDireita("R$ ${dados.totalPago.numero()}", (16f * k).sp, FontWeight.ExtraBold)
        } else {
            CLinha("VALOR A PAGAR R$", "R$ ${dados.totalPago.numero()}", (14f * k).sp, FontWeight.ExtraBold)
        }
        Tracejado()

        CLinha("FORMA DE PAGAMENTO", "VALOR PAGO R$", pequeno, FontWeight.Bold)
        CLinha(dados.pagamento, dados.totalPago.numero(), corpo)
        CLinha("Troco R$", dados.troco.numero(), corpo)
        Tracejado()

        CTexto("CONSUMIDOR: ${dados.consumidor}", corpo, FontWeight.Bold)
        CTexto("CPF: ${dados.cpf}", corpo, FontWeight.Bold)
        CTexto(
            if (dados.entrega != null) "Entrega: ${dados.entrega}" else "Retirada na loja",
            corpo,
            FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        CTexto("NFC-e nº ${dados.numero} • Série ${dados.serie}", pequeno)
        CTexto("${dados.dataHora.let { dataHoraNota(it) }} • Dados de exemplo", pequeno)
        Spacer(Modifier.height(6.dp))
        CTexto("Consulte pela chave de acesso em:", pequeno)
        CTexto("[URL oficial da SEFAZ da UF emitente]", pequeno)
        Spacer(Modifier.height(6.dp))
        CTexto("CHAVE DE ACESSO", pequeno, FontWeight.Bold)
        CTexto("[44 dígitos gerados na emissão]", pequeno)
        Spacer(Modifier.height(6.dp))
        CTexto("QR CODE FISCAL", pequeno, FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        QuadroDoQrCode(pequeno)
        Spacer(Modifier.height(6.dp))
        CTexto("Protocolo de autorização:", pequeno)
        CTexto("[Preenchido após autorização da SEFAZ]", pequeno)
        Tracejado()

        CTexto("INFORMAÇÕES ADICIONAIS", pequeno, FontWeight.Bold)
        CTexto(
            "Tributos aproximados: [valor e fonte a informar na emissão, quando aplicável].",
            pequeno
        )
        CTexto("Pedido #${dados.pedidoId} • Referência do aplicativo", pequeno)
        Tracejado()

        CTexto("PRÉVIA DE LAYOUT", pequeno, FontWeight.ExtraBold)
        CTexto("NÃO É NOTA EMITIDA", pequeno, FontWeight.ExtraBold)
        Spacer(Modifier.height(4.dp))
        CTexto(
            if (estreito) "58 mm • margens laterais de 5 mm" else "80 mm • margens laterais de 4 mm",
            pequeno
        )
        CTexto("Dados fiscais e QR Code dependem da emissão.", pequeno)
    }
}

@Composable
private fun CTexto(texto: String, tamanho: TextUnit, peso: FontWeight = FontWeight.Normal) {
    Text(
        texto,
        fontSize = tamanho,
        fontWeight = peso,
        color = Tinta,
        textAlign = TextAlign.Center,
        lineHeight = (tamanho.value * 1.3f).sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CEsquerda(texto: String, tamanho: TextUnit, peso: FontWeight = FontWeight.Normal) {
    Text(
        texto,
        fontSize = tamanho,
        fontWeight = peso,
        color = Tinta,
        lineHeight = (tamanho.value * 1.3f).sp,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CDireita(texto: String, tamanho: TextUnit, peso: FontWeight = FontWeight.Normal) {
    Text(
        texto,
        fontSize = tamanho,
        fontWeight = peso,
        color = Tinta,
        textAlign = TextAlign.End,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun CLinha(esquerda: String, direita: String, tamanho: TextUnit, peso: FontWeight = FontWeight.Normal) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            esquerda,
            fontSize = tamanho,
            fontWeight = peso,
            color = Tinta,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(6.dp))
        Text(direita, fontSize = tamanho, fontWeight = peso, color = Tinta, textAlign = TextAlign.End)
    }
}

@Composable
private fun Tracejado() {
    Canvas(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(9.dp)
    ) {
        drawLine(
            color = Tinta,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
        )
    }
}

/** Quadrado tracejado "Inserido pela emissão" no lugar do QR Code. */
@Composable
private fun QuadroDoQrCode(tamanhoTexto: TextUnit) {
    Box(Modifier.height(86.dp).width(86.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                color = Tinta,
                style = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                )
            )
        }
        Text(
            "Inserido pela\nemissão",
            fontSize = tamanhoTexto,
            color = Tinta,
            textAlign = TextAlign.Center,
            lineHeight = (tamanhoTexto.value * 1.3f).sp
        )
    }
}

// ---------------------------------------------------------------------
// Previews (Android Studio): mostram os dados exatos do modelo do Figma
// ---------------------------------------------------------------------

@Preview(showBackground = true, widthDp = 390, heightDp = 1250)
@Composable
private fun PreviewNotaFiscal() {
    NotaFiscalScreen(NotaDoFigma, aoVoltar = {}, aoVerDanfe = {})
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun PreviewDanfe80() {
    DanfeScreen(NotaDoFigma, larguraInicial = 80, aoVoltar = {})
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun PreviewDanfe58() {
    DanfeScreen(NotaDoFigma, larguraInicial = 58, aoVoltar = {})
}
