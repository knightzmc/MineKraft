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
package org.kryptonmc.krypton.command.argument.serializer.minecraft

import io.netty.buffer.ByteBuf
import org.kryptonmc.krypton.command.argument.serializer.ArgumentSerializer
import org.kryptonmc.krypton.command.arguments.entities.EntityArgument

/**
 * A serialiser for [EntityArgument]
 */
class EntityArgumentSerializer : ArgumentSerializer<EntityArgument> {

    override fun write(argument: EntityArgument, buf: ByteBuf) {
        var mask = 0

        if (argument.singleTarget) mask = mask or 0x01
        if (argument.onlyPlayers) mask = mask or 0x02

        buf.writeByte(mask)
    }

}
