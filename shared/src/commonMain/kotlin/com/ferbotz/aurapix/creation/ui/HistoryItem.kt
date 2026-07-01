package com.ferbotz.aurapix.creation.ui

/** Presentation model for a creation card in the history grid. */
data class HistoryItem(
    val title: String,
    val category: String,
    val id: String = "",
    val imageUrl: String? = null,
    val status: String = "COMPLETED",
)

/** Placeholder history used by previews / empty defaults. */
val sampleHistory = listOf(
    HistoryItem("Midnight Portrait", "Portrait"),
    HistoryItem("Neon Dreams", "Abstract"),
    HistoryItem("Mountain Mist", "Landscape"),
    HistoryItem("Studio Headshot", "Portrait"),
    HistoryItem("Ink Wash", "Abstract"),
    HistoryItem("Coastal Light", "Landscape"),
)
