package com.example.myapplication.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(vm: CaisViewModel, aoEntrar: (ehVendedor: Boolean) -> Unit) {
    var cadastro by remember { mutableStateOf(false) }
    var vendedor by remember { mutableStateOf(false) }
    var nome by remember { mutableStateOf("") }
    var doc by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var aceito by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        CaisLogo()
        Spacer(Modifier.height(4.dp))
        Text(
            if (cadastro) "Crie sua conta para começar." else "Peixe fresco de Manaus, direto de quem pesca.",
            fontSize = 14.sp,
            color = Muted
        )
        Spacer(Modifier.height(20.dp))

        if (cadastro) {
            Seletor(
                opcoes = listOf("Sou cliente", "Sou vendedor"),
                selecionada = if (vendedor) 1 else 0,
                aoSelecionar = { vendedor = it == 1 }
            )
            Spacer(Modifier.height(8.dp))
            CampoTexto(nome, { nome = it }, "Nome completo", dica = "Seu nome")
            CampoTexto(
                doc, { doc = it }, "CPF ou CNPJ",
                dica = "000.000.000-00", teclado = KeyboardType.Number
            )
        }
        CampoTexto(
            email, { email = it }, "E-mail",
            dica = "voce@email.com", teclado = KeyboardType.Email
        )
        CampoTexto(senha, { senha = it }, "Senha", dica = "Mínimo 6 caracteres", senha = true)

        if (cadastro) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = aceito,
                    onCheckedChange = { aceito = it },
                    colors = CheckboxDefaults.colors(checkedColor = Navy)
                )
                Text(
                    "Aceito o uso dos meus dados para o funcionamento do app (LGPD).",
                    fontSize = 12.sp,
                    color = Muted
                )
            }
        }

        erro?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, color = Perigo, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))
        BotaoPrimario(if (cadastro) "Criar conta" else "Entrar") {
            val resultado = when {
                cadastro && !aceito -> "Aceite o termo de uso dos dados para continuar."
                cadastro -> vm.cadastrar(nome, doc, email, senha, vendedor)
                else -> vm.entrar(email, senha)
            }
            if (resultado == null) {
                aoEntrar(vm.usuario?.ehVendedor == true)
            } else {
                erro = resultado
            }
        }

        TextButton(
            onClick = {
                cadastro = !cadastro
                erro = null
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (cadastro) "Já tem conta? Entrar" else "Não tem conta? Criar conta",
                color = Navy,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (!cadastro) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Contas de teste (senha 123456):\ncliente@cais.com  •  vendedor@cais.com",
                fontSize = 12.sp,
                color = Muted
            )
        }
    }
}
