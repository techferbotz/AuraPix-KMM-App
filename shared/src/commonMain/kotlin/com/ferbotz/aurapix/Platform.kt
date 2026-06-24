package com.ferbotz.aurapix

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform