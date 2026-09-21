package com.example.wallet.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class WalletDestinationTest {

    @Test
    fun `the four primary destinations have distinct routes`() {
        val routes = listOf(
            WalletDestination.Home.route,
            WalletDestination.Transactions.route,
            WalletDestination.Reports.route,
            WalletDestination.Accounts.route,
        )

        assertEquals(4, routes.distinct().size)
    }

    @Test
    fun `home is the expected start route`() {
        assertEquals("home", WalletDestination.Home.route)
    }
}
