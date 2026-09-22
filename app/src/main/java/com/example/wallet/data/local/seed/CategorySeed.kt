package com.example.wallet.data.local.seed

import com.example.wallet.domain.model.CategoryType

/**
 * The default category taxonomy from plan.md §14, verbatim — 10 groups, exact names and
 * colors. Kept as a plain Kotlin structure (no Room dependency) so it's unit-testable
 * without a database, per plans/02-database.md's `CategorySeedTest`.
 */
data class CategoryGroupSeed(
    val name: String,
    val color: String,
    val type: CategoryType,
    val subcategories: List<String>,
)

object CategorySeed {
    val groups: List<CategoryGroupSeed> = listOf(
        CategoryGroupSeed(
            name = "Food & Drinks",
            color = "#F44336",
            type = CategoryType.EXPENSE,
            subcategories = listOf(
                "Food & Drinks",
                "Bar, Cafe",
                "Restaurant, Fast-food",
                "Groceries",
            ),
        ),
        CategoryGroupSeed(
            name = "Shopping",
            color = "#42B5E8",
            type = CategoryType.EXPENSE,
            subcategories = listOf(
                "Shopping",
                "Drug-store, chemist",
                "Leisure time",
                "Stationery, tools",
                "Gifts, joy",
                "Electronics, accessories",
                "Pets, animals",
                "Home, garden",
                "Kids",
                "Health and beauty",
                "Jewels, accessories",
                "Clothes & Footwear",
            ),
        ),
        CategoryGroupSeed(
            name = "Housing",
            color = "#FF9F1C",
            type = CategoryType.EXPENSE,
            subcategories = listOf(
                "Housing",
                "Property insurance",
                "Maintenance, repairs",
                "Services",
                "Energy, utilities",
                "Mortgage",
                "Rent",
            ),
        ),
        CategoryGroupSeed(
            name = "Transportation",
            color = "#78909C",
            type = CategoryType.EXPENSE,
            subcategories = listOf(
                "Transportation",
                "Business trips",
                "Long distance",
                "Taxi",
                "Public transport",
                "Uber / Pathao / inDrive",
            ),
        ),
        CategoryGroupSeed(
            name = "Vehicle",
            color = "#9C6ADE",
            type = CategoryType.EXPENSE,
            subcategories = listOf(
                "Leasing",
                "Vehicle insurance",
                "Rental",
                "Parking",
                "Fuel",
                "Maintenance",
            ),
        ),
        CategoryGroupSeed(
            name = "Life & Entertainment",
            color = "#4CAF50",
            type = CategoryType.EXPENSE,
            subcategories = listOf(
                "Life & Entertainment",
                "Alcohol / Tobacco",
                "Charity / Gifts",
                "Holiday / Trips / Hotels",
                "TV / Streaming",
                "Books / Audio / Subscriptions",
                "Life Events",
                "Culture / Sports Events",
                "Active Sport / Fitness",
                "Wellness / Beauty",
                "Health Care / Doctor",
            ),
        ),
        CategoryGroupSeed(
            name = "Communication, PC",
            color = "#B39DDB",
            type = CategoryType.EXPENSE,
            subcategories = listOf(
                "Communication / PC",
                "Postal Services",
                "Internet",
                "Telephone / Mobile Phone",
            ),
        ),
        CategoryGroupSeed(
            name = "Financial Expense",
            color = "#26A69A",
            type = CategoryType.EXPENSE,
            subcategories = listOf(
                "Financial Expense",
                "Child Support",
                "Charges & Fees",
                "Advisory",
                "Fines",
                "Loans / Interests",
                "Investments",
            ),
        ),
        CategoryGroupSeed(
            name = "Income",
            color = "#2E8B57",
            type = CategoryType.INCOME,
            subcategories = listOf(
                "Income",
                "Gifts",
                "Child Support",
                "Refunds (Tax, Purchase)",
                "Lending / Renting",
                "Dues & Grants",
                "Wages / Invoices",
            ),
        ),
        CategoryGroupSeed(
            name = "Others",
            color = "#9E9E9E",
            type = CategoryType.EXPENSE,
            subcategories = listOf(
                "Others",
                "Missing",
            ),
        ),
    )
}
