package com.ferbotz.aurapix.profile.ui

data class FaqItem(val question: String, val answer: String)

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
