package com.expensetracker.wallet.core.design.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.CarRepair
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.LocalPostOffice
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Yard
import androidx.compose.ui.graphics.vector.ImageVector

/** A distinct icon per name from [com.expensetracker.wallet.data.local.seed.CategorySeed] — every
 * group and subcategory gets its own glyph instead of the same generic tag, so the category
 * picker's list of colored bubbles is actually scannable. Keyed by exact seed name (case
 * sensitive) rather than a stored id, since [Category]/[CategoryGroup] carry no icon of their
 * own yet; a name a user typed themselves (a custom group/category) falls back to a generic tag,
 * which is the previous behavior for every entry. */
private val groupIconsByName: Map<String, ImageVector> = mapOf(
    "Food & Drinks" to Icons.Filled.Restaurant,
    "Shopping" to Icons.Filled.ShoppingBag,
    "Housing" to Icons.Filled.Home,
    "Transportation" to Icons.Filled.Commute,
    "Vehicle" to Icons.Filled.DirectionsCar,
    "Life & Entertainment" to Icons.Filled.Celebration,
    "Communication, PC" to Icons.Filled.Computer,
    "Financial Expense" to Icons.Filled.RequestQuote,
    "Income" to Icons.Filled.Payments,
    "Others" to Icons.Filled.Category,
)

private val categoryIconsByName: Map<String, ImageVector> = mapOf(
    // Food & Drinks
    "Food & Drinks" to Icons.Filled.Restaurant,
    "Bar, Cafe" to Icons.Filled.LocalCafe,
    "Restaurant, Fast-food" to Icons.Filled.Fastfood,
    "Groceries" to Icons.Filled.LocalGroceryStore,
    // Shopping
    "Shopping" to Icons.Filled.ShoppingBag,
    "Drug-store, chemist" to Icons.Filled.LocalPharmacy,
    "Leisure time" to Icons.Filled.Weekend,
    "Stationery, tools" to Icons.Filled.Build,
    "Gifts, joy" to Icons.Filled.CardGiftcard,
    "Electronics, accessories" to Icons.Filled.Devices,
    "Pets, animals" to Icons.Filled.Pets,
    "Home, garden" to Icons.Filled.Yard,
    "Kids" to Icons.Filled.ChildCare,
    "Health and beauty" to Icons.Filled.Spa,
    "Jewels, accessories" to Icons.Filled.Diamond,
    "Clothes & Footwear" to Icons.Filled.Checkroom,
    // Housing
    "Housing" to Icons.Filled.Home,
    "Property insurance" to Icons.Filled.Shield,
    "Maintenance, repairs" to Icons.Filled.Build,
    "Services" to Icons.Filled.Handyman,
    "Energy, utilities" to Icons.Filled.Bolt,
    "Mortgage" to Icons.Filled.HomeWork,
    "Rent" to Icons.Filled.VpnKey,
    // Transportation
    "Transportation" to Icons.Filled.Commute,
    "Business trips" to Icons.Filled.BusinessCenter,
    "Long distance" to Icons.Filled.Flight,
    "Taxi" to Icons.Filled.LocalTaxi,
    "Public transport" to Icons.Filled.DirectionsBus,
    "Uber / Pathao / inDrive" to Icons.Filled.TwoWheeler,
    // Vehicle
    "Leasing" to Icons.Filled.RequestQuote,
    "Vehicle insurance" to Icons.Filled.VerifiedUser,
    "Rental" to Icons.Filled.CarRental,
    "Parking" to Icons.Filled.LocalParking,
    "Fuel" to Icons.Filled.LocalGasStation,
    "Maintenance" to Icons.Filled.CarRepair,
    // Life & Entertainment
    "Life & Entertainment" to Icons.Filled.Celebration,
    "Alcohol / Tobacco" to Icons.Filled.LocalBar,
    "Charity / Gifts" to Icons.Filled.VolunteerActivism,
    "Holiday / Trips / Hotels" to Icons.Filled.BeachAccess,
    "TV / Streaming" to Icons.Filled.Tv,
    "Books / Audio / Subscriptions" to Icons.AutoMirrored.Filled.MenuBook,
    "Life Events" to Icons.Filled.Cake,
    "Culture / Sports Events" to Icons.Filled.TheaterComedy,
    "Active Sport / Fitness" to Icons.Filled.FitnessCenter,
    "Wellness / Beauty" to Icons.Filled.SelfImprovement,
    "Health Care / Doctor" to Icons.Filled.LocalHospital,
    // Communication, PC
    "Communication / PC" to Icons.Filled.Computer,
    "Postal Services" to Icons.Filled.LocalPostOffice,
    "Internet" to Icons.Filled.Wifi,
    "Telephone / Mobile Phone" to Icons.Filled.Smartphone,
    // Financial Expense
    "Financial Expense" to Icons.Filled.RequestQuote,
    "Child Support" to Icons.Filled.FamilyRestroom,
    "Charges & Fees" to Icons.Filled.Receipt,
    "Advisory" to Icons.Filled.SupportAgent,
    "Fines" to Icons.Filled.Gavel,
    "Loans / Interests" to Icons.Filled.AccountBalance,
    "Investments" to Icons.AutoMirrored.Filled.ShowChart,
    // Income
    "Income" to Icons.Filled.Payments,
    "Gifts" to Icons.Filled.CardGiftcard,
    "Refunds (Tax, Purchase)" to Icons.AutoMirrored.Filled.Undo,
    "Lending / Renting" to Icons.Filled.Handshake,
    "Dues & Grants" to Icons.Filled.AccountBalanceWallet,
    "Wages / Invoices" to Icons.Filled.Work,
    // Others
    "Others" to Icons.Filled.Category,
    "Missing" to Icons.AutoMirrored.Filled.HelpOutline,
)

/** The icon for a [com.expensetracker.wallet.domain.model.CategoryGroup] row in the category picker —
 * falls back to a generic tag for a custom group the user created (not part of the seed). */
fun categoryGroupIcon(groupName: String): ImageVector = groupIconsByName[groupName] ?: Icons.Filled.Sell

/** The icon for a leaf [com.expensetracker.wallet.domain.model.Category] row — same fallback as
 * [categoryGroupIcon] for a custom subcategory. */
fun categoryIcon(categoryName: String): ImageVector = categoryIconsByName[categoryName] ?: Icons.Filled.Sell
