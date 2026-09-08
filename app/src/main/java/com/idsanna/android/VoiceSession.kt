package com.idsanna.android

import android.content.Context
import java.security.SecureRandom

class VoiceSession(private val context: Context) {
    private val prefs = context.getSharedPreferences("voice_session", Context.MODE_PRIVATE)
    private val random = SecureRandom()
    private var expiresAt: Long
        get() = prefs.getLong("expires_at", 0L)
        set(value) = prefs.edit().putLong("expires_at", value).apply()

    fun activationCode(): String {
        val existing = prefs.getString("activation_code", null)
        if (existing != null) return existing
        val code = (1000 + random.nextInt(9000)).toString()
        prefs.edit().putString("activation_code", code).apply()
        return code
    }

    fun isActive(): Boolean = System.currentTimeMillis() < expiresAt

    fun tryActivate(transcript: String): Boolean {
        val code = activationCode()
        val normalized = transcript.lowercase().replace(" ", "")
        val accepted = normalized.contains(code)
        if (accepted) expiresAt = System.currentTimeMillis() + SESSION_DURATION_MS
        return accepted
    }

    fun clear() { expiresAt = 0L }

    companion object { private const val SESSION_DURATION_MS = 5 * 60 * 1000L }
}
