package com.example.myapplication.data.banco

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistenciaTest {

    private val disco = ArmazenamentoEmMemoria()

    /** "Fecha e abre o app": cria outro banco lendo o mesmo armazenamento. */
    private fun reabrir() = BancoFalso(armazenamento = disco)

    @Test
    fun contaCriadaContinuaExistindoDepoisDeFecharOApp() {
        val primeiro = BancoFalso(armazenamento = disco)
        primeiro.criarUsuario("Maria", "maria@email.com", "abcdef", "9299999", TipoUsuario.CLIENTE)

        val segundo = reabrir()
        val maria = segundo.autenticar("maria@email.com", "abcdef")
        assertNotNull(maria)
        assertEquals("Maria", maria!!.nome)
        assertNull(segundo.autenticar("maria@email.com", "errada"))
    }

    @Test
    fun pedidoEStatusContinuamDepoisDeFecharOApp() {
        val primeiro = BancoFalso(armazenamento = disco)
        val end = primeiro.enderecosDoUsuario(1).first()
        val criado = (primeiro.criarPedidos(
            1, listOf(ItemDeCompra(1, 2.0)), TipoEntrega.ENTREGA, end.id, "Pix", 6.0
        ) as Resultado.Ok).valor.single()
        primeiro.avancarStatus(criado.id)

        val segundo = reabrir()
        val lido = segundo.pedidoPorId(criado.id)!!
        assertEquals(StatusPedido.CONFIRMADO, lido.status)
        assertEquals(criado.total, lido.total, 0.001)
        assertEquals(38.90, segundo.itensDoPedido(criado.id).single().precoUnitario, 0.001)
    }

    @Test
    fun produtoNovoEDisponibilidadeContinuamDepoisDeFecharOApp() {
        val primeiro = BancoFalso(armazenamento = disco)
        val novo = (primeiro.criarProduto(3, 1, "Aruanã", "Peixe de rio", 41.5, dataIso(0), true) as Resultado.Ok).valor
        primeiro.mudarDisponibilidade(novo.id, false)

        val lido = reabrir().produtoPorId(novo.id)!!
        assertEquals("Aruanã", lido.nome)
        assertEquals(41.5, lido.precoKg, 0.001)
        assertEquals(false, lido.disponivel)
    }

    @Test
    fun textoComTabQuebraDeLinhaBarraENuloVoltaIgual() {
        val primeiro = BancoFalso(armazenamento = disco)
        val estranho = "Rua\tdo\\Peixe\nfundos \\N"
        primeiro.salvarEndereco(1, estranho, "10", "Centro", "Manaus", null)
        primeiro.salvarEndereco(1, "Rua B", "20", "Centro", "Manaus", "apto 2")

        val enderecos = reabrir().enderecosDoUsuario(1)
        assertEquals(estranho, enderecos[1].rua)
        assertNull(enderecos[1].complemento)
        assertEquals("apto 2", enderecos[2].complemento)
    }

    @Test
    fun arquivoEstragadoFazOBancoVoltarAosDadosIniciais() {
        disco.gravar("banco.txt", "isso nao e um banco de verdade")
        val banco = BancoFalso(armazenamento = disco)
        assertNotNull(banco.autenticar("cliente@cais.com", "123456"))
        assertEquals(10, banco.listarProdutos().size)
    }

    @Test
    fun primeiraAberturaCriaOArquivoComOsDadosIniciais() {
        assertNull(disco.ler("banco.txt"))
        BancoFalso(armazenamento = disco)
        assertTrue(disco.ler("banco.txt")!!.startsWith("cais-banco v1"))
    }

    @Test
    fun excluirContaTambemSomeDoArquivo() {
        val primeiro = BancoFalso(armazenamento = disco)
        primeiro.excluirUsuario(1)
        assertNull(reabrir().autenticar("cliente@cais.com", "123456"))
    }
}
