package com.expensetracker.wallet.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.expensetracker.wallet.data.local.Converters
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
import com.expensetracker.wallet.data.local.entity.AccountEntity
import com.expensetracker.wallet.data.local.entity.AutomationCandidateEntity
import com.expensetracker.wallet.data.local.entity.BudgetCategoryEntity
import com.expensetracker.wallet.data.local.entity.BudgetEntity
import com.expensetracker.wallet.data.local.entity.CategoryEntity
import com.expensetracker.wallet.data.local.entity.CategoryGroupEntity
import com.expensetracker.wallet.data.local.entity.GoalContributionEntity
import com.expensetracker.wallet.data.local.entity.GoalEntity
import com.expensetracker.wallet.data.local.entity.InstitutionEntity
import com.expensetracker.wallet.data.local.entity.LabelEntity
import com.expensetracker.wallet.data.local.entity.MerchantAliasEntity
import com.expensetracker.wallet.data.local.entity.MerchantEntity
import com.expensetracker.wallet.data.local.entity.NotificationEntity
import com.expensetracker.wallet.data.local.entity.RecurringTransactionEntity
import com.expensetracker.wallet.data.local.entity.TemplateEntity
import com.expensetracker.wallet.data.local.entity.TransactionEntity
import com.expensetracker.wallet.data.local.entity.TransactionLabelEntity
import com.expensetracker.wallet.data.local.entity.TransactionSplitEntity
import com.expensetracker.wallet.data.local.entity.UserEntity

/**
 * v7 (plan.md §11-§19, §84, §86, §87). `notifications` and `users` arrived in migration v1 -> v2
 * (Phase 4); `transactions.transferId` arrived in migration v2 -> v3 (Phase 6, §22);
 * `recurring_transactions.autoPost` and `transactions.recurringTransactionId` arrived in
 * migration v3 -> v4 (Phase 11, plans/11-recurring-goals.md); `transactions.source`/
 * `transactions.sourceReference` and `automation_candidates` arrived in migration v4 -> v5
 * (Phase 14, plans/14-sms-notification-automation.md); `transactions.place` arrived in migration
 * v5 -> v6 (the redesigned transaction form's "Place" field); `templates` arrived in migration
 * v6 -> v7 (the transaction form's fixed account/label/payee/place shortcut) — see Migrations.kt.
 * `exportSchema = true` remains on: every future change goes through a real migration (see
 * androidTest/.../AppDatabaseMigrationTest.kt for the harness).
 *
 * Still deliberately absent: `sync_operations` (Phase 17) and every §12/§74 "future field" not
 * yet needed by a built phase (version, ...) — each arrives via its own migration when the phase
 * that needs it is built, per §69 rule 11.
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
        AutomationCandidateEntity::class,
        TemplateEntity::class,
    ],
    version = 7,
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
    abstract fun automationCandidateDao(): AutomationCandidateDao
    abstract fun templateDao(): TemplateDao
}
