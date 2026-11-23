package com.example.myapplication.data.remoto

import com.example.myapplication.data.Categoria
import com.example.myapplication.data.Produto
import com.example.myapplication.data.banco.fotoDrawable
import com.example.myapplication.data.banco.rotuloDaData
import com.example.myapplication.data.Vendedor
fun VendedorDto.paraUi() = Vendedor(
    id = id,
    nome = nomeLoja,
    descricao = descricao ?:"",
    bairro = "Centro",
    nota = 0.0
)
fun ProdutoDto.paraUi(categorias: List<CategoriaDto>): Produto {
    val nomeCategoria = categorias.firstOrNull { it.id == categoriaId }?.nome ?: ""
    val categoria = when{
        nomeCategoria.startsWith("Peixe", ignoreCase = true) -> Categoria.PEIXE
        nomeCategoria.startsWith("Cam", ignoreCase = true) -> Categoria.CAMARAO
        else -> Categoria.FRUTOS_DO_MAR
    }
    return Produto(
        id = id,
        nome = nome,
        tipo = descricao?:"",
        categoria = categoria,
        preco = precoKg,
        nota = 0.0,
        vendedorId = vendedorId,
        dataPesca = rotuloDaData(dataPesca?:""),
        disponivel = disponivel,
        promocao = false,
        foto = fotoDrawable(foto),
        estoqueKg = estoqueKg
    )
}
