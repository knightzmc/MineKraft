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
package org.kryptonmc.krypton.command.argument.serializer.brigadier

import com.mojang.brigadier.arguments.DoubleArgumentType
import io.netty.buffer.ByteBuf
import org.kryptonmc.krypton.command.argument.serializer.ArgumentSerializer

/**
 * A serialiser for Brigadier's [DoubleArgumentType]
 */
class DoubleArgumentSerializer : ArgumentSerializer<DoubleArgumentType> {

    override fun write(argument: DoubleArgumentType, buf: ByteBuf) {
        val minimum = argument.minimum != -Double.MAX_VALUE
        val maximum = argument.maximum != Double.MAX_VALUE
        buf.writeByte(createFlags(minimum, maximum))
        if (minimum) buf.writeDouble(argument.minimum)
        if (maximum) buf.writeDouble(argument.maximum)
    }
}
