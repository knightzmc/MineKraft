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
package org.kryptonmc.krypton.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.util.Codec
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTException
import org.jglrxavpok.hephaistos.nbt.SNBTParser
import java.io.StringReader

val ADVENTURE_SNBT_CODEC: Codec<NBT, String, NBTException, RuntimeException> = Codec.of({ SNBTParser(StringReader(it)).parse() }, NBT::toSNBT)

fun String.toJsonComponent(): Component = GsonComponentSerializer.gson().deserialize(this)

fun Component.toSectionText(): String = LegacyComponentSerializer.legacySection().serialize(this)

fun Component.toAmpersandText(): String = LegacyComponentSerializer.legacyAmpersand().serialize(this)
