package com.example.myapplication.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Produto
import com.example.myapplication.data.Vendedor

//----Favoritos (cliente)----

@Composable
fun FavoritosScreen(vm: CaisViewModel, aoVoltar: () -> Unit, aoAbrirProduto: (Int) -> Unit) {
    // Produtos marcados com o coração, na ordem em que foram favoritados
    val produtos = vm.favoritos.mapNotNull { id -> vm.produtos.firstOrNull { it.id == id } }
    FavoritosConteudo(
        produtos = produtos,
        vendedorDe = vm::vendedorDe,
        aoVoltar = aoVoltar,
        aoFavoritar = vm::alternarFavorito,
        aoAbrirProduto = aoAbrirProduto
    )
}

@Composable
fun FavoritosConteudo(
    produtos: List<Produto>,
    vendedorDe: (String) -> Vendedor?,
    aoVoltar: () -> Unit,
    aoFavoritar: (Int) -> Unit,
    aoAbrirProduto: (Int) -> Unit
) {
    // Sempre começa em "Produtos favoritos"
    var aba by remember { mutableStateOf(0) }

    TelaDaConta(
        titulo = "Favoritos",
        descricao = "Seus produtos e lojas salvos para comprar de novo.",
        aoVoltar = aoVoltar,
        rodape = {}
    ) {
        Seletor(
            opcoes = listOf("Produtos favoritos", "Lojas favoritas"),
            selecionada = aba,
            aoSelecionar = { aba = it }
        )
        Spacer(Modifier.height(16.dp))

        if (aba == 1) {
            EstadoVazio("🏪", "Em breve", "Logo você poderá salvar suas lojas favoritas aqui.")
        } else if (produtos.isEmpty()) {
            EstadoVazio(
                "🤍",
                "Nenhum produto favorito",
                "Toque no coração de um produto para salvá-lo aqui."
            )
        } else {
            produtos.forEach { p ->
                CartaoProduto(
                    produto = p,
                    vendedor = vendedorDe(p.vendedorId),
                    favorito = true,
                    aoFavoritar = { aoFavoritar(p.id) },
                    aoClicar = { aoAbrirProduto(p.id) }
                )
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

//----Previews----

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 390, heightDp = 860)
@Composable
private fun PreviewFavoritosVazio() {
    FavoritosConteudo(emptyList(), { null }, {}, {}, {})
}
