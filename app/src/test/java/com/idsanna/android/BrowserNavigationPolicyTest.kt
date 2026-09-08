package com.idsanna.android

import android.net.Uri
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserNavigationPolicyTest {
    @Test
    fun allowsHttpsOnlyWhenAllowListMatches() {
        val policy = BrowserNavigationPolicy(setOf("example.com"))

        assertTrue(policy.isAllowed(Uri.parse("https://example.com/path")))
        assertTrue(policy.isAllowed(Uri.parse("https://sub.example.com/path")))
        assertFalse(policy.isAllowed(Uri.parse("http://example.com/path")))
        assertFalse(policy.isAllowed(Uri.parse("https://evil.example.org/path")))
    }

    @Test
    fun defaultPolicyRejectsNonHttpsSchemes() {
        val policy = BrowserNavigationPolicy()

        assertTrue(policy.isAllowed(Uri.parse("https://example.com")))
        assertFalse(policy.isAllowed(Uri.parse("file:///sdcard/private.html")))
        assertFalse(policy.isAllowed(Uri.parse("content://private/item")))
        assertFalse(policy.isAllowed(Uri.parse("javascript:alert(1)")))
    }
}
