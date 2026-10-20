package com.example.myapplication.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Produto
import com.example.myapplication.data.Vendedor
import com.example.myapplication.data.reais

private val CorFavorito = Color(0xFFE5484D)

@Composable
private fun Coracao(favorito: Boolean, aoFavoritar: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = aoFavoritar, modifier = modifier.size(32.dp)) {
        Icon(
            if (favorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = if (favorito) "Remover dos favoritos" else "Favoritar",
            tint = if (favorito) CorFavorito else Muted,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Cartão largo, usado na lista da Busca. */
@Composable
fun CartaoProduto(
    produto: Produto,
    vendedor: Vendedor?,
    favorito: Boolean,
    aoFavoritar: () -> Unit,
    aoClicar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = aoClicar),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(Modifier.padding(10.dp)) {
            FotoProduto(
                categoria = produto.categoria,
                seed = produto.id,
                foto = produto.foto,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(12.dp)),
                tamanhoEmoji = 40.sp
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        produto.nome,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Coracao(favorito, aoFavoritar)
                }
                Text(
                    produto.tipo + " • " + produto.dataPesca,
                    fontSize = 12.sp,
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Nota(produto.nota)
                Text(
                    produto.preco.reais() + "/kg",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(6.dp))
                FaixaVendedor(vendedor)
            }
        }
    }
}

/** Cartão pequeno, usado nos carrosséis da Home. */
@Composable
fun CartaoProdutoMini(
    produto: Produto,
    vendedor: Vendedor?,
    favorito: Boolean,
    aoFavoritar: () -> Unit,
    aoClicar: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(156.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = aoClicar),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Ice)
    ) {
        Column(Modifier.padding(6.dp)) {
            Box {
                FotoProduto(
                    categoria = produto.categoria,
                    seed = produto.id,
                    foto = produto.foto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(84.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    tamanhoEmoji = 34.sp
                )
                Coracao(
                    favorito,
                    aoFavoritar,
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(50))
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(produto.nome, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text(produto.tipo, fontSize = 11.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Nota(produto.nota)
            Text(produto.preco.reais() + "/kg", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(6.dp))
            FaixaVendedor(vendedor)
        }
    }
}

/** Cartão do vendedor em destaque (Home). */
@Composable
fun CartaoVendedor(vendedor: Vendedor, aoClicar: () -> Unit) {
    Card(
        modifier = Modifier
            .width(138.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = aoClicar),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(Brush.verticalGradient(listOf(Teal, Navy))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    vendedor.nome.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Column(Modifier.padding(10.dp)) {
                Text(vendedor.nome, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                Text(vendedor.descricao, fontSize = 11.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Teal,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(vendedor.bairro, fontSize = 10.sp, maxLines = 1, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(6.dp))
                    Nota(vendedor.nota)
                }
            }
        }
    }
}
