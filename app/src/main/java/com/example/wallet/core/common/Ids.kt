package com.example.wallet.core.common

import java.util.UUID

/** Globally unique string IDs for every entity (plan.md §73) — simplifies future sync. */
fun newId(): String = UUID.randomUUID().toString()
