package com.expensetracker.wallet.core.database

import android.content.Context
import androidx.room.Room
import com.expensetracker.wallet.data.local.dao.AccountDao
import com.expensetracker.wallet.data.local.dao.AutomationCandidateDao
import com.expensetracker.wallet.data.local.dao.BudgetCategoryDao
import com.expensetracker.wallet.data.local.dao.BudgetDao
import com.expensetracker.wallet.data.local.dao.CategoryDao
import com.expensetracker.wallet.data.local.dao.CategoryGroupDao
import com.expensetracker.wallet.data.local.dao.GoalContributionDao
import com.expensetracker.wallet.data.local.dao.GoalDao
import com.expensetracker.wallet.data.local.dao.InstitutionDao
import com.expensetracker.wallet.data.local.dao.LabelDao
import com.expensetracker.wallet.data.local.dao.MerchantDao
import com.expensetracker.wallet.data.local.dao.NotificationDao
import com.expensetracker.wallet.data.local.dao.RecurringTransactionDao
import com.expensetracker.wallet.data.local.dao.TemplateDao
import com.expensetracker.wallet.data.local.dao.TransactionDao
import com.expensetracker.wallet.data.local.dao.TransactionLabelDao
import com.expensetracker.wallet.data.local.dao.TransactionSplitDao
import com.expensetracker.wallet.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "wallet.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
            .build()

    @Provides
    fun provideInstitutionDao(db: AppDatabase): InstitutionDao = db.institutionDao()

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideTransactionSplitDao(db: AppDatabase): TransactionSplitDao = db.transactionSplitDao()

    @Provides
    fun provideCategoryGroupDao(db: AppDatabase): CategoryGroupDao = db.categoryGroupDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideLabelDao(db: AppDatabase): LabelDao = db.labelDao()

    @Provides
    fun provideTransactionLabelDao(db: AppDatabase): TransactionLabelDao = db.transactionLabelDao()

    @Provides
    fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()

    @Provides
    fun provideBudgetCategoryDao(db: AppDatabase): BudgetCategoryDao = db.budgetCategoryDao()

    @Provides
    fun provideRecurringTransactionDao(db: AppDatabase): RecurringTransactionDao =
        db.recurringTransactionDao()

    @Provides
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideGoalContributionDao(db: AppDatabase): GoalContributionDao = db.goalContributionDao()

    @Provides
    fun provideMerchantDao(db: AppDatabase): MerchantDao = db.merchantDao()

    @Provides
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideAutomationCandidateDao(db: AppDatabase): AutomationCandidateDao = db.automationCandidateDao()

    @Provides
    fun provideTemplateDao(db: AppDatabase): TemplateDao = db.templateDao()
}
