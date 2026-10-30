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
}
