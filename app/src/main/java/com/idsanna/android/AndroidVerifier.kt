package com.idsanna.android

import android.accessibilityservice.AccessibilityService

class AndroidVerifier(private val service: AccessibilityService) {
    fun verifyPackage(expectedPackage: String): VerificationResult {
        val actual = service.rootInActiveWindow?.packageName?.toString()
        return if (actual == expectedPackage) VerificationResult(true, "foreground_package_verified")
        else VerificationResult(false, "expected_package=$expectedPackage actual=${actual ?: "none"}")
    }

    fun verifyVisibleText(expectedText: String): VerificationResult {
        val observation = IdsannaAccessibilityService.observations.latest()
        val found = observation?.nodes?.any { it.text == expectedText || it.contentDescription == expectedText } == true
        return VerificationResult(found, if (found) "visible_text_verified" else "visible_text_not_found")
    }
}

data class VerificationResult(val success: Boolean, val evidence: String)
