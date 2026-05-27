package org.noztek.esktransport.core.network

object ApiErrorParser {
    fun parse(throwable: Throwable, fallback: String): String {
        val message = throwable.message?.trim().orEmpty()
        return if (message.isNotEmpty()) message else fallback
    }
}
