package com.example.myapplication.data.remoto

import io.github.jan.supabase.postgrest.postgrest

object CatalogoRemoto {
    private val db get() = SupabaseCliente.cliente.postgrest

    suspend fun categorias(): List<CategoriaDto> =
        db.from("categoria").select().decodeList<CategoriaDto>()

    suspend fun vendedores(): List<VendedorDto> =
        db.from("vendedor").select().decodeList<VendedorDto>()

    suspend fun produtos(): List<ProdutoDto> =
        db.from("produto").select().decodeList<ProdutoDto>()

    /** Cadastra um produto. O RLS só deixa o vendedor dono (id_vendedor = auth.uid()) inserir. */
    suspend fun criarProduto(produto: ProdutoNovoDto) {
        db.from("produto").insert(produto)
    }

    suspend fun definirDisponibilidade(idProduto: Int, disponivel: Boolean) {
        db.from("produto").update({ set("disponivel", disponivel) }) {
            filter { eq("id_produto", idProduto) }
        }
    }

    suspend fun definirEstoque(idProduto: Int, estoqueKg: Double) {
        db.from("produto").update({ set("estoque_kg", estoqueKg) }) {
            filter { eq("id_produto", idProduto) }
        }
    }
}
