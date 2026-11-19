package com.example.myapplication.data.remoto
import io.github.jan.supabase.postgrest.postgrest
object CatalogoRemoto{
    private val db get() = SupabaseCliente.cliente.postgrest
    suspend fun categorias(): List<CategoriaDto> =
        db.from("categoria").select().decodeList<CategoriaDto>()
    suspend fun vendedores(): List<VendedorDto> =
        db.from("vendedor").select().decodeList<VendedorDto>()
    suspend fun produtos(): List<ProdutoDto> =
        db.from("produto").select().decodeList<ProdutoDto>()

}
