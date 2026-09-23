package com.example.wallet.domain.usecase.transaction

import com.example.wallet.domain.model.Transaction
import com.example.wallet.domain.model.TransactionType
import com.example.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory fake mirroring the real DAO's soft-delete-excludes-from-sums semantics. */
class FakeTransactionRepository : TransactionRepository {
    private val transactions = MutableStateFlow<Map<String, Transaction>>(emptyMap())

    override fun observeTransactions(): Flow<List<Transaction>> =
        transactions.map { it.values.filter { tx -> tx.deletedAt == null } }

    override fun observeByAccount(accountId: String): Flow<List<Transaction>> =
        transactions.map { it.values.filter { tx -> tx.deletedAt == null && tx.accountId == accountId } }

    override fun observeByDateRange(startInclusive: Long, endInclusive: Long): Flow<List<Transaction>> =
        transactions.map {
            it.values.filter { tx -> tx.deletedAt == null && tx.date in startInclusive..endInclusive }
        }

    override suspend fun getTransaction(id: String): Transaction? = transactions.value[id]

    override suspend fun create(transaction: Transaction) {
        transactions.value = transactions.value + (transaction.id to transaction)
    }

    override suspend fun createBatch(transactions: List<Transaction>) {
        this.transactions.value = this.transactions.value + transactions.associateBy { it.id }
    }

    override suspend fun update(transaction: Transaction) {
        transactions.value = transactions.value + (transaction.id to transaction)
    }

    override suspend fun delete(id: String) {
        val existing = transactions.value[id] ?: return
        transactions.value = transactions.value + (id to existing.copy(deletedAt = System.currentTimeMillis()))
    }

    override suspend fun sumByAccountAndType(accountId: String, type: TransactionType): Long =
        transactions.value.values
            .filter { it.deletedAt == null && it.accountId == accountId && it.type == type }
            .sumOf { it.amountMinor }

    override suspend fun sumByTypeInRange(type: TransactionType, startInclusive: Long, endInclusive: Long): Long =
        transactions.value.values
            .filter { it.deletedAt == null && it.type == type && it.date in startInclusive..endInclusive }
            .sumOf { it.amountMinor }

    override suspend fun sumByAccountAndTypeInRange(
        accountId: String,
        type: TransactionType,
        startInclusive: Long,
        endInclusive: Long,
    ): Long = transactions.value.values
        .filter {
            it.deletedAt == null && it.accountId == accountId && it.type == type &&
                it.date in startInclusive..endInclusive
        }
        .sumOf { it.amountMinor }

    override suspend fun createTransferPair(outgoing: Transaction, incoming: Transaction) {
        transactions.value = transactions.value + (outgoing.id to outgoing) + (incoming.id to incoming)
    }

    override suspend fun deleteTransferPair(transactionIds: List<String>) {
        val deletedAt = System.currentTimeMillis()
        transactions.value = transactions.value.mapValues { (id, tx) ->
            if (id in transactionIds) tx.copy(deletedAt = deletedAt) else tx
        }
    }

    override suspend fun getByTransferId(transferId: String): List<Transaction> =
        transactions.value.values.filter { it.deletedAt == null && it.transferId == transferId }
}
