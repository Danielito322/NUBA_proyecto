package com.daniel.nuba.data

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp

object SupabaseConfig {
    // REEMPLAZA ESTOS VALORES CON LOS DE TU CONSOLA DE SUPABASE
    const val SUPABASE_URL = "https://lpsyrjzqxoydcothmxhk.supabase.co"
    const val SUPABASE_ANON_KEY = "sb_publishable_0rdZOsvFJXbxPJnf_txgdQ_OJ_KOL90"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            httpEngine = OkHttp.create()
            install(Auth)
            install(Postgrest)
            install(Storage)
        }
    }
}
