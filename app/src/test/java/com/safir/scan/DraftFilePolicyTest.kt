package com.safir.scan

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DraftFilePolicyTest {
    @Test
    fun acceptsScannerJpegPages() {
        assertTrue(DraftFilePolicy.isProcessableDraftName("page_20260908_001.jpg"))
        assertTrue(DraftFilePolicy.isProcessableDraftName("import_20260908_001.JPEG"))
    }

    @Test
    fun rejectsHiddenEditorAndTemporaryFiles() {
        assertFalse(DraftFilePolicy.isProcessableDraftName(".page.jpg.safirbase.jpg"))
        assertFalse(DraftFilePolicy.isProcessableDraftName("page.opencv.tmp.jpg"))
        assertFalse(DraftFilePolicy.isProcessableDraftName("page.crop.tmp.jpg"))
        assertFalse(DraftFilePolicy.isProcessableDraftName("document.pdf"))
        assertFalse(DraftFilePolicy.isProcessableDraftName(""))
    }
}
