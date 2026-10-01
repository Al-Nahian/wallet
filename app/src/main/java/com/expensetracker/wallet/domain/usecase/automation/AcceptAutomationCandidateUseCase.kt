package com.expensetracker.wallet.domain.usecase.automation

import com.expensetracker.wallet.domain.model.AutomationCandidateStatus
import com.expensetracker.wallet.domain.model.TransactionType
import com.expensetracker.wallet.domain.repository.AutomationCandidateRepository
import com.expensetracker.wallet.domain.usecase.budget.CheckBudgetAlertsUseCase
import com.expensetracker.wallet.domain.usecase.transaction.CreateTransactionUseCase
import com.expensetracker.wallet.domain.usecase.transaction.CreateTransferUseCase
import com.expensetracker.wallet.domain.usecase.transaction.TransactionValidationException
import javax.inject.Inject

sealed class AcceptCandidateError {
    object CandidateNotFound : AcceptCandidateError()
    object AccountRequired : AcceptCandidateError()
    object DestinationAccountRequired : AcceptCandidateError()
    data class Underlying(val message: String) : AcceptCandidateError()
}

class AcceptCandidateException(val error: AcceptCandidateError) : Exception()

/**
 * plans/14-sms-notification-automation.md — accepts a Review Queue row exactly like the user
 * hand-entering it (plan.md §69 rule 19: automation never bypasses `CreateTransactionUseCase`/
 * `CreateTransferUseCase`), preserving the original SMS's `source`/`sourceReference` so it stays
 * traceable and the dedup check still recognizes it if the same message is ever reprocessed.
 */
class AcceptAutomationCandidateUseCase @Inject constructor(
    private val automationCandidateRepository: AutomationCandidateRepository,
    private val createTransactionUseCase: CreateTransactionUseCase,
    private val createTransferUseCase: CreateTransferUseCase,
    private val checkBudgetAlertsUseCase: CheckBudgetAlertsUseCase,
) {
    suspend operator fun invoke(
        candidateId: String,
        accountId: String?,
        toAccountId: String?,
        categoryId: String?,
        amountInput: String,
        payee: String?,
        note: String?,
        date: Long,
    ): Result<Unit> {
        val candidate = automationCandidateRepository.getById(candidateId)
            ?: return Result.failure(AcceptCandidateException(AcceptCandidateError.CandidateNotFound))
        if (accountId == null) return Result.failure(AcceptCandidateException(AcceptCandidateError.AccountRequired))

        val result = if (candidate.type == TransactionType.TRANSFER) {
            if (toAccountId == null) {
                return Result.failure(AcceptCandidateException(AcceptCandidateError.DestinationAccountRequired))
            }
            createTransferUseCase(
                fromAccountId = accountId,
                toAccountId = toAccountId,
                amountInput = amountInput,
                note = note ?: payee,
                date = date,
                source = candidate.sourceType,
                sourceReference = candidate.sourceReference,
            ).map { }
        } else {
            createTransactionUseCase(
                type = candidate.type,
                accountId = accountId,
                amountInput = amountInput,
                categoryId = categoryId,
                payee = payee,
                note = note,
                date = date,
                source = candidate.sourceType,
                sourceReference = candidate.sourceReference,
            ).map { transaction ->
                if (transaction.type == TransactionType.EXPENSE) checkBudgetAlertsUseCase()
            }
        }

        return result.fold(
            onSuccess = {
                automationCandidateRepository.updateStatus(candidateId, AutomationCandidateStatus.ACCEPTED)
                Result.success(Unit)
            },
            onFailure = { error ->
                val message = (error as? TransactionValidationException)?.error?.userMessage
                    ?: "We couldn't save this transaction. Please try again."
                Result.failure(AcceptCandidateException(AcceptCandidateError.Underlying(message)))
            },
        )
    }
}
