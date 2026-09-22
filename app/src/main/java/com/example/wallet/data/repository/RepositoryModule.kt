package com.example.wallet.data.repository

import com.example.wallet.domain.repository.AccountRepository
import com.example.wallet.domain.repository.BudgetRepository
import com.example.wallet.domain.repository.CategoryRepository
import com.example.wallet.domain.repository.InstitutionRepository
import com.example.wallet.domain.repository.LabelRepository
import com.example.wallet.domain.repository.NotificationRepository
import com.example.wallet.domain.repository.TransactionRepository
import com.example.wallet.domain.repository.TransactionSplitRepository
import com.example.wallet.domain.repository.UserRepository
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

    @Binds
    abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    abstract fun bindTransactionSplitRepository(impl: TransactionSplitRepositoryImpl): TransactionSplitRepository

    @Binds
    abstract fun bindLabelRepository(impl: LabelRepositoryImpl): LabelRepository

    @Binds
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository
}
