package com.example.myapplication.data

import com.example.myapplication.R

/** Dados simulados do MVP (sem servidor). Troque por banco/API quando o projeto evoluir. */
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

    val vendedores = listOf(
        Vendedor(1, "Noah Ludwig", "Revendedor do Cais", "Centro", 4.97),
        Vendedor(2, "Lúcia Barreto", "Peixaria Ribeirinha", "Cidade Nova", 4.94),
        Vendedor(3, "Carlos Braga", "Amazonas Pescados", "Adrianópolis", 4.78),
        Vendedor(4, "Sofia Costa", "Tião do peixe", "Compensa", 4.71)
    )

    val produtos = listOf(
        Produto(1, "Tambaqui", "Peixe de rio", Categoria.PEIXE, 38.90, 5.0, 3, "Hoje", foto = R.drawable.foto_tambaqui),
        Produto(2, "Matrinxã", "Peixe de cativeiro", Categoria.PEIXE, 32.25, 5.0, 2, "Hoje", foto = R.drawable.foto_matrinxa),
        Produto(3, "Pirarucu", "Peixe de cativeiro", Categoria.PEIXE, 61.35, 4.9, 3, "Ontem", foto = R.drawable.foto_pirarucu),
        Produto(4, "Tambaqui inteiro", "Peixe de rio", Categoria.PEIXE, 18.21, 4.5, 1, "Hoje", promocao = true, foto = R.drawable.foto_tambaqui),
        Produto(5, "Jaraqui", "Peixe de rio", Categoria.PEIXE, 22.50, 4.6, 4, "Hoje", promocao = true, foto = R.drawable.foto_jaraqui),
        Produto(6, "Pacu", "Peixe de rio", Categoria.PEIXE, 27.90, 4.7, 2, "Ontem", foto = R.drawable.foto_tambaqui),
        Produto(7, "Camarão regional", "Camarão de água doce", Categoria.CAMARAO, 62.00, 4.8, 1, "Hoje", foto = R.drawable.foto_camarao),
        Produto(8, "Camarão rosa limpo", "Camarão do mar", Categoria.CAMARAO, 74.90, 4.9, 4, "Ontem", foto = R.drawable.foto_camarao),
        Produto(9, "Caranguejo", "Frutos do mar", Categoria.FRUTOS_DO_MAR, 45.00, 4.5, 1, "Hoje"),
        Produto(10, "Filhote", "Peixe de rio", Categoria.PEIXE, 54.00, 4.8, 3, "Hoje", promocao = true)
    )

    /** Contas de teste: cliente@cais.com e vendedor@cais.com, senha 123456 */
    val usuarios = listOf(
        Usuario("Cliente Demo", "000.000.000-00", "cliente@cais.com", "123456"),
        Usuario("Carlos Braga", "00.000.000/0001-00", "vendedor@cais.com", "123456", vendedorId = 3)
    )

    /** Pedidos de outros clientes, para o painel do vendedor (Carlos Braga) ter o que mostrar. */
    val pedidos = listOf(
        Pedido(
            1042, "ana@email.com", "Ana, Centro", 3,
            listOf(ItemCarrinho(produtos[0], 1.5)),
            true, "Rua das Palmeiras, 120 – Centro", 6.0, "Pix", 0
        ),
        Pedido(
            1043, "bruno@email.com", "Bruno, Cidade Nova", 3,
            listOf(ItemCarrinho(produtos[2], 2.0)),
            true, "Av. Cidade Nova, 45 – Cidade Nova", 12.0, "Cartão", 1
        ),
        Pedido(
            1044, "carla@email.com", "Carla, Adrianópolis", 3,
            listOf(ItemCarrinho(produtos[9], 1.0)),
            false, "Retirada: Feira da Panair – Banca 14", 0.0, "Pix", 2
        )
    )
}
