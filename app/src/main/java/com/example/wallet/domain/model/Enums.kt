package com.example.wallet.domain.model

/** plan.md §11 */
enum class AccountType {
    BANK,
    CASH,
    CREDIT_CARD,
    MOBILE_WALLET,
    SAVINGS,
    INVESTMENT,
    LOAN,
    OTHER,
}

/** plan.md §10 */
enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER,
    REFUND,
    ADJUSTMENT,
}

/** plan.md §14 — a category group (and everything under it) is either expense- or income-facing. */
enum class CategoryType {
    EXPENSE,
    INCOME,
}

/** plan.md §17 */
enum class BudgetPeriod {
    WEEKLY,
    MONTHLY,
    CUSTOM,
}

/** plan.md §18 */
enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
}
