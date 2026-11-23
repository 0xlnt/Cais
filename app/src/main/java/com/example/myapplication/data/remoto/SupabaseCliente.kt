package com.example.myapplication.data.remoto

import com.example.myapplication.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

/** Cliente único do Supabase (banco, login e fotos). As chaves vêm do local.properties. */
object SupabaseCliente {
    val cliente by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Postgrest)
            install(Auth)
            install(Storage)
        }
    }
}