package com.example.myapplication.data.remoto

import com.example.myapplication.data.Categoria
import com.example.myapplication.data.Produto
import com.example.myapplication.data.Vendedor
import com.example.myapplication.data.banco.fotoDrawable
import com.example.myapplication.data.banco.rotuloDaData

fun VendedorDto.paraUi() = Vendedor(
    id = id,
    nome = nomeLoja,
    descricao = descricao ?: "",
    bairro = "Centro",   // provisório: o Supabase ainda não tem esse dado
    nota = 0.0           // provisório
)

fun CategoriaDto.paraEnum(): Categoria = when {
    nome.startsWith("Peixe", ignoreCase = true) -> Categoria.PEIXE
    nome.startsWith("Cam", ignoreCase = true) -> Categoria.CAMARAO
    else -> Categoria.FRUTOS_DO_MAR
}

/** Id da categoria no Supabase que corresponde ao enum da tela (usado ao cadastrar produto). */
fun List<CategoriaDto>.idDe(categoria: Categoria): Int? =
    firstOrNull { it.paraEnum() == categoria }?.id

fun ProdutoDto.paraUi(categorias: List<CategoriaDto>): Produto {
    val categoria = categorias.firstOrNull { it.id == categoriaId }?.paraEnum() ?: Categoria.PEIXE
    return Produto(
        id = id,
        nome = nome,
        tipo = descricao ?: "",
        categoria = categoria,
        preco = precoKg,
        nota = 0.0,
        vendedorId = vendedorId,
        dataPesca = rotuloDaData(dataPesca ?: ""),
        disponivel = disponivel,
        promocao = false,
        // produtos antigos guardam o nome de um drawable; os novos guardam a URL da foto
        foto = fotoDrawable(foto),
        estoqueKg = estoqueKg,
        fotoUrl = foto?.takeIf { it.startsWith("http") }
    )
}
