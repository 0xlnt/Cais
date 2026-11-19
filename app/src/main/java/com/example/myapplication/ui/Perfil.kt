package com.example.myapplication.ui

import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

//----Perfil do cliente e da loja----

private val FundoCartao = Ice
private val VerdeClaro = Color(0xFFE3F4EA)
private val VerdeBorda = Color(0xFFB9E2CB)
private val AmareloClaro = Color(0xFFFFF4D6)
private val AmareloBorda = Color(0xFFF2DDA0)

enum class ItemPerfilCliente {
    CONVERSAS, NOTIFICACOES, DADOS_PESSOAIS, ENDERECOS, FAVORITOS, AVALIACOES,
    PAGAMENTOS, SEGURANCA, AJUDA
}

enum class ItemPerfilLoja {
    EDITAR_LOJA, VER_LOJA, PAUSAR, CONVERSAS, NOTIFICACOES,
    DADOS_LOJA, HORARIOS, REPUTACAO, DOCUMENTOS,
    PRODUTOS, PREPARO,
    ENDERECO_LOJA, DELIVERY,
    VENDAS, RECEBIMENTO,
    SEGURANCA, AJUDA, SAIR
}

private enum class DialogoPerfil { NENHUM, SEGURANCA, EXCLUIR }

/** Aba Perfil: loja para vendedor, "Meu perfil" para cliente. */
@Composable
fun PerfilScreen(
    vm: CaisViewModel,
    aoSair: () -> Unit,
    aoPainel: () -> Unit,
    aoDadosPessoais: () -> Unit,
    aoEnderecos: () -> Unit,
    aoSeguranca: () -> Unit
) {
    val u = vm.usuario ?: return
    val contexto = LocalContext.current
    var dialogo by remember { mutableStateOf(DialogoPerfil.NENHUM) }
    var lojaAberta by remember { mutableStateOf(true) }

    fun emBreve() {
        Toast.makeText(contexto, "Em breve", Toast.LENGTH_SHORT).show()
    }

    if (u.ehVendedor) {
        PerfilLojaConteudo(nome = u.nome, email = u.email, lojaAberta = lojaAberta) { item ->
            when (item) {
                ItemPerfilLoja.PAUSAR -> lojaAberta = !lojaAberta
                ItemPerfilLoja.PRODUTOS, ItemPerfilLoja.VENDAS -> aoPainel()
                ItemPerfilLoja.SEGURANCA -> dialogo = DialogoPerfil.SEGURANCA
                ItemPerfilLoja.SAIR -> {
                    vm.sair()
                    aoSair()
                }
                else -> emBreve()
            }
        }
    } else {
        PerfilClienteConteudo(nome = u.nome, email = u.email) { item ->
            when (item) {
                ItemPerfilCliente.DADOS_PESSOAIS -> aoDadosPessoais()
                ItemPerfilCliente.ENDERECOS -> aoEnderecos()
                ItemPerfilCliente.SEGURANCA -> aoSeguranca()
                else -> emBreve()
            }
        }
    }

    if (dialogo == DialogoPerfil.SEGURANCA) {
        AlertDialog(
            onDismissRequest = { dialogo = DialogoPerfil.NENHUM },
            containerColor = Color.White,
            title = { Text("Segurança e privacidade", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Senha, privacidade e exclusão da conta.") },
            confirmButton = {
                // Desligado. Para ligar: troque emBreve() por dialogo = DialogoPerfil.EXCLUIR
                TextButton(onClick = { emBreve() }) {
                    Text("Excluir minha conta", color = Perigo, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogo = DialogoPerfil.NENHUM }) { Text("Fechar", color = Muted) }
            }
        )
    }

    if (dialogo == DialogoPerfil.EXCLUIR) {
        DialogoExcluirConta(aoCancelar = { dialogo = DialogoPerfil.NENHUM }) {
            dialogo = DialogoPerfil.NENHUM
            vm.excluirConta()
            aoSair()
        }
    }
}

/** Confirmação antes de excluir a conta. */
@Composable
internal fun DialogoExcluirConta(aoCancelar: () -> Unit, aoConfirmar: () -> Unit) {
    AlertDialog(
        onDismissRequest = aoCancelar,
        containerColor = Color.White,
        title = { Text("Excluir conta?", fontWeight = FontWeight.ExtraBold) },
        text = { Text("Seus dados de acesso serão apagados. Essa ação não pode ser desfeita.") },
        confirmButton = {
            TextButton(onClick = aoConfirmar) {
                Text("Excluir", color = Perigo, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = aoCancelar) { Text("Cancelar", color = Muted) }
        }
    )
}

//----Meu perfil (cliente)----

@Composable
fun PerfilClienteConteudo(
    nome: String,
    email: String,
    contaAbertaNoInicio: Boolean = false,
    aoAbrir: (ItemPerfilCliente) -> Unit
) {
    // "Minha conta" começa fechada
    var contaAberta by rememberSaveable { mutableStateOf(contaAbertaNoInicio) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        CabecalhoDaTela("Meu perfil")
        Spacer(Modifier.height(16.dp))
        CabecalhoDoUsuario(nome, email, textoEditar = null, tipoDaConta = "Conta de cliente")

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CartaoAtalho(
                Icons.Outlined.ChatBubbleOutline, "Conversas", "Fale com os vendedores",
                Modifier.weight(1f)
            ) { aoAbrir(ItemPerfilCliente.CONVERSAS) }
            CartaoAtalho(
                Icons.Outlined.NotificationsNone, "Notificações", "Acompanhe as novidades",
                Modifier.weight(1f)
            ) { aoAbrir(ItemPerfilCliente.NOTIFICACOES) }
        }

        Spacer(Modifier.height(18.dp))
        CartaoMinhaConta(
            aberta = contaAberta,
            aoAlternar = { contaAberta = !contaAberta },
            aoAbrir = aoAbrir
        )

        TituloDoGrupo("Pagamentos")
        LinhaDeMenu(Icons.Outlined.CreditCard, "Formas de pagamento", "Gerencie suas formas de pagar") {
            aoAbrir(ItemPerfilCliente.PAGAMENTOS)
        }

        TituloDoGrupo("Configurações")
        LinhaDeMenu(
            Icons.Outlined.VerifiedUser, "Segurança e privacidade",
            "Senha, termos de uso e privacidade"
        ) { aoAbrir(ItemPerfilCliente.SEGURANCA) }

        TituloDoGrupo("Ajuda")
        LinhaDeMenu(
            Icons.AutoMirrored.Outlined.HelpOutline, "Central de ajuda",
            "Pedidos, reembolsos e fale com o Cais"
        ) { aoAbrir(ItemPerfilCliente.AJUDA) }

        Spacer(Modifier.height(16.dp))
    }
}

/** Cartão "Minha conta", que abre e fecha. */
@Composable
private fun CartaoMinhaConta(
    aberta: Boolean,
    aoAlternar: () -> Unit,
    aoAbrir: (ItemPerfilCliente) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FundoCartao)
            .animateContentSize()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = aoAlternar)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.PersonOutline, contentDescription = null, tint = Navy, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Minha conta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
                Text("Dados pessoais, endereços e mais", fontSize = 11.sp, color = Muted)
            }
            Icon(
                if (aberta) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (aberta) "Recolher Minha conta" else "Expandir Minha conta",
                tint = Navy,
                modifier = Modifier.size(22.dp)
            )
        }

        if (aberta) {
            DivisorDoCartao()
            LinhaDoCartao("Dados pessoais", "Nome, telefone e e-mail") {
                aoAbrir(ItemPerfilCliente.DADOS_PESSOAIS)
            }
            DivisorDoCartao()
            LinhaDoCartao("Endereços", "Gerencie seus locais de entrega") {
                aoAbrir(ItemPerfilCliente.ENDERECOS)
            }
            DivisorDoCartao()
            LinhaDoCartao("Favoritos", "Seus produtos e vendedores salvos") {
                aoAbrir(ItemPerfilCliente.FAVORITOS)
            }
            DivisorDoCartao()
            LinhaDoCartao("Minhas avaliações", "Veja ou avalie suas compras") {
                aoAbrir(ItemPerfilCliente.AVALIACOES)
            }
        }
    }
}

//----Perfil da loja (vendedor)----

@Composable
fun PerfilLojaConteudo(
    nome: String,
    email: String,
    lojaAberta: Boolean,
    aoAbrir: (ItemPerfilLoja) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        CabecalhoDaTela("Perfil da loja")
        Spacer(Modifier.height(16.dp))
        CabecalhoDoUsuario(nome, email, "Editar dados da loja →", "Conta de vendedor") {
            aoAbrir(ItemPerfilLoja.EDITAR_LOJA)
        }

        Spacer(Modifier.height(14.dp))
        CartaoStatusDaLoja(lojaAberta, aoVerLoja = { aoAbrir(ItemPerfilLoja.VER_LOJA) }) {
            aoAbrir(ItemPerfilLoja.PAUSAR)
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CartaoAtalho(
                Icons.Outlined.ChatBubbleOutline, "Conversas", "Fale com os clientes",
                Modifier.weight(1f)
            ) { aoAbrir(ItemPerfilLoja.CONVERSAS) }
            CartaoAtalho(
                Icons.Outlined.NotificationsNone, "Notificações", "Acompanhe as novidades",
                Modifier.weight(1f)
            ) { aoAbrir(ItemPerfilLoja.NOTIFICACOES) }
        }

        TituloDoGrupo("Minha loja")
        LinhaDeMenu(Icons.Outlined.Storefront, "Dados da loja", "Nome, contato e apresentação") {
            aoAbrir(ItemPerfilLoja.DADOS_LOJA)
        }
        LinhaDeMenu(Icons.Outlined.Schedule, "Horários de funcionamento", "Dias e horários para receber pedidos") {
            aoAbrir(ItemPerfilLoja.HORARIOS)
        }
        LinhaDeMenu(Icons.Outlined.StarBorder, "Reputação e avaliações", "Veja o que seus clientes estão dizendo") {
            aoAbrir(ItemPerfilLoja.REPUTACAO)
        }
        LinhaDeMenu(Icons.Outlined.VerifiedUser, "Documentos e verificações", "Cadastro e documentação da loja") {
            aoAbrir(ItemPerfilLoja.DOCUMENTOS)
        }

        TituloDoGrupo("Catálogo")
        LinhaDeMenu(Icons.Outlined.Inventory2, "Produtos e estoque", "Cadastre itens, preços e disponibilidade") {
            aoAbrir(ItemPerfilLoja.PRODUTOS)
        }
        LinhaDeMenu(Icons.Outlined.Tune, "Opções de preparo", "Inteiro, limpo, em postas ou filé") {
            aoAbrir(ItemPerfilLoja.PREPARO)
        }

        TituloDoGrupo("Entrega e retirada")
        LinhaDeMenu(Icons.Outlined.LocationOn, "Endereço e retirada na loja", "Local e orientações para buscar o pedido") {
            aoAbrir(ItemPerfilLoja.ENDERECO_LOJA)
        }
        LinhaDeMenu(Icons.Outlined.LocalShipping, "Delivery", "Entregador da loja ou serviço parceiro") {
            aoAbrir(ItemPerfilLoja.DELIVERY)
        }

        TituloDoGrupo("Financeiro")
        LinhaDeMenu(Icons.Outlined.BarChart, "Vendas e recebimentos", "Histórico, ganhos e valores a receber") {
            aoAbrir(ItemPerfilLoja.VENDAS)
        }
        LinhaDeMenu(Icons.Outlined.AccountBalance, "Dados de recebimento", "Gerencie a conta para seus repasses") {
            aoAbrir(ItemPerfilLoja.RECEBIMENTO)
        }

        TituloDoGrupo("Conta e suporte")
        LinhaDeMenu(
            Icons.Outlined.VerifiedUser, "Segurança e privacidade",
            "Acesso, privacidade e exclusão da conta"
        ) { aoAbrir(ItemPerfilLoja.SEGURANCA) }
        LinhaDeMenu(
            Icons.AutoMirrored.Outlined.HelpOutline, "Central de ajuda",
            "Suporte para você e sua loja"
        ) { aoAbrir(ItemPerfilLoja.AJUDA) }

        Spacer(Modifier.height(16.dp))
        BotaoSairDaConta { aoAbrir(ItemPerfilLoja.SAIR) }
        Spacer(Modifier.height(16.dp))
    }
}

//----Peças comuns----

@Composable
private fun CabecalhoDaTela(titulo: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            titulo,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Navy,
            modifier = Modifier.weight(1f)
        )
        // "Cais•" pequeno
        Row(verticalAlignment = Alignment.Bottom) {
            Text("Cais", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
            Box(
                Modifier
                    .padding(start = 2.dp, bottom = 5.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(Teal)
            )
        }
    }
}

@Composable
private fun CabecalhoDoUsuario(
    nome: String,
    email: String,
    textoEditar: String?,
    tipoDaConta: String,
    aoEditar: () -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(nome, 60.dp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(nome, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
            Text(email, fontSize = 12.sp, color = Muted)
            if (textoEditar != null) {
                Text(
                    textoEditar,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Navy,
                    modifier = Modifier
                        .clickable(onClick = aoEditar)
                        .padding(top = 2.dp)
                )
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(FundoCartao)
            .border(1.dp, Linha, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(tipoDaConta, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Navy)
    }
}

@Composable
private fun CartaoAtalho(
    icone: ImageVector,
    titulo: String,
    subtitulo: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(FundoCartao)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Icon(icone, contentDescription = null, tint = Navy, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(10.dp))
        Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Text(subtitulo, fontSize = 11.sp, color = Muted)
    }
}

@Composable
private fun TituloDoGrupo(texto: String) {
    Text(
        texto,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Navy,
        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
    )
}

@Composable
private fun LinhaDeMenu(
    icone: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(FundoCartao)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, contentDescription = null, tint = Navy, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(subtitulo, fontSize = 11.sp, color = Muted)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Opção dentro de um cartão agrupado. */
@Composable
internal fun LinhaDoCartao(titulo: String, subtitulo: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
            Text(subtitulo, fontSize = 11.sp, color = Muted)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Muted,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Divisor entre as opções do cartão. */
@Composable
internal fun DivisorDoCartao() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White)
    )
}

@Composable
internal fun BotaoSairDaConta(onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Linha, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.Logout,
            contentDescription = null,
            tint = Navy,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Sair da conta", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Navy)
    }
}

/** Status da loja: aberta (verde) ou pausada (amarelo). */
@Composable
private fun CartaoStatusDaLoja(
    aberta: Boolean,
    aoVerLoja: () -> Unit,
    aoPausar: () -> Unit
) {
    val fundo = if (aberta) VerdeClaro else AmareloClaro
    val borda = if (aberta) VerdeBorda else AmareloBorda
    val cor = if (aberta) Verde else Color(0xFF8A5A00)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(fundo)
            .border(1.dp, borda, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (aberta) Verde else Estrela)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (aberta) "Loja aberta" else "Loja pausada",
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = cor
            )
        }
        Text(
            if (aberta) "Recebendo novos pedidos" else "Não está recebendo novos pedidos",
            fontSize = 12.sp,
            color = cor,
            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotaoDoCartao(
                Icons.Outlined.Visibility, "Ver minha loja", Modifier.weight(1f), aoVerLoja
            )
            BotaoDoCartao(
                if (aberta) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                if (aberta) "Pausar pedidos" else "Retomar pedidos",
                Modifier.weight(1f),
                aoPausar
            )
        }
    }
}

@Composable
private fun BotaoDoCartao(
    icone: ImageVector,
    texto: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Linha, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icone, contentDescription = null, tint = Navy, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(texto, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Navy)
    }
}

//----Previews----

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 960)
@Composable
private fun PreviewPerfilCliente() {
    PerfilClienteConteudo("Cliente Demo", "cliente@cais.com") {}
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 1060)
@Composable
private fun PreviewPerfilClienteContaAberta() {
    PerfilClienteConteudo("Cliente Demo", "cliente@cais.com", contaAbertaNoInicio = true) {}
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 1500)
@Composable
private fun PreviewPerfilLoja() {
    PerfilLojaConteudo("Carlos Braga", "vendedor@cais.com", true) {}
}
