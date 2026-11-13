package com.example.myapplication.data.banco

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BancoFalsoTest {

    private val banco = BancoFalso()

    @Test
    fun loginComSenhaCertaFuncionaEErradaNao() {
        assertNotNull(banco.autenticar("cliente@cais.com", "123456"))
        assertNotNull(banco.autenticar("  CLIENTE@cais.com ", "123456"))
        assertNull(banco.autenticar("cliente@cais.com", "errada"))
        assertNull(banco.autenticar("naoexiste@cais.com", "123456"))
    }

    @Test
    fun senhaNaoFicaGuardadaPura() {
        val usuario = banco.autenticar("cliente@cais.com", "123456")!!
        assertNotEquals("123456", usuario.senhaHash)
        assertFalse(usuario.senhaHash.contains("123456"))
    }

    @Test
    fun naoCriaDoisUsuariosComMesmoEmail() {
        val primeiro = banco.criarUsuario("Maria", "maria@email.com", "abcdef", "", TipoUsuario.CLIENTE)
        val segundo = banco.criarUsuario("Outra", "MARIA@email.com", "abcdef", "", TipoUsuario.CLIENTE)
        assertTrue(primeiro is Resultado.Ok)
        assertEquals("Esse e-mail já tem conta.", segundo.erro)
    }

    @Test
    fun produtoCadastradoApareceNaListaDoVendedor() {
        val antes = banco.produtosDoVendedor(3).size
        val r = banco.criarProduto(3, 1, "Aruanã", "Peixe de rio", 40.0, dataIso(0), true)
        assertTrue(r is Resultado.Ok)
        assertEquals(antes + 1, banco.produtosDoVendedor(3).size)
    }

    @Test
    fun produtoComPrecoInvalidoDaErro() {
        assertEquals(
            "Digite um preço válido (ex.: 38,90).",
            banco.criarProduto(3, 1, "X", "", 0.0, dataIso(0), true).erro
        )
    }

    @Test
    fun carrinhoComDoisVendedoresGeraDoisPedidos() {
        val itens = listOf(ItemDeCompra(1, 2.0), ItemDeCompra(2, 1.0)) // vendedores 3 e 2
        val end = banco.enderecosDoUsuario(1).first()
        val r = banco.criarPedidos(1, itens, TipoEntrega.ENTREGA, end.id, "Pix", 6.0) as Resultado.Ok
        assertEquals(2, r.valor.size)
        assertEquals(setOf(2, 3), r.valor.map { it.vendedorId }.toSet())
        // cada pedido cobra o próprio frete
        r.valor.forEach { assertEquals(it.subtotal + 6.0, it.total, 0.001) }
    }

    @Test
    fun pedidoGuardaOPrecoDaHoraDaCompra() {
        val end = banco.enderecosDoUsuario(1).first()
        val r = banco.criarPedidos(
            1, listOf(ItemDeCompra(1, 2.0)), TipoEntrega.ENTREGA, end.id, "Pix", 6.0
        ) as Resultado.Ok
        val pedido = r.valor.single()
        assertEquals(StatusPedido.REALIZADO, pedido.status)
        assertEquals(38.90 * 2.0, pedido.subtotal, 0.001)

        banco.mudarDisponibilidade(1, false) // mudar o produto depois não mexe no pedido
        assertEquals(38.90, banco.itensDoPedido(pedido.id).single().precoUnitario, 0.001)
    }

    @Test
    fun retiradaNaoCobraFreteENaoPedeEndereco() {
        val r = banco.criarPedidos(
            1, listOf(ItemDeCompra(4, 1.0)), TipoEntrega.RETIRADA, null, "Dinheiro", 15.0
        ) as Resultado.Ok
        assertEquals(0.0, r.valor.single().frete, 0.001)
        assertNull(r.valor.single().enderecoId)
    }

    @Test
    fun entregaSemEnderecoDaErro() {
        val r = banco.criarPedidos(1, listOf(ItemDeCompra(1, 1.0)), TipoEntrega.ENTREGA, null, "Pix", 6.0)
        assertEquals("Escolha o endereço de entrega.", r.erro)
    }

    @Test
    fun carrinhoVazioOuProdutoIndisponivelDaErro() {
        assertEquals(
            "Seu carrinho está vazio.",
            banco.criarPedidos(1, emptyList(), TipoEntrega.RETIRADA, null, "Pix", 0.0).erro
        )
        banco.mudarDisponibilidade(4, false)
        assertEquals(
            "Tambaqui inteiro não está mais disponível.",
            banco.criarPedidos(1, listOf(ItemDeCompra(4, 1.0)), TipoEntrega.RETIRADA, null, "Pix", 0.0).erro
        )
    }

    @Test
    fun criarPedidoLimpaOCarrinhoSalvo() {
        banco.salvarCarrinho(1, listOf(ItemDeCompra(4, 1.0)))
        assertEquals(1, banco.carrinhoDoUsuario(1).size)
        banco.criarPedidos(1, listOf(ItemDeCompra(4, 1.0)), TipoEntrega.RETIRADA, null, "Pix", 0.0)
        assertTrue(banco.carrinhoDoUsuario(1).isEmpty())
    }

    @Test
    fun statusAvancaAteEntregueEPara() {
        var pedido = banco.pedidoPorId(1042)!!
        assertEquals(StatusPedido.REALIZADO, pedido.status)
        repeat(4) { pedido = banco.avancarStatus(1042)!! }
        assertEquals(StatusPedido.ENTREGUE, pedido.status)
        assertNull(banco.avancarStatus(1042))
    }

    @Test
    fun avaliacaoSoDepoisDeEntregueEAtualizaAMediaDoVendedor() {
        assertEquals("Só dá para avaliar pedido entregue.", banco.avaliar(1042, 5, null).erro)
        repeat(4) { banco.avancarStatus(1042) }
        assertEquals("A nota vai de 1 a 5.", banco.avaliar(1042, 6, null).erro)
        assertTrue(banco.avaliar(1042, 4, "Ótimo") is Resultado.Ok)
        assertEquals("Esse pedido já foi avaliado.", banco.avaliar(1042, 5, null).erro)
        assertEquals(4.0, banco.vendedorPorId(3)!!.avaliacaoMedia, 0.001)
    }
}
