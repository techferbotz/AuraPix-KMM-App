package com.ferbotz.aurapix.ui.screens

/**
 * Static sample content used to populate the UI-only screens (no view models yet).
 * Real data sources replace these later; image URLs are intentionally null so the
 * NetworkImage placeholders render.
 */

data class TemplateItem(
    val name: String,
    val premium: Boolean = false,
    val id: String = "",
    val thumbnailUrl: String? = null,
)

data class HistoryItem(
    val title: String,
    val category: String,
    val id: String = "",
    val imageUrl: String? = null,
    val status: String = "COMPLETED",
)

data class PopularItem(val name: String, val category: String, val uses: String)

data class CreditPack(
    val name: String,
    val credits: Int,
    val price: String,
    val perks: List<String>,
    val best: Boolean = false,
)

data class FaqItem(val question: String, val answer: String)

val sampleCategories = listOf("Portrait", "Anime", "Wedding", "Professional", "Fun")

val sampleFeatured = listOf(
    "Cinematic Noir" to "Dramatic studio lighting",
    "Golden Hour" to "Warm, sunlit portraits",
)

val sampleTemplates = listOf(
    TemplateItem("Studio Pro", premium = true),
    TemplateItem("Anime Hero"),
    TemplateItem("Vintage Film"),
    TemplateItem("Cyberpunk", premium = true),
    TemplateItem("Renaissance"),
    TemplateItem("Watercolor"),
)

val sampleHistory = listOf(
    HistoryItem("Midnight Portrait", "Portrait"),
    HistoryItem("Neon Dreams", "Abstract"),
    HistoryItem("Mountain Mist", "Landscape"),
    HistoryItem("Studio Headshot", "Portrait"),
    HistoryItem("Ink Wash", "Abstract"),
    HistoryItem("Coastal Light", "Landscape"),
)

val sampleRecentSearches = listOf("Cyberpunk", "Medieval", "Pixar style")

val samplePopular = listOf(
    PopularItem("Cyberpunk Neon", "Sci-Fi", "12.4k uses"),
    PopularItem("Royal Portrait", "Classic", "9.1k uses"),
    PopularItem("Anime Studio", "Anime", "21.7k uses"),
    PopularItem("Film Noir", "Cinematic", "5.3k uses"),
)

val sampleCreditPacks = listOf(
    CreditPack("Starter", 20, "$5.00", listOf("20 generations", "Standard quality")),
    CreditPack("Creator", 50, "$12.00", listOf("50 generations", "HD quality")),
    CreditPack("Artist", 100, "$20.00", listOf("100 generations", "HD quality", "Priority queue")),
    CreditPack("Professional", 500, "$80.00", listOf("500 generations", "4K quality", "Priority queue", "Commercial license"), best = true),
)

val sampleFaqs = listOf(
    FaqItem(
        "Why did my generation fail?",
        "Generations can fail due to an unstable connection, a busy server, or photos that don't meet the guidelines. Retrying usually resolves it.",
    ),
    FaqItem(
        "How do credits work?",
        "Each image generation uses one credit. Credits never expire and can be topped up any time from the Purchase Credits screen.",
    ),
    FaqItem(
        "How many photos should I upload?",
        "Upload at least 3 clear, front-facing photos for the best results. More variety means a more accurate model.",
    ),
    FaqItem(
        "Can I use results commercially?",
        "Commercial usage is included with the yearly Premium plan and the Professional credit pack.",
    ),
)
