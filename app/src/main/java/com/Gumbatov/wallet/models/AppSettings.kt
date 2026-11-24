package com.Gumbatov.wallet.models

/** App theme: follow the system or force light / dark. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Currency shown after every amount. Amounts themselves are not converted. */
enum class Currency(val symbol: String) {
    AZN("₼"),
    RUB("₽"),
    USD("$"),
    EUR("€")
}