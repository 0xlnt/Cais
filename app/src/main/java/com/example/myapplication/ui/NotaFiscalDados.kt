package com.example.myapplication.ui

import com.example.myapplication.data.Pedido
import java.util.Calendar
import java.util.Locale

/*
 * Dados que aparecem na tela "Nota fiscal" e nos cupons DANFE.
 * Tudo é DEMONSTRAÇÃO (sem valor fiscal): CNPJ, IE, CPF, chave e QR Code são de exemplo.
 */

data class ItemNota(
    val codigo: String,
    val descricao: String,
    val kg: Double,
    val precoKg: Double
) {
    val total: Double get() = kg * precoKg
}

data class NotaFiscalDados(
    val pedidoId: Int,
    val loja: String,
    val cnpj: String = "00.000.000/0001-01",
    val inscricaoEstadual: String = "99.123.456-78",
    val enderecoLoja: String = "Rua do Mercado, 120 - Centro",
    val cidadeLoja: String = "Manaus/AM • CEP: 69000-000",
    val itens: List<ItemNota>,
    val desconto: Double = 0.0,
    val taxaEntrega: Double = 0.0,
    val pagamento: String,
    val troco: Double = 0.0,
    val consumidor: String,
    val cpf: String = "123.456.789-00",
    /** null = retirada na loja */
    val entrega: String?,
    val numero: String,
    val serie: String = "001",
    val dataHora: Long
) {
    val subtotal: Double get() = itens.sumOf { it.total }
    val totalPago: Double get() = subtotal - desconto + taxaEntrega
}

/** Monta a nota a partir de um pedido do app. [loja] é o nome do vendedor/loja. */
fun Pedido.paraNota(loja: String): NotaFiscalDados = NotaFiscalDados(
    pedidoId = id,
    loja = loja,
    itens = itens.mapIndexed { i, item ->
        ItemNota("%03d".format(i + 1), item.produto.nome, item.kg, item.produto.preco)
    },
    taxaEntrega = taxa,
    pagamento = pagamento,
    consumidor = clienteNome,
    entrega = if (entrega) destino.replace(" – ", " - ") else null,
    numero = "%03d.%03d".format(id / 1000, id % 1000),
    dataHora = if (criadoEm > 0L) criadoEm else System.currentTimeMillis()
)

/** Exatamente os dados do modelo do Figma (usado nos Previews). */
val NotaDoFigma = NotaFiscalDados(
    pedidoId = 1048,
    loja = "Peixaria Maré Boa",
    itens = listOf(
        ItemNota("001", "Filé de tilápia fresco", 1.2, 39.90),
        ItemNota("002", "Posta de salmão fresca", 0.8, 54.90)
    ),
    desconto = 1.80,
    pagamento = "Pix",
    consumidor = "João da Silva",
    entrega = "Rua Exemplo, 45 - Bairro Flores",
    numero = "000.001",
    dataHora = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 13, 12, 42, 0)
    }.timeInMillis
)

// ---------- formatação (pt-BR) ----------

private val ptBR: Locale = Locale.forLanguageTag("pt-BR")
private val meses = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")

/** 91.8 -> "91,80" */
fun Double.numero(): String = String.format(ptBR, "%.2f", this)

/** 1.2 -> "1,200 kg" */
fun Double.kgNota(): String = String.format(ptBR, "%.3f", this) + " kg"

/** "13 nov 2026 · 12:42" */
fun dataHoraCurta(ms: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = ms }
    return "%d %s %d · %02d:%02d".format(
        c.get(Calendar.DAY_OF_MONTH), meses[c.get(Calendar.MONTH)], c.get(Calendar.YEAR),
        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)
    )
}

/** "13/11/2026 às 12:42" */
fun dataHoraNota(ms: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = ms }
    return "%02d/%02d/%04d às %02d:%02d".format(
        c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR),
        c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)
    )
}
