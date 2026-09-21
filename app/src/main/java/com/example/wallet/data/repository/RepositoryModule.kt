package com.example.wallet.data.repository

import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.InstitutionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    abstract fun bindInstitutionRepository(impl: InstitutionRepositoryImpl): InstitutionRepository
}
