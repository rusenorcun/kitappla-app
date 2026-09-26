package com.kitappla.app.core.image

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoverImageTest {
    @Test
    fun cameraPhotoIsSampledThenScaledToTheMaxEdge() {
        assertEquals(2, CoverImage.sampleSize(4032, 3024))           // 2016 px ile çözülür
        assertEquals(1280 to 960, CoverImage.targetSize(2016, 1512))
        assertEquals(1, CoverImage.sampleSize(1000, 800))
        assertEquals(1000 to 800, CoverImage.targetSize(1000, 800))
    }

    @Test
    fun smallSupportedImagesAreSentUntouched() {
        assertFalse(CoverImage.needsShrinking(400_000, "image/png", 900, 1200))
        assertFalse(CoverImage.needsShrinking(200_000, "image/gif", 300, 300))
    }

    @Test
    fun bigUnsupportedOrHugeImagesAreShrunk() {
        assertTrue("3 MB sınırını aşar", CoverImage.needsShrinking(4_500_000, "image/jpeg", 4032, 3024))
        assertTrue("HEIC sunucuda desteklenmez", CoverImage.needsShrinking(300_000, "image/heic", 1000, 1000))
        assertTrue("çözünürlük çok yüksek", CoverImage.needsShrinking(900_000, "image/jpeg", 6000, 4000))
    }
}
