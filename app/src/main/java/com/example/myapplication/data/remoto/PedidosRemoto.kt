package com.example.myapplication.data.remoto

import com.example.myapplication.data.ItemCarrinho
import com.example.myapplication.data.Pedido
import com.example.myapplication.data.Produto
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDateTime
import java.time.ZoneOffset

/** Pedidos, itens e endereços no Supabase. O RLS garante que cada um só vê o que é seu. */
object PedidosRemoto {
    private val db get() = SupabaseCliente.cliente.postgrest

    suspend fun zonas(): List<ZonaDto> =
        db.from("zona_entrega").select().decodeList<ZonaDto>()

    suspend fun criarEndereco(endereco: EnderecoNovoDto): EnderecoDto =
        db.from("endereco").insert(endereco) { select() }.decodeSingle<EnderecoDto>()

    suspend fun criarPedido(pedido: PedidoNovoDto): PedidoDto =
        db.from("pedido").insert(pedido) { select() }.decodeSingle<PedidoDto>()

    suspend fun criarItens(itens: List<ItemPedidoNovoDto>) {
        db.from("item_pedido").insert(itens)
    }

    /** Pedidos do usuário logado (como cliente ou como vendedor), do mais novo para o mais antigo. */
    suspend fun pedidos(): List<PedidoDto> =
        db.from("pedido")
            .select { order(column = "criado_em", order = Order.DESCENDING) }
            .decodeList<PedidoDto>()

    suspend fun itens(idsDosPedidos: List<Int>): List<ItemPedidoDto> =
        if (idsDosPedidos.isEmpty()) emptyList()
        else db.from("item_pedido")
            .select { filter { isIn("id_pedido", idsDosPedidos) } }
            .decodeList<ItemPedidoDto>()

    suspend fun enderecos(ids: List<Int>): List<EnderecoDto> =
        if (ids.isEmpty()) emptyList()
        else db.from("endereco")
            .select { filter { isIn("id_endereco", ids) } }
            .decodeList<EnderecoDto>()

    /** Nomes dos clientes. O vendedor só consegue ler quem fez pedido para ele (política do perfil). */
    suspend fun nomes(ids: List<String>): List<PerfilDto> =
        if (ids.isEmpty()) emptyList()
        else db.from("perfil")
            .select { filter { isIn("id", ids) } }
            .decodeList<PerfilDto>()

    suspend fun mudarStatus(idPedido: Int, status: String) {
        db.from("pedido").update({ set("status", status) }) {
            filter { eq("id_pedido", idPedido) }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Status: o app tem 5 passos (0 a 4); o banco tem nomes próprios (e dois para o passo 3).
// ---------------------------------------------------------------------------------------------

fun statusParaPasso(status: String): Int = when (status) {
    "recebido" -> 0
    "confirmado" -> 1
    "em_preparo" -> 2
    "saiu_para_entrega", "pronto_retirada" -> 3
    "entregue" -> 4
    else -> 0
}

fun passoParaStatus(passo: Int, entrega: Boolean): String = when (passo) {
    0 -> "recebido"
    1 -> "confirmado"
    2 -> "em_preparo"
    3 -> if (entrega) "saiu_para_entrega" else "pronto_retirada"
    else -> "entregue"
}

private fun milissegundos(texto: String): Long =
    try {
        LocalDateTime.parse(texto).toInstant(ZoneOffset.UTC).toEpochMilli()
    } catch (e: Exception) {
        0L
    }

/** Monta o pedido que as telas usam, juntando itens, produtos do catálogo, nome e endereço. */
fun PedidoDto.paraUi(
    itens: List<ItemPedidoDto>,
    produtos: List<Produto>,
    nomeDoCliente: String,
    emailDoCliente: String,
    endereco: EnderecoDto?
): Pedido {
    val entrega = tipoEntrega == "entrega"
    val destino = when {
        !entrega -> "Retirada com o vendedor"
        endereco != null -> "${endereco.rua}, ${endereco.numero ?: "s/n"} – ${endereco.bairro}"
        else -> "Endereço de entrega"
    }
    return Pedido(
        id = id,
        clienteEmail = emailDoCliente,
        clienteNome = nomeDoCliente,
        vendedorId = vendedorId,
        itens = itens.filter { it.pedidoId == id }.mapNotNull { item ->
            // usa o preço da hora da compra, não o preço de hoje
            produtos.firstOrNull { it.id == item.produtoId }
                ?.let { ItemCarrinho(it.copy(preco = item.precoKg), item.quantidadeKg) }
        },
        entrega = entrega,
        destino = destino,
        taxa = taxaEntrega,
        pagamento = formaPagamento,
        status = statusParaPasso(status),
        criadoEm = milissegundos(criadoEm),
        clienteId = clienteId
    )
}
