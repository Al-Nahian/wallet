package com.expensetracker.wallet.data.local

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.expensetracker.wallet.core.database.AppDatabase
import com.expensetracker.wallet.core.testing.buildInMemoryTestDatabase
import com.expensetracker.wallet.data.local.entity.AccountEntity
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
import com.expensetracker.wallet.data.local.entity.RecurringTransactionEntity
import com.expensetracker.wallet.data.local.entity.TransactionEntity
import com.expensetracker.wallet.data.local.entity.TransactionLabelEntity
import com.expensetracker.wallet.data.local.entity.TransactionSplitEntity
import com.expensetracker.wallet.domain.model.AccountType
import com.expensetracker.wallet.domain.model.BudgetPeriod
import com.expensetracker.wallet.domain.model.CategoryType
import com.expensetracker.wallet.domain.model.RecurringFrequency
import com.expensetracker.wallet.domain.model.TransactionType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Insert + query round-trip for every entity (plans/02-database.md acceptance criteria). */
@RunWith(AndroidJUnit4::class)
class AllDaosRoundTripTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = buildInMemoryTestDatabase()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun institutionRoundTrip() = runBlocking {
        val institution = InstitutionEntity(id = "inst-1", name = "BRAC Bank", type = "BANK")
        db.institutionDao().upsert(institution)
        assertEquals(institution, db.institutionDao().getById("inst-1"))
    }

    @Test
    fun accountRoundTrip() = runBlocking {
        val account = testAccount()
        db.accountDao().insert(account)
        assertEquals(account, db.accountDao().getById(account.id))
        assertEquals(listOf(account), db.accountDao().observeActive().first())
    }

    @Test
    fun categoryGroupAndCategoryRoundTrip() = runBlocking {
        val group = testCategoryGroup()
        db.categoryGroupDao().upsertAll(listOf(group))
        val category = testCategory(groupId = group.id)
        db.categoryDao().upsertAll(listOf(category))

        assertEquals(1, db.categoryGroupDao().count())
        assertEquals(listOf(category), db.categoryDao().observeByGroup(group.id).first())
    }

    @Test
    fun transactionAndSplitRoundTrip() = runBlocking {
        val account = testAccount()
        db.accountDao().insert(account)
        val group = testCategoryGroup()
        db.categoryGroupDao().upsertAll(listOf(group))
        val category = testCategory(groupId = group.id)
        db.categoryDao().upsertAll(listOf(category))

        val transaction = testTransaction(accountId = account.id, categoryId = category.id)
        db.transactionDao().insert(transaction)
        assertEquals(transaction, db.transactionDao().getById(transaction.id))

        val split = TransactionSplitEntity(
            id = "split-1",
            transactionId = transaction.id,
            categoryId = category.id,
            amountMinor = transaction.amountMinor,
        )
        db.transactionSplitDao().upsertAll(listOf(split))
        assertEquals(listOf(split), db.transactionSplitDao().observeByTransaction(transaction.id).first())
    }

    @Test
    fun transactionSoftDeleteExcludesFromObserveAll() = runBlocking {
        val account = testAccount()
        db.accountDao().insert(account)
        val transaction = testTransaction(accountId = account.id, categoryId = null)
        db.transactionDao().insert(transaction)

        db.transactionDao().softDelete(transaction.id, deletedAt = 999L)

        assertEquals(emptyList<TransactionEntity>(), db.transactionDao().observeAll().first())
        assertEquals(999L, db.transactionDao().getById(transaction.id)?.deletedAt)
    }

    @Test
    fun labelAndTransactionLabelRoundTrip() = runBlocking {
        val account = testAccount()
        db.accountDao().insert(account)
        val transaction = testTransaction(accountId = account.id, categoryId = null)
        db.transactionDao().insert(transaction)

        val label = LabelEntity(id = "label-1", name = "Family", color = "#FF0000", createdAt = 0L)
        db.labelDao().insert(label)
        db.transactionLabelDao().assign(TransactionLabelEntity(transaction.id, label.id))

        assertEquals(
            listOf(label),
            db.transactionLabelDao().observeLabelsForTransaction(transaction.id).first(),
        )
    }

    @Test
    fun budgetAndBudgetCategoryRoundTrip() = runBlocking {
        val group = testCategoryGroup()
        db.categoryGroupDao().upsertAll(listOf(group))
        val category = testCategory(groupId = group.id)
        db.categoryDao().upsertAll(listOf(category))

        val budget = BudgetEntity(
            id = "budget-1",
            name = "Monthly",
            period = BudgetPeriod.MONTHLY,
            startDate = 0L,
            endDate = 1L,
            amountMinor = 30_000_00,
            currency = "BDT",
            createdAt = 0L,
            updatedAt = 0L,
        )
        db.budgetDao().insert(budget)
        val budgetCategory = BudgetCategoryEntity(budget.id, category.id, limitMinor = 10_000_00)
        db.budgetCategoryDao().upsertAll(listOf(budgetCategory))

        assertEquals(budget, db.budgetDao().observeById(budget.id).first())
        assertEquals(listOf(budgetCategory), db.budgetCategoryDao().observeByBudget(budget.id).first())
    }

    @Test
    fun recurringTransactionRoundTrip() = runBlocking {
        val account = testAccount()
        db.accountDao().insert(account)

        val recurring = RecurringTransactionEntity(
            id = "recurring-1",
            accountId = account.id,
            amountMinor = 1_500_00,
            currency = "BDT",
            frequency = RecurringFrequency.MONTHLY,
            nextDate = 0L,
            type = TransactionType.EXPENSE,
        )
        db.recurringTransactionDao().insert(recurring)

        assertEquals(listOf(recurring), db.recurringTransactionDao().observeActive().first())
        assertEquals(listOf(recurring), db.recurringTransactionDao().getDue(beforeOrAt = 0L))
    }

    @Test
    fun goalAndContributionRoundTrip() = runBlocking {
        val goal = GoalEntity(
            id = "goal-1",
            name = "Emergency Fund",
            targetAmountMinor = 100_000_00,
            currentAmountMinor = 0L,
            currency = "BDT",
            createdAt = 0L,
            updatedAt = 0L,
        )
        db.goalDao().insert(goal)
        val contribution = GoalContributionEntity(
            id = "contribution-1",
            goalId = goal.id,
            amountMinor = 5_000_00,
            date = 0L,
        )
        db.goalContributionDao().insert(contribution)

        assertEquals(goal, db.goalDao().observeById(goal.id).first())
        assertEquals(listOf(contribution), db.goalContributionDao().observeByGoal(goal.id).first())
    }

    @Test
    fun merchantAndAliasRoundTrip() = runBlocking {
        val merchant = MerchantEntity(id = "merchant-1", canonicalName = "Foodpanda")
        db.merchantDao().insert(merchant)
        db.merchantDao().insertAlias(MerchantAliasEntity(merchant.id, "FOODPANDA*DHAKA"))

        assertEquals(merchant, db.merchantDao().findByAlias("FOODPANDA*DHAKA"))
    }

    private fun testAccount() = AccountEntity(
        id = "account-1",
        name = "Cash",
        type = AccountType.CASH,
        institutionId = null,
        currency = "BDT",
        openingBalanceMinor = 100_000_00,
        createdAt = 0L,
        updatedAt = 0L,
    )

    private fun testCategoryGroup() = CategoryGroupEntity(
        id = "group-1",
        name = "Food & Drinks",
        color = "#F44336",
        type = CategoryType.EXPENSE,
        sortOrder = 0,
        isSystem = true,
    )

    private fun testCategory(groupId: String) = CategoryEntity(
        id = "category-1",
        groupId = groupId,
        name = "Groceries",
        sortOrder = 0,
        isSystem = true,
    )

    private fun testTransaction(accountId: String, categoryId: String?) = TransactionEntity(
        id = "transaction-1",
        accountId = accountId,
        type = TransactionType.EXPENSE,
        amountMinor = 1_500_00,
        currency = "BDT",
        categoryId = categoryId,
        date = 0L,
        createdAt = 0L,
        updatedAt = 0L,
    )
}
