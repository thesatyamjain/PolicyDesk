package com.policydesk.app

import com.policydesk.app.data.ota.OtaReleaseInfo
import com.policydesk.app.data.ota.OtaVersionComparator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OtaUpdateTest {

    @Test
    fun testSemanticVersionComparison() {
        // Newer versions
        assertTrue(OtaVersionComparator.isNewerVersion("0.1.1", "0.1.0"))
        assertTrue(OtaVersionComparator.isNewerVersion("0.2.0", "0.1.9"))
        assertTrue(OtaVersionComparator.isNewerVersion("1.0.0", "0.9.9"))
        assertTrue(OtaVersionComparator.isNewerVersion("0.1.10", "0.1.9"))
        assertTrue(OtaVersionComparator.isNewerVersion("0.1.12", "0.1.1"))
        assertTrue(OtaVersionComparator.isNewerVersion("0.1.13", "0.1.12"))
        assertTrue(OtaVersionComparator.isNewerVersion("v0.1.1", "v0.1.0"))
        assertTrue(OtaVersionComparator.isNewerVersion("v0.1.1", "0.1.0"))

        // Same versions
        assertFalse(OtaVersionComparator.isNewerVersion("0.1.0", "0.1.0"))
        assertFalse(OtaVersionComparator.isNewerVersion("1.0.0", "1.0.0"))
        assertFalse(OtaVersionComparator.isNewerVersion("v0.1.0", "0.1.0"))

        // Older versions
        assertFalse(OtaVersionComparator.isNewerVersion("0.1.0", "0.1.1"))
        assertFalse(OtaVersionComparator.isNewerVersion("0.0.9", "0.1.0"))
        assertFalse(OtaVersionComparator.isNewerVersion("0.9.9", "1.0.0"))
    }

    @Test
    fun testOtaReleaseInfoMbCalculation() {
        val info = OtaReleaseInfo(
            tagName = "v0.1.1",
            versionName = "0.1.1",
            releaseTitle = "PolicyDesk v0.1.1",
            releaseNotes = "Fixes and improvements",
            apkDownloadUrl = "https://example.com/PolicyDesk.apk",
            apkSizeBytes = 3_585_053L, // 3,585,053 / 1048576 = 3.418 MB
            publishedAt = "2026-10-30T08:06:02Z"
        )

        assertEquals("3.4 MB", info.displaySizeMb)
    }

    @Test
    fun testParseReleaseNotes() {
        val raw = """
            ## 🚀 PolicyDesk v0.1.12

            ### What's Changed
            * feat(ui): format active policy countdown into compound months and days (Option B) (cf3018f)
            * fix(ui): reconstruct AppleIcons.Person2 vector geometry with clean circular heads, rounded shoulders and negative-space separation (6cb5892)
            * chore: clean up obsolete flutter entries from .gitignore (10f3e36)
            * Add sticky actions to policy form (cd0a053)

            ---
            ### 🔒 Binary Integrity & Checksums
            | File | Size | SHA-256 Checksum |
        """.trimIndent()

        val parsed = OtaReleaseInfo.parseReleaseNotes(raw)
        assertEquals(4, parsed.size)
        assertEquals("Format active policy countdown into compound months and days (Option B)", parsed[0])
        assertEquals("Reconstruct AppleIcons.Person2 vector geometry with clean circular heads, rounded shoulders and negative-space separation", parsed[1])
        assertEquals("Clean up obsolete flutter entries from .gitignore", parsed[2])
        assertEquals("Add sticky actions to policy form", parsed[3])
    }
}
