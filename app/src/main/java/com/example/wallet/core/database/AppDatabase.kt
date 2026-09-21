package com.example.wallet.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.wallet.data.local.Converters
import com.example.wallet.data.local.dao.AccountDao
import com.example.wallet.data.local.dao.BudgetCategoryDao
import com.example.wallet.data.local.dao.BudgetDao
import com.example.wallet.data.local.dao.CategoryDao
import com.example.wallet.data.local.dao.CategoryGroupDao
import com.example.wallet.data.local.dao.GoalContributionDao
import com.example.wallet.data.local.dao.GoalDao
import com.example.wallet.data.local.dao.InstitutionDao
import com.example.wallet.data.local.dao.LabelDao
import com.example.wallet.data.local.dao.MerchantDao
import com.example.wallet.data.local.dao.NotificationDao
import com.example.wallet.data.local.dao.RecurringTransactionDao
import com.example.wallet.data.local.dao.TransactionDao
import com.example.wallet.data.local.dao.TransactionLabelDao
import com.example.wallet.data.local.dao.TransactionSplitDao
import com.example.wallet.data.local.dao.UserDao
import com.example.wallet.data.local.entity.AccountEntity
import com.example.wallet.data.local.entity.BudgetCategoryEntity
import com.example.wallet.data.local.entity.BudgetEntity
import com.example.wallet.data.local.entity.CategoryEntity
import com.example.wallet.data.local.entity.CategoryGroupEntity
import com.example.wallet.data.local.entity.GoalContributionEntity
import com.example.wallet.data.local.entity.GoalEntity
import com.example.wallet.data.local.entity.InstitutionEntity
import com.example.wallet.data.local.entity.LabelEntity
import com.example.wallet.data.local.entity.MerchantAliasEntity
import com.example.wallet.data.local.entity.MerchantEntity
import com.example.wallet.data.local.entity.NotificationEntity
import com.example.wallet.data.local.entity.RecurringTransactionEntity
import com.example.wallet.data.local.entity.TransactionEntity
import com.example.wallet.data.local.entity.TransactionLabelEntity
import com.example.wallet.data.local.entity.TransactionSplitEntity
import com.example.wallet.data.local.entity.UserEntity

/**
 * v2 (plan.md §11-§19, §84, §86). `notifications` and `users` arrived in migration v1 -> v2
 * (Phase 4, see Migrations.kt) — the first real, tested migration since Phase 2's initial
 * schema. `exportSchema = true` remains on: every future change goes through a real migration
 * (see androidTest/.../AppDatabaseMigrationTest.kt for the harness).
 *
 * Still deliberately absent: `sync_operations` (Phase 17) and every §12/§74 "future field" not
 * yet needed by a built phase (transferId, source, confidence, version, ...) — each arrives via
 * its own migration when the phase that needs it is built, per §69 rule 11.
 */
@Database(
    entities = [
        InstitutionEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        TransactionSplitEntity::class,
        CategoryGroupEntity::class,
        CategoryEntity::class,
        LabelEntity::class,
        TransactionLabelEntity::class,
        BudgetEntity::class,
        BudgetCategoryEntity::class,
        RecurringTransactionEntity::class,
        GoalEntity::class,
        GoalContributionEntity::class,
        MerchantEntity::class,
        MerchantAliasEntity::class,
        NotificationEntity::class,
        UserEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun institutionDao(): InstitutionDao
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun transactionSplitDao(): TransactionSplitDao
    abstract fun categoryGroupDao(): CategoryGroupDao
    abstract fun categoryDao(): CategoryDao
    abstract fun labelDao(): LabelDao
    abstract fun transactionLabelDao(): TransactionLabelDao
    abstract fun budgetDao(): BudgetDao
    abstract fun budgetCategoryDao(): BudgetCategoryDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun goalDao(): GoalDao
    abstract fun goalContributionDao(): GoalContributionDao
    abstract fun merchantDao(): MerchantDao
    abstract fun notificationDao(): NotificationDao
    abstract fun userDao(): UserDao
}
