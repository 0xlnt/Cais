package com.example.myapplication.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Categoria
import com.example.myapplication.data.Vendedor
import com.example.myapplication.data.emKg
import com.example.myapplication.data.notaTexto

// ---------- cores do Cais ----------
val Navy = Color(0xFF0B3A6B)
val Teal = Color(0xFF13B4C8)
val Ice = Color(0xFFE6EFF8)
val Linha = Color(0xFFD3E0EE)
val Muted = Color(0xFF6B819A)
val Verde = Color(0xFF1F9D6B)
val Pagina = Color(0xFFF3F6FA)
val Estrela = Color(0xFFF5A623)
val Perigo = Color(0xFFD64545)
val FaixaTeal = Color(0xFFE0F6F9)

// ---------- visuais básicos ----------

@Composable
fun CaisLogo() {
    Row(verticalAlignment = Alignment.Bottom) {
        Text("Cais", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Navy)
        Box(
            Modifier
                .padding(start = 2.dp, bottom = 7.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Teal)
        )
    }
}

/**
 * Foto do produto. Por enquanto é um bloco colorido com emoji (não tem foto real no MVP).
 * Quando tiver fotos, troque o corpo desta função por Image(painterResource(...)).
 */
@Composable
fun FotoProduto(
    categoria: Categoria,
    seed: Int,
    modifier: Modifier = Modifier,
    tamanhoEmoji: TextUnit = 34.sp,
    foto: Int? = null
) {
    val cores = when (categoria) {
        Categoria.PEIXE ->
            if (seed % 2 == 0) listOf(Color(0xFF7FB6D9), Color(0xFF3B7FAE))
            else listOf(Color(0xFF8FCBBF), Color(0xFF2F8F83))
        Categoria.CAMARAO -> listOf(Color(0xFFF0A58B), Color(0xFFD1604A))
        Categoria.FRUTOS_DO_MAR -> listOf(Color(0xFFB9A6E0), Color(0xFF7459B8))
    }
    Box(
        modifier.background(Brush.verticalGradient(cores)),
        contentAlignment = Alignment.Center
    ) {
        if (foto != null) {
            Image(
                painter = painterResource(foto),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(categoria.emoji, fontSize = tamanhoEmoji)
        }
    }
}

@Composable
fun Avatar(nome: String, tamanho: Dp = 28.dp) {
    Box(
        Modifier
            .size(tamanho)
            .clip(CircleShape)
            .background(Teal),
        contentAlignment = Alignment.Center
    ) {
        Text(
            nome.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (tamanho.value * 0.45f).sp
        )
    }
}

@Composable
fun Nota(nota: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (nota > 0) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Estrela, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(2.dp))
        }
        Text(nota.notaTexto(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FaixaVendedor(vendedor: Vendedor?, modifier: Modifier = Modifier) {
    if (vendedor == null) return
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(FaixaTeal)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(vendedor.nome, 24.dp)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(vendedor.descricao, fontSize = 9.sp, color = Muted, maxLines = 1)
            Text(vendedor.nome, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Text("›", fontSize = 18.sp, color = Muted)
    }
}

@Composable
fun Pilula(texto: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .border(1.dp, Linha, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(texto, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecaoTitulo(titulo: String, aoVerTodos: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(titulo, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
        if (aoVerTodos != null) {
            Text(
                "Ver todos ›",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Teal,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = aoVerTodos)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun EstadoVazio(
    emoji: String,
    titulo: String,
    subtitulo: String,
    textoBotao: String? = null,
    aoClicar: (() -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(titulo, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(subtitulo, fontSize = 14.sp, color = Muted, textAlign = TextAlign.Center)
        if (textoBotao != null && aoClicar != null) {
            Spacer(Modifier.height(16.dp))
            BotaoPrimario(textoBotao, onClick = aoClicar)
        }
    }
}

// ---------- botões e campos ----------

@Composable
fun BotaoPrimario(
    texto: String,
    modifier: Modifier = Modifier,
    cor: Color = Navy,
    ativo: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = ativo,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = cor,
            contentColor = Color.White,
            disabledContainerColor = Linha,
            disabledContentColor = Muted
        )
    ) {
        Text(texto, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

/** Botão pequeno (usado nos cards do painel do vendedor). Mantém a cor mesmo desativado. */
@Composable
fun BotaoPequeno(
    texto: String,
    cor: Color = Teal,
    ativo: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = ativo,
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = cor,
            contentColor = Color.White,
            disabledContainerColor = cor,
            disabledContentColor = Color.White
        )
    ) {
        Text(texto, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun CampoTexto(
    valor: String,
    aoMudar: (String) -> Unit,
    rotulo: String,
    modifier: Modifier = Modifier,
    dica: String = "",
    teclado: KeyboardType = KeyboardType.Text,
    senha: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = aoMudar,
        label = { Text(rotulo) },
        placeholder = { if (dica.isNotEmpty()) Text(dica) },
        singleLine = true,
        visualTransformation = if (senha) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (senha) KeyboardType.Password else teclado),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Teal,
            unfocusedBorderColor = Linha,
            focusedLabelColor = Navy,
            cursorColor = Navy
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@Composable
fun ChipFiltro(rotulo: String, selecionado: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selecionado) Navy else Ice)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            rotulo,
            color = if (selecionado) Color.White else Navy,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

/** Seletor de 2 ou mais opções lado a lado (ex.: Entrega | Retirada). */
@Composable
fun Seletor(
    opcoes: List<String>,
    selecionada: Int,
    aoSelecionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Ice)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        opcoes.forEachIndexed { i, texto ->
            val ligado = i == selecionada
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (ligado) Navy else Color.Transparent)
                    .clickable { aoSelecionar(i) }
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    texto,
                    color = if (ligado) Color.White else Navy,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun BotaoCircular(
    icone: ImageVector,
    descricao: String,
    modifier: Modifier = Modifier,
    tinta: Color = Navy,
    onClick: () -> Unit
) {
    Box(
        modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.92f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icone, contentDescription = descricao, tint = tinta, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun BotaoQuadrado(icone: ImageVector, descricao: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Navy)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icone, contentDescription = descricao, tint = Color.White, modifier = Modifier.size(18.dp))
    }
}

/** − 1,5 kg + */
@Composable
fun StepperKg(kg: Double, aoDiminuir: () -> Unit, aoAumentar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        BotaoQuadrado(Icons.Filled.Remove, "Diminuir", aoDiminuir)
        Text(kg.emKg(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        BotaoQuadrado(Icons.Filled.Add, "Aumentar", aoAumentar)
    }
}

/** Campo que parece um input mas abre algo ao tocar (lista, calendário...). */
@Composable
fun CampoClicavel(rotulo: String, texto: String, aoClicar: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            rotulo,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Muted,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.5.dp, Linha, RoundedCornerShape(10.dp))
                .clickable(onClick = aoClicar)
                .padding(horizontal = 14.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(texto, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Muted)
        }
    }
}

/** Lista suspensa (ex.: zona de entrega). */
@Composable
fun SeletorLista(
    rotulo: String,
    opcoes: List<String>,
    selecionada: Int,
    aoSelecionar: (Int) -> Unit
) {
    var aberto by remember { mutableStateOf(false) }
    Box {
        CampoClicavel(rotulo, opcoes.getOrElse(selecionada) { "" }) { aberto = true }
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            opcoes.forEachIndexed { i, opcao ->
                DropdownMenuItem(
                    text = { Text(opcao) },
                    onClick = {
                        aoSelecionar(i)
                        aberto = false
                    }
                )
            }
        }
    }
}

@Composable
fun LinhaSeparadora() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Linha)
    )
}

/** Linha do tempo do pedido (Recebido → Em preparo → ... → Entregue). */
@Composable
fun LinhaDoTempo(passos: List<String>, atual: Int) {
    Column {
        passos.forEachIndexed { i, texto ->
            val feito = i < atual || atual == passos.lastIndex
            val agora = i == atual && !feito
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    feito -> Teal
                                    agora -> Color.White
                                    else -> Ice
                                }
                            )
                            .border(2.dp, if (feito || agora) Teal else Linha, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (feito) {
                            Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (i < passos.lastIndex) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(24.dp)
                                .background(if (i < atual) Teal else Linha)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.padding(top = 3.dp)) {
                    Text(
                        texto,
                        fontSize = 14.sp,
                        fontWeight = if (agora) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = if (feito || agora) Navy else Muted
                    )
                    if (agora) Text("Etapa atual", fontSize = 12.sp, color = Muted)
                }
            }
        }
    }
}
