package com.example.myapplication.data.remoto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Perfil novo (tabela perfil): o id é o mesmo do usuário criado no login do Supabase. */
@Serializable
data class PerfilNovoDto(
    val id: String,
    val nome: String,
    @SerialName("cpf_cnpj") val cpfCnpj: String,
    /** "cliente" ou "vendedor" (enum tipo_usuario do banco) */
    val tipo: String
)

/** Loja nova (tabela vendedor): o id é o mesmo do perfil. */
@Serializable
data class VendedorNovoDto(
    val id: String,
    @SerialName("nome_loja") val nomeLoja: String,
    val descricao: String
)
