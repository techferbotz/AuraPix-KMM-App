package com.ferbotz.aurapix.feed.ui

/** Placeholder content for the (not-yet-wired) search screen. */

data class PopularItem(val name: String, val category: String, val uses: String)

val sampleRecentSearches = listOf("Cyberpunk", "Medieval", "Pixar style")

val samplePopular = listOf(
    PopularItem("Cyberpunk Neon", "Sci-Fi", "12.4k uses"),
    PopularItem("Royal Portrait", "Classic", "9.1k uses"),
    PopularItem("Anime Studio", "Anime", "21.7k uses"),
    PopularItem("Film Noir", "Cinematic", "5.3k uses"),
)
