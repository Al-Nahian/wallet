package com.example.wallet.domain.model

/** plan.md §86 */
data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val deepLink: String?,
    val createdAt: Long,
    val readAt: Long?,
    val relatedEntityType: String?,
    val relatedEntityId: String?,
) {
    val isUnread: Boolean get() = readAt == null
}

/**
 * plan.md §86: "Needs your attention" surfaces the types that genuinely require a decision,
 * as opposed to purely informational ones (e.g. a captured transaction that already succeeded).
 */
fun NotificationType.needsAttention(): Boolean = this in setOf(
    NotificationType.TRANSACTION_NEEDS_REVIEW,
    NotificationType.POSSIBLE_DUPLICATE,
    NotificationType.SYNC_CONFLICT,
    NotificationType.BUDGET_EXCEEDED,
)
