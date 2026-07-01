package com.ferbotz.aurapix.billing.ui

data class CreditPack(
    val name: String,
    val credits: Int,
    val price: String,
    val perks: List<String>,
    val best: Boolean = false,
)

val sampleCreditPacks = listOf(
    CreditPack("Starter", 20, "$5.00", listOf("20 generations", "Standard quality")),
    CreditPack("Creator", 50, "$12.00", listOf("50 generations", "HD quality")),
    CreditPack("Artist", 100, "$20.00", listOf("100 generations", "HD quality", "Priority queue")),
    CreditPack("Professional", 500, "$80.00", listOf("500 generations", "4K quality", "Priority queue", "Commercial license"), best = true),
)
