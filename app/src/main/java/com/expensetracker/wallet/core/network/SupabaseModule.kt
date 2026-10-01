package com.expensetracker.wallet.core.network

import com.expensetracker.wallet.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.ExternalAuthAction
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        install(Auth) {
            // OAuth providers (Google/Azure) return to this scheme+host via Custom Tabs;
            // see AndroidManifest.xml's second intent-filter on MainActivity.
            host = "login-callback"
            scheme = "com.expensetracker.wallet"
            defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
        }
        install(Postgrest)
    }
}
