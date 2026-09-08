package com.safir.scan

import java.util.Locale

object DraftFilePolicy {
    fun isProcessableDraftName(name: String): Boolean {
        if (name.startsWith(".")) return false
        val lower = name.lowercase(Locale.US)
        if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg")) return false
        if (lower.contains(".tmp.") || lower.contains("safirbase")) return false
        return true
    }
}
