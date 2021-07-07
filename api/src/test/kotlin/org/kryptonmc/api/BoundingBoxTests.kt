/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api

import org.kryptonmc.api.block.BoundingBox
import kotlin.test.Test
import kotlin.test.assertEquals

class BoundingBoxTests {

    @Test
    fun `test size of box calculations`() {
        val box = BoundingBox(-3.0, -3.0, -3.0, 3.0, 3.0, 3.0)
        assertEquals(box.sizeX, 6.0)
        assertEquals(box.sizeY, 6.0)
        assertEquals(box.sizeZ, 6.0)
    }

    @Test
    fun `test volume calculation`() {
        val box = BoundingBox(-3.0, -3.0, -3.0, 3.0, 3.0, 3.0)
        val expectedVolume = 6.0 * 6.0 * 6.0 // size.x * size.y * size.z
        assertEquals(box.volume, expectedVolume)
    }

    @Test
    fun `test center calculation`() {
        val box = BoundingBox(-3.0, -3.0, -3.0, 3.0, 3.0, 3.0)
        assertEquals(box.centerX, -3.0 + 6.0 * 0.5)
        assertEquals(box.centerY, -3.0 + 6.0 * 0.5)
        assertEquals(box.centerZ, -3.0 + 6.0 * 0.5)
    }
}
