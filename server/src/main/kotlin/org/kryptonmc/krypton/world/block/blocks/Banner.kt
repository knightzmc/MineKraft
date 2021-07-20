/*
 * This file is part of the Krypton project, licensed under the GNU General Public License v3.0
 *
 * Copyright (C) 2021 KryptonMC and the contributors of the Krypton project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.kryptonmc.krypton.world.block.blocks

import net.kyori.adventure.text.Component
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.kryptonmc.api.space.Vector
import org.kryptonmc.krypton.adventure.toJsonComponent
import org.kryptonmc.krypton.world.block.tile.BlockEntity

// TODO: Add this
@Suppress("unused")
class Banner

data class BannerEntity(
    override val position: Vector,
    override val keepPacked: Boolean,
    val customName: Component?,
    val patterns: List<BannerPattern>
) : BlockEntity("banner") {

    companion object {

        fun fromNBT(position: Vector, keepPacked: Boolean, nbt: NBTCompound): BannerEntity {
            val customName = nbt.getString("CustomName").takeIf { it.isNotEmpty() }?.toJsonComponent()
            val patterns = nbt.getList<NBTCompound>("Patterns").map {
                BannerPattern(
                    BannerPatternType.fromCode(it.getString("Pattern")),
                    BannerPatternColor.fromId(it.getInt("Color"))
                )
            }
            return BannerEntity(position, keepPacked, customName, patterns)
        }
    }
}

data class BannerPattern(
    val pattern: BannerPatternType,
    val color: BannerPatternColor
)

enum class BannerPatternType(val code: String) {

    BASE("b"),
    BOTTOM_STRIPE("bs"),
    TOP_STRIPE("ts"),
    LEFT_STRIPE("ls"),
    RIGHT_STRIPE("rs"),
    CENTER_STRIPE("cs"),
    MIDDLE_STRIPE("ms"),
    DOWN_RIGHT_STRIPE("drs"),
    DOWN_LEFT_STRIPE("dls"),
    SMALL_STRIPES("ss"),
    DIAGONAL_CROSS("cr"),
    SQUARE_CROSS("sc"),
    LEFT_OF_DIAGONAL("ld"),
    RIGHT_OF_UPSIDE_DOWN_DIAGONAL("rud"),
    LEFT_OF_UPSIDE_DOWN_DIAGONAL("lud"),
    RIGHT_OF_DIAGONAL("rd"),
    LEFT_VERTICAL_HALF("vh"),
    RIGHT_VERTICAL_HALF("vhr"),
    TOP_HORIZONTAL_HALF("hh"),
    BOTTOM_HORIZONTAL_HALF("hhb"),
    BOTTOM_LEFT_CORNER("bl"),
    BOTTOM_RIGHT_CORNER("br"),
    TOP_LEFT_CORNER("tl"),
    TOP_RIGHT_CORNER("tr"),
    BOTTOM_TRIANGLE("bt"),
    TOP_TRIANGLE("tt"),
    BOTTOM_TRIANGLE_SAWTOOTH("bts"),
    TOP_TRIANGLE_SAWTOOTH("tts"),
    MIDDLE_CIRCLE("mc"),
    MIDDLE_RHOMBUS("mr"),
    BORDER("bo"),
    CURLY_BORDER("cbo"),
    BRICK("bri"),
    GRADIENT("gra"),
    UPSIDE_DOWN_GRADIENT("gru"),
    CREEPER("cre"),
    SKULL("sku"),
    FLOWER("flo"),
    MOJANG("moj"),
    GLOBE("glb"),
    PIGLIN("pig");

    companion object {

        private val VALUES = values().associateBy { it.code }

        fun fromCode(code: String) = VALUES.getValue(code)
    }
}

enum class BannerPatternColor {

    WHITE,
    ORANGE,
    MAGENTA,
    LIGHT_BLUE,
    YELLOW,
    LIME,
    PINK,
    GRAY,
    LIGHT_GRAY,
    CYAN,
    PURPLE,
    BLUE,
    BROWN,
    GREEN,
    RED,
    BLACK;

    companion object {

        private val VALUES = values().associateBy { it.ordinal }

        fun fromId(id: Int) = VALUES.getValue(id)
    }
}
