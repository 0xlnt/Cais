package com.example.myapplication.data.remoto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CategoriaDto(
    @SerialName("id_categoria") val id: Int,
    val nome: String
)
@Serializable
data class VendedorDto(
    @SerialName("id") val id: String,
    @SerialName("nome_loja") val nomeLoja: String,
    val descricao: String? = null,
    val foto: String? = null
)
@Serializable
data class ProdutoDto(
    @SerialName("id_produto") val id: Int,
    @SerialName("id_vendedor") val vendedorId: String,
    @SerialName("id_categoria") val categoriaId: Int,
    val nome: String,
    val descricao: String? = null,
    @SerialName("preco_kg") val precoKg: Double,
    val foto: String? = null,
    @SerialName("data_pesca") val dataPesca: String? = null,
    val disponivel: Boolean = true,
    @SerialName("estoque_kg") val estoqueKg: Double = 0.0
)

/** Produto novo, como o app envia ao Supabase (sem o id, que o banco gera). */
@Serializable
data class ProdutoNovoDto(
    @SerialName("id_vendedor") val vendedorId: String,
    @SerialName("id_categoria") val categoriaId: Int,
    val nome: String,
    val descricao: String,
    @SerialName("preco_kg") val precoKg: Double,
    val foto: String? = null,
    @SerialName("data_pesca") val dataPesca: String,
    val disponivel: Boolean,
    @SerialName("estoque_kg") val estoqueKg: Double
)

@Serializable
data class PerfilDto(
    val id: String,
    val nome: String? = null,
    @SerialName("cpf_cnpj") val cpfCnpj: String? = null,
    val telefone: String? = null
)








