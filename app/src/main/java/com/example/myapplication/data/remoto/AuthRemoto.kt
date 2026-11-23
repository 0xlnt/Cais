package com.example.myapplication.data.remoto

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
object  AuthRemoto{
    private val cliente get() = SupabaseCliente.cliente
    suspend fun entrar(email:String, senha:String): PerfilDto{
        cliente.auth.signInWith(Email){
            this.email = email
            this.password = senha
        }
        val id  = cliente.auth.currentUserOrNull()?.id?: error ("Sem usuário após o login")
        return cliente.postgrest.from("perfil")
            .select {filter {eq("id", id)}}
            .decodeSingle<PerfilDto>()
    }

    suspend fun ehVendedor(id: String): Boolean =
        cliente.postgrest.from("vendedor")
            .select { filter { eq("id", id) } }
            .decodeList<VendedorDto>()
            .isNotEmpty()

    suspend fun sair(){
        cliente.auth.signOut()
    }
}