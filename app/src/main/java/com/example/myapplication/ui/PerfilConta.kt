package com.example.myapplication.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.banco.EnderecoRow

//----Telas da Minha conta (cliente)----
// Por enquanto só visuais: os botões mostram "Em breve".

@Composable
private fun avisoEmBreve(): () -> Unit {
    val contexto = LocalContext.current
    return { Toast.makeText(contexto, "Em breve", Toast.LENGTH_SHORT).show() }
}

//----Dados pessoais----

@Composable
fun DadosPessoaisScreen(vm: CaisViewModel, aoVoltar: () -> Unit) {
    val u = vm.usuario ?: return
    DadosPessoaisConteudo(
        nome = u.nome,
        telefone = vm.telefoneDoUsuario,
        email = u.email,
        aoVoltar = aoVoltar,
        aoSalvar = avisoEmBreve()
    )
}

@Composable
fun DadosPessoaisConteudo(
    nome: String,
    telefone: String,
    email: String,
    aoVoltar: () -> Unit,
    aoSalvar: () -> Unit
) {
    var nomeDigitado by rememberSaveable { mutableStateOf(nome) }
    var telefoneDigitado by rememberSaveable { mutableStateOf(telefone) }
    var emailDigitado by rememberSaveable { mutableStateOf(email) }

    TelaDaConta(
        titulo = "Dados pessoais",
        descricao = "Confira e atualize suas informações de contato.",
        aoVoltar = aoVoltar,
        rodape = { BotaoPrimario("Salvar alterações", onClick = aoSalvar) }
    ) {
        CampoDaConta("Nome", nomeDigitado, { nomeDigitado = it })
        CampoDaConta(
            "Telefone", telefoneDigitado, { telefoneDigitado = it },
            teclado = KeyboardType.Phone, dica = "(92) 99999-9999"
        )
        CampoDaConta("E-mail", emailDigitado, { emailDigitado = it }, teclado = KeyboardType.Email)
    }
}

//----Conta (vendedor)----

@Composable
fun ContaScreen(vm: CaisViewModel, aoVoltar: () -> Unit) {
    val u = vm.usuario ?: return
    // O cadastro guarda um só documento: 11 dígitos é CPF, 14 é CNPJ
    val digitos = u.doc.filter { it.isDigit() }
    ContaConteudo(
        email = u.email,
        cpf = if (digitos.length == 11) u.doc else "",
        cnpj = if (digitos.length == 14) u.doc else "",
        telefone = vm.telefoneDoUsuario,
        aoVoltar = aoVoltar,
        aoSalvar = avisoEmBreve()
    )
}

@Composable
fun ContaConteudo(
    email: String,
    cpf: String,
    cnpj: String,
    telefone: String,
    aoVoltar: () -> Unit,
    aoSalvar: () -> Unit
) {
    var emailDigitado by rememberSaveable { mutableStateOf(email) }
    var cpfDigitado by rememberSaveable { mutableStateOf(cpf) }
    var cnpjDigitado by rememberSaveable { mutableStateOf(cnpj) }
    var telefoneDigitado by rememberSaveable { mutableStateOf(telefone) }

    TelaDaConta(
        titulo = "Conta",
        descricao = "Confira os dados de acesso e de cadastro da sua loja.",
        aoVoltar = aoVoltar,
        rodape = { BotaoPrimario("Salvar alterações", onClick = aoSalvar) }
    ) {
        CampoDaConta("E-mail", emailDigitado, { emailDigitado = it }, teclado = KeyboardType.Email)
        CampoDaConta(
            "CPF", cpfDigitado, { cpfDigitado = it },
            teclado = KeyboardType.Number, dica = "000.000.000-00"
        )
        CampoDaConta(
            "CNPJ", cnpjDigitado, { cnpjDigitado = it },
            teclado = KeyboardType.Number, dica = "00.000.000/0000-00"
        )
        CampoDaConta(
            "Telefone", telefoneDigitado, { telefoneDigitado = it },
            teclado = KeyboardType.Phone, dica = "(92) 99999-9999"
        )
    }
}

/** Campo com o rótulo em cima. */
@Composable
private fun CampoDaConta(
    rotulo: String,
    valor: String,
    aoMudar: (String) -> Unit,
    teclado: KeyboardType = KeyboardType.Text,
    dica: String = ""
) {
    Text(
        rotulo,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Navy,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    TextField(
        value = valor,
        onValueChange = aoMudar,
        singleLine = true,
        placeholder = { if (dica.isNotEmpty()) Text(dica, color = Muted) },
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Ice,
            unfocusedContainerColor = Ice,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Navy,
            unfocusedTextColor = Navy,
            cursorColor = Navy
        ),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(20.dp))
}

//----Endereços----

@Composable
fun EnderecosScreen(vm: CaisViewModel, aoVoltar: () -> Unit) {
    val enderecos = remember(vm.usuario) { vm.meusEnderecos() }
    EnderecosConteudo(enderecos = enderecos, aoVoltar = aoVoltar, aoAcao = avisoEmBreve())
}

/** O primeiro endereço é o principal. */
@Composable
fun EnderecosConteudo(
    enderecos: List<EnderecoRow>,
    aoVoltar: () -> Unit,
    aoAcao: () -> Unit
) {
    TelaDaConta(
        titulo = "Endereços",
        descricao = "Gerencie os locais onde deseja receber seus pedidos.",
        aoVoltar = aoVoltar,
        rodape = { BotaoPrimario("+ Adicionar endereço", onClick = aoAcao) }
    ) {
        if (enderecos.isEmpty()) {
            EstadoVazio(
                "📍",
                "Nenhum endereço salvo",
                "Adicione um endereço para receber seus pedidos em casa."
            )
        } else {
            enderecos.forEachIndexed { i, e ->
                CartaoEndereco(e, principal = i == 0, aoAcao = aoAcao)
            }
        }
    }
}

@Composable
private fun CartaoEndereco(e: EnderecoRow, principal: Boolean, aoAcao: () -> Unit) {
    Column(
        Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Ice)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${e.rua}, ${e.numero}",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Navy,
                modifier = Modifier.weight(1f)
            )
            if (principal) Pilula("Principal")
        }
        Spacer(Modifier.height(10.dp))
        Text("${e.bairro} · ${e.cidade}", fontSize = 13.sp, color = Muted)
        if (!e.complemento.isNullOrBlank()) {
            Text(e.complemento, fontSize = 13.sp, color = Muted)
        }
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (principal) "Endereço principal" else "Definir como principal",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Navy,
                modifier = Modifier
                    .weight(1f)
                    .then(if (principal) Modifier else Modifier.clickable(onClick = aoAcao))
            )
            AcaoDoCartao("Editar", Navy, aoAcao)
            Spacer(Modifier.width(6.dp))
            AcaoDoCartao("Excluir", Perigo, aoAcao)
        }
    }
}

@Composable
private fun AcaoDoCartao(texto: String, cor: Color, onClick: () -> Unit) {
    Text(
        texto,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = cor,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    )
}

//----Segurança e privacidade----

@Composable
fun SegurancaScreen(vm: CaisViewModel, aoVoltar: () -> Unit, aoSair: () -> Unit) {
    val emBreve = avisoEmBreve()
    var confirmarExclusao by remember { mutableStateOf(false) }

    SegurancaConteudo(
        aoVoltar = aoVoltar,
        aoOpcao = emBreve,
        aoSair = {
            vm.sair()
            aoSair()
        },
        // Desligado. Para ligar: troque emBreve por { confirmarExclusao = true }
        aoExcluirConta = emBreve
    )

    if (confirmarExclusao) {
        DialogoExcluirConta(aoCancelar = { confirmarExclusao = false }) {
            confirmarExclusao = false
            vm.excluirConta()
            aoSair()
        }
    }
}

@Composable
fun SegurancaConteudo(
    aoVoltar: () -> Unit,
    aoOpcao: () -> Unit,
    aoSair: () -> Unit,
    aoExcluirConta: () -> Unit
) {
    TelaDaConta(
        titulo = "Segurança e Privacidade",
        descricao = "Gerencie sua senha e consulte como suas informações são utilizadas no Cais.",
        aoVoltar = aoVoltar,
        rodape = {
            BotaoSairDaConta(onClick = aoSair)
            Text(
                "Excluir minha conta",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Perigo,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = aoExcluirConta)
                    .padding(vertical = 8.dp)
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Ice)
        ) {
            LinhaDoCartao("Senha", "Atualize sua senha de acesso", aoOpcao)
            DivisorDoCartao()
            LinhaDoCartao("Termos de Uso", "Consulte as condições de uso do Cais", aoOpcao)
            DivisorDoCartao()
            LinhaDoCartao("Uso de Dados e Privacidade", "Saiba como seus dados são utilizados", aoOpcao)
        }
    }
}

//----Estrutura comum----

@Composable
private fun TelaDaConta(
    titulo: String,
    descricao: String,
    aoVoltar: () -> Unit,
    rodape: @Composable ColumnScope.() -> Unit,
    conteudo: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = aoVoltar) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Voltar",
                    tint = Navy,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(titulo, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            Text(descricao, fontSize = 14.sp, color = Muted)
            Spacer(Modifier.height(20.dp))
            conteudo()
        }

        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), content = rodape)
    }
}

//----Previews----

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewDadosPessoais() {
    DadosPessoaisConteudo("Cliente Demo", "(92) 99999-1234", "cliente@cais.com", {}, {})
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewConta() {
    ContaConteudo("vendedor@cais.com", "", "", "(92) 99999-1234", {}, {})
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewEnderecos() {
    EnderecosConteudo(
        enderecos = listOf(
            EnderecoRow(1, 1, "Rua das Palmeiras", "120", "Centro", "Manaus", "Casa, portão azul"),
            EnderecoRow(2, 1, "Av. Brasil", "850", "Compensa", "Manaus", "Sala 02")
        ),
        aoVoltar = {},
        aoAcao = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewSeguranca() {
    SegurancaConteudo({}, {}, {}, {})
}
