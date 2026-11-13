package com.example.myapplication.data.banco

import com.example.myapplication.R
import com.example.myapplication.data.Categoria
import com.example.myapplication.data.ItemCarrinho
import com.example.myapplication.data.Pedido
import com.example.myapplication.data.Produto
import com.example.myapplication.data.Usuario
import com.example.myapplication.data.Vendedor
import java.util.Calendar

/*
 * Converte as linhas das tabelas (Tabelas.kt) para os modelos que as telas já usam (Models.kt).
 * Assim as telas não precisam saber como o banco guarda os dados.
 */

/** Nome do arquivo guardado no banco -> id do drawable. Foto nova: acrescente uma linha aqui. */
fun fotoDrawable(nome: String?): Int? = when (nome) {
    "foto_tambaqui" -> R.drawable.foto_tambaqui
    "foto_matrinxa" -> R.drawable.foto_matrinxa
    "foto_pirarucu" -> R.drawable.foto_pirarucu
    "foto_jaraqui" -> R.drawable.foto_jaraqui
    "foto_camarao" -> R.drawable.foto_camarao
    else -> null
}

fun CategoriaRow.paraEnum(): Categoria = when {
    nome.startsWith("Peixe", ignoreCase = true) -> Categoria.PEIXE
    nome.startsWith("Cam", ignoreCase = true) -> Categoria.CAMARAO
    else -> Categoria.FRUTOS_DO_MAR
}

/** Id da categoria no banco que corresponde ao enum da tela. */
fun CaisRepository.categoriaIdDe(categoria: Categoria): Int =
    listarCategorias().firstOrNull { it.paraEnum() == categoria }?.id ?: 1

/** "2026-11-13" -> "Hoje", "Ontem" ou "11/11/2026". */
fun rotuloDaData(iso: String): String {
    val p = iso.split("-")
    if (p.size != 3) return iso
    val ano = p[0].toIntOrNull() ?: return iso
    val mes = p[1].toIntOrNull() ?: return iso
    val dia = p[2].toIntOrNull() ?: return iso
    val cal = Calendar.getInstance()
    fun igual() = cal.get(Calendar.YEAR) == ano && cal.get(Calendar.MONTH) + 1 == mes &&
        cal.get(Calendar.DAY_OF_MONTH) == dia
    if (igual()) return "Hoje"
    cal.add(Calendar.DAY_OF_YEAR, -1)
    if (igual()) return "Ontem"
    return "%02d/%02d/%04d".format(dia, mes, ano)
}

/** "13/11/2026" (como a tela digita) -> "2026-11-13". Se não entender, devolve a data de hoje. */
fun dataBrParaIso(texto: String): String {
    val p = texto.trim().split("/")
    val dia = p.getOrNull(0)?.toIntOrNull()
    val mes = p.getOrNull(1)?.toIntOrNull()
    val ano = p.getOrNull(2)?.toIntOrNull()
    return if (dia != null && mes != null && ano != null) "%04d-%02d-%02d".format(ano, mes, dia) else dataIso(0)
}

fun VendedorRow.paraUi(repo: CaisRepository): Vendedor = Vendedor(
    id = id,
    nome = nomeLoja,
    descricao = descricao,
    bairro = repo.enderecosDoUsuario(usuarioId).firstOrNull()?.bairro ?: "Centro",
    nota = avaliacaoMedia
)

/** A tabela produto não tem nota própria: a tela mostra a média do vendedor. */
fun ProdutoRow.paraUi(repo: CaisRepository): Produto {
    val categoria = repo.listarCategorias().firstOrNull { it.id == categoriaId }
    return Produto(
        id = id,
        nome = nome,
        tipo = descricao,
        categoria = categoria?.paraEnum() ?: Categoria.PEIXE,
        preco = precoKg,
        nota = repo.vendedorPorId(vendedorId)?.avaliacaoMedia ?: 0.0,
        vendedorId = vendedorId,
        dataPesca = rotuloDaData(dataPesca),
        disponivel = disponivel,
        promocao = ofertaDoDia,
        foto = fotoDrawable(foto)
    )
}

/** O banco guarda só o id; a tela usa o produto completo dentro do item. */
fun ItemDeCompra.paraUi(repo: CaisRepository): ItemCarrinho? =
    repo.produtoPorId(produtoId)?.let { ItemCarrinho(it.paraUi(repo), quantidadeKg) }

fun UsuarioRow.paraUi(repo: CaisRepository): Usuario = Usuario(
    nome = nome,
    doc = "", // a tabela usuario não tem CPF/CNPJ
    email = email,
    senha = "", // a senha nunca volta do banco
    vendedorId = repo.vendedorDoUsuario(id)?.id
)

fun PedidoRow.paraUi(repo: CaisRepository): Pedido {
    val cliente = repo.usuarioPorId(clienteId)
    val entrega = tipoEntrega == TipoEntrega.ENTREGA
    val endereco = enderecoId?.let { repo.enderecoPorId(it) }
    val destino = if (entrega && endereco != null) {
        "${endereco.rua}, ${endereco.numero} – ${endereco.bairro}"
    } else {
        "Retirada com o vendedor"
    }
    return Pedido(
        id = id,
        clienteEmail = cliente?.email ?: "",
        clienteNome = cliente?.nome ?: "Cliente",
        vendedorId = vendedorId,
        itens = repo.itensDoPedido(id).mapNotNull { item ->
            repo.produtoPorId(item.produtoId)?.let {
                // usa o preço da hora da compra, não o preço de hoje
                ItemCarrinho(it.paraUi(repo).copy(preco = item.precoUnitario), item.quantidadeKg)
            }
        },
        entrega = entrega,
        destino = destino,
        taxa = frete,
        pagamento = formaPagamento,
        status = StatusPedido.ORDEM.indexOf(status).coerceAtLeast(0)
    )
}
