package org.noztek.esktransport

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform