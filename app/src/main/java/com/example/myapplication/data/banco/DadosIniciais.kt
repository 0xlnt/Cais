package com.example.myapplication.data.banco

/** Dados de exemplo que já vêm "dentro" do banco falso. Contas de teste: senha 123456. */
internal object DadosIniciais {

    fun preencher(banco: BancoFalso) {
        val agora = System.currentTimeMillis()
        val hash = Senha.gerarHash("123456")

        // ---- categorias ----
        banco.inserirCategoria(CategoriaRow(1, "Peixe de rio"))
        banco.inserirCategoria(CategoriaRow(2, "Camarão"))
        banco.inserirCategoria(CategoriaRow(3, "Frutos do mar"))

        // ---- usuarios: 1 cliente, 4 vendedores e 3 clientes que já têm pedidos ----
        fun usuario(id: Int, nome: String, email: String, tipo: String) =
            banco.inserirUsuario(UsuarioRow(id, nome, email, hash, "(92) 90000-000$id", tipo, agora))

        usuario(1, "Cliente Demo", "cliente@cais.com", TipoUsuario.CLIENTE)
        usuario(2, "Noah Ludwig", "noah@cais.com", TipoUsuario.VENDEDOR)
        usuario(3, "Lúcia Barreto", "lucia@cais.com", TipoUsuario.VENDEDOR)
        usuario(4, "Carlos Braga", "vendedor@cais.com", TipoUsuario.VENDEDOR)
        usuario(5, "Sofia Costa", "sofia@cais.com", TipoUsuario.VENDEDOR)
        usuario(6, "Ana", "ana@email.com", TipoUsuario.CLIENTE)
        usuario(7, "Bruno", "bruno@email.com", TipoUsuario.CLIENTE)
        usuario(8, "Carla", "carla@email.com", TipoUsuario.CLIENTE)

        // ---- enderecos (o bairro do vendedor entra na busca por bairro) ----
        banco.inserirEndereco(EnderecoRow(1, 1, "Rua das Palmeiras", "120", "Centro", "Manaus"))
        banco.inserirEndereco(EnderecoRow(2, 2, "Rua Marechal Deodoro", "10", "Centro", "Manaus"))
        banco.inserirEndereco(EnderecoRow(3, 3, "Av. Cidade Nova", "300", "Cidade Nova", "Manaus"))
        banco.inserirEndereco(EnderecoRow(4, 4, "Av. Epaminondas", "55", "Adrianópolis", "Manaus"))
        banco.inserirEndereco(EnderecoRow(5, 5, "Rua Pará", "8", "Compensa", "Manaus"))
        banco.inserirEndereco(EnderecoRow(6, 6, "Rua das Palmeiras", "120", "Centro", "Manaus"))
        banco.inserirEndereco(EnderecoRow(7, 7, "Av. Cidade Nova", "45", "Cidade Nova", "Manaus"))

        // ---- vendedores ----
        banco.inserirVendedor(VendedorRow(1, 2, "Revendedor do Cais", "Peixe fresco todo dia.", null, 4.97))
        banco.inserirVendedor(VendedorRow(2, 3, "Peixaria Ribeirinha", "Direto do rio para a sua mesa.", null, 4.94))
        banco.inserirVendedor(VendedorRow(3, 4, "Amazonas Pescados", "Pescado selecionado.", null, 4.78))
        banco.inserirVendedor(VendedorRow(4, 5, "Tião do peixe", "Tradição da Compensa.", null, 4.71))

        // ---- produtos ----
        val hoje = dataIso(0)
        val ontem = dataIso(1)
        fun produto(
            id: Int, vendedor: Int, cat: Int, nome: String, desc: String, preco: Double,
            data: String, foto: String?, oferta: Boolean = false
        ) = banco.inserirProduto(
            ProdutoRow(id, vendedor, cat, nome, desc, preco, foto, data, true, oferta)
        )

        produto(1, 3, 1, "Tambaqui", "Peixe de rio", 38.90, hoje, "foto_tambaqui")
        produto(2, 2, 1, "Matrinxã", "Peixe de cativeiro", 32.25, hoje, "foto_matrinxa")
        produto(3, 3, 1, "Pirarucu", "Peixe de cativeiro", 61.35, ontem, "foto_pirarucu")
        produto(4, 1, 1, "Tambaqui inteiro", "Peixe de rio", 18.21, hoje, "foto_tambaqui", oferta = true)
        produto(5, 4, 1, "Jaraqui", "Peixe de rio", 22.50, hoje, "foto_jaraqui", oferta = true)
        produto(6, 2, 1, "Pacu", "Peixe de rio", 27.90, ontem, "foto_tambaqui")
        produto(7, 1, 2, "Camarão regional", "Camarão de água doce", 62.00, hoje, "foto_camarao")
        produto(8, 4, 2, "Camarão rosa limpo", "Camarão do mar", 74.90, ontem, "foto_camarao")
        produto(9, 1, 3, "Caranguejo", "Frutos do mar", 45.00, hoje, null)
        produto(10, 3, 1, "Filhote", "Peixe de rio", 54.00, hoje, null, oferta = true)

        // ---- pedidos de outros clientes (para o painel do vendedor ter o que mostrar) ----
        fun pedido(
            id: Int, cliente: Int, endereco: Int?, tipo: String, pagto: String,
            produtoId: Int, kg: Double, preco: Double, frete: Double, status: String
        ) {
            val subtotal = preco * kg
            banco.inserirPedido(
                PedidoRow(id, cliente, 3, endereco, tipo, pagto, subtotal, frete, subtotal + frete, status, agora),
                listOf(PedidoItemRow(id, id, produtoId, kg, preco))
            )
        }
        pedido(1042, 6, 6, TipoEntrega.ENTREGA, "Pix", 1, 1.5, 38.90, 6.0, StatusPedido.REALIZADO)
        pedido(1043, 7, 7, TipoEntrega.ENTREGA, "Cartão", 3, 2.0, 61.35, 12.0, StatusPedido.PREPARACAO)
        pedido(1044, 8, null, TipoEntrega.RETIRADA, "Pix", 10, 1.0, 54.00, 0.0, StatusPedido.SAIU_ENTREGA)
    }
}
