package com.example.myapplication.data.remoto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Pedido como vem do Supabase (tabela pedido). */
@Serializable
data class PedidoDto(
    @SerialName("id_pedido") val id: Int,
    @SerialName("id_cliente") val clienteId: String,
    @SerialName("id_vendedor") val vendedorId: String,
    @SerialName("id_endereco") val enderecoId: Int? = null,
    @SerialName("tipo_entrega") val tipoEntrega: String,
    val status: String,
    val subtotal: Double,
    @SerialName("taxa_entrega") val taxaEntrega: Double = 0.0,
    val total: Double,
    @SerialName("forma_pagamento") val formaPagamento: String = "Pix",
    @SerialName("criado_em") val criadoEm: String = ""
)

/** Pedido novo, como o app envia (sem id, status e data: o banco preenche). */
@Serializable
data class PedidoNovoDto(
    @SerialName("id_cliente") val clienteId: String,
    @SerialName("id_vendedor") val vendedorId: String,
    @SerialName("id_endereco") val enderecoId: Int? = null,
    @SerialName("tipo_entrega") val tipoEntrega: String,
    val subtotal: Double,
    @SerialName("taxa_entrega") val taxaEntrega: Double,
    val total: Double,
    @SerialName("forma_pagamento") val formaPagamento: String
)

@Serializable
data class ItemPedidoDto(
    @SerialName("id_item") val id: Int,
    @SerialName("id_pedido") val pedidoId: Int,
    @SerialName("id_produto") val produtoId: Int,
    @SerialName("quantidade_kg") val quantidadeKg: Double,
    @SerialName("preco_kg") val precoKg: Double
)

@Serializable
data class ItemPedidoNovoDto(
    @SerialName("id_pedido") val pedidoId: Int,
    @SerialName("id_produto") val produtoId: Int,
    @SerialName("quantidade_kg") val quantidadeKg: Double,
    @SerialName("preco_kg") val precoKg: Double
)

@Serializable
data class ZonaDto(
    @SerialName("id_zona") val id: Int,
    val bairro: String,
    val taxa: Double = 0.0,
    @SerialName("pedido_minimo") val pedidoMinimo: Double = 0.0
)

@Serializable
data class EnderecoDto(
    @SerialName("id_endereco") val id: Int,
    val rua: String,
    val bairro: String,
    val numero: String? = null,
    val complemento: String? = null
)

@Serializable
data class EnderecoNovoDto(
    @SerialName("id_cliente") val clienteId: String,
    @SerialName("id_zona") val zonaId: Int,
    val rua: String,
    val bairro: String,
    val numero: String? = null,
    val complemento: String? = null
)
