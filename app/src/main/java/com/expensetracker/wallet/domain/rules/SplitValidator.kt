package com.expensetracker.wallet.domain.rules

/**
 * plan.md §13 / §26 rule 3: split amounts must sum to exactly the parent transaction's amount.
 * Pure function, no DB dependency, so it's trivially unit-testable and reusable from both the
 * split-entry UI (Phase 6) and any future import/automation path that creates splits.
 */
fun validateSplitsSum(transactionAmountMinor: Long, splitAmountsMinor: List<Long>): Boolean =
    splitAmountsMinor.sum() == transactionAmountMinor
