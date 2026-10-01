package com.expensetracker.wallet.data.local

import androidx.room.TypeConverter
import com.expensetracker.wallet.domain.model.AccountType
import com.expensetracker.wallet.domain.model.AutomationCandidateStatus
import com.expensetracker.wallet.domain.model.BudgetPeriod
import com.expensetracker.wallet.domain.model.CategoryType
import com.expensetracker.wallet.domain.model.NotificationType
import com.expensetracker.wallet.domain.model.ParseConfidence
import com.expensetracker.wallet.domain.model.RecurringFrequency
import com.expensetracker.wallet.domain.model.TransactionSource
import com.expensetracker.wallet.domain.model.TransactionType

/** Enum <-> String converters so entities can use typed enums instead of raw strings. */
class Converters {
    @TypeConverter
    fun fromAccountType(value: AccountType): String = value.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = AccountType.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = TransactionType.valueOf(value)

    @TypeConverter
    fun fromCategoryType(value: CategoryType): String = value.name

    @TypeConverter
    fun toCategoryType(value: String): CategoryType = CategoryType.valueOf(value)

    @TypeConverter
    fun fromBudgetPeriod(value: BudgetPeriod): String = value.name

    @TypeConverter
    fun toBudgetPeriod(value: String): BudgetPeriod = BudgetPeriod.valueOf(value)

    @TypeConverter
    fun fromRecurringFrequency(value: RecurringFrequency): String = value.name

    @TypeConverter
    fun toRecurringFrequency(value: String): RecurringFrequency = RecurringFrequency.valueOf(value)

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = NotificationType.valueOf(value)

    @TypeConverter
    fun fromTransactionSource(value: TransactionSource): String = value.name

    @TypeConverter
    fun toTransactionSource(value: String): TransactionSource = TransactionSource.valueOf(value)

    @TypeConverter
    fun fromParseConfidence(value: ParseConfidence): String = value.name

    @TypeConverter
    fun toParseConfidence(value: String): ParseConfidence = ParseConfidence.valueOf(value)

    @TypeConverter
    fun fromAutomationCandidateStatus(value: AutomationCandidateStatus): String = value.name

    @TypeConverter
    fun toAutomationCandidateStatus(value: String): AutomationCandidateStatus =
        AutomationCandidateStatus.valueOf(value)
}
