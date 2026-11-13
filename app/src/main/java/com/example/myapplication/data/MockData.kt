package com.example.myapplication.data

/**
 * Opções fixas das telas (zonas de entrega, pontos de retirada e formas de pagamento).
 * Vendedores, produtos, usuários e pedidos agora vêm do banco falso: data/banco/.
 */
object MockData {

    val zonas = listOf(
        Zona("Centro", 6.0, 0.0),
        Zona("Adrianópolis", 8.0, 0.0),
        Zona("Cidade Nova", 12.0, 50.0),
        Zona("Ponta Negra", 15.0, 80.0)
    )

    val pontosRetirada = listOf(
        "Feira da Panair – Banca 14",
        "Mercado Adolpho Lisboa"
    )

    val pagamentos = listOf("Pix", "Cartão", "Dinheiro")
}
