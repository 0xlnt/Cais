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

    /**
     * Cria a conta no login do Supabase e grava o perfil (e a loja, se for vendedor).
     * Com a confirmação de e-mail desligada, a pessoa já fica logada depois do cadastro.
     * Se não der para gravar o perfil, sai da conta recém-criada e repassa o erro.
     */
    suspend fun cadastrar(nome: String, doc: String, email: String, senha: String, vendedor: Boolean): PerfilDto {
        cliente.auth.signUpWith(Email) {
            this.email = email
            this.password = senha
        }
        val id = cliente.auth.currentUserOrNull()?.id
            ?: error("Cadastro sem sessão (a confirmação de e-mail está ligada?)")
        try {
            cliente.postgrest.from("perfil").insert(
                PerfilNovoDto(id = id, nome = nome, cpfCnpj = doc, tipo = if (vendedor) "vendedor" else "cliente")
            )
            if (vendedor) {
                cliente.postgrest.from("vendedor").insert(
                    VendedorNovoDto(id = id, nomeLoja = nome, descricao = "Vendedor no Cais")
                )
            }
        } catch (e: Exception) {
            try { cliente.auth.signOut() } catch (_: Exception) {}
            throw e
        }
        return PerfilDto(id = id, nome = nome, cpfCnpj = doc)
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