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
package org.kryptonmc.krypton.command.arguments.entities

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import org.kryptonmc.api.command.Sender
import org.kryptonmc.krypton.util.argument


class EntityArgument private constructor(val type: EntityType, val single: Boolean) : ArgumentType<EntityQuery> {

    override fun parse(reader: StringReader) = if (reader.canRead() && reader.peek() == '@') {
        reader.skip()
        EntityArgumentParser.parse(reader, reader.read())
    } else {
        val input = reader.readString()

        if (input.matches(PLAYER_NAME_REGEX)) {
            EntityQuery(
                playerName = input,
                type = EntityQuery.Operation.PLAYER,
                args = listOf()
            )
        } else {
            EntityQuery(args = listOf(), EntityQuery.Operation.UNKNOWN)
        }
    }

    override fun getExamples() = EXAMPLES

    companion object {

        private val EXAMPLES = listOf("Player1", "Player2", "@a", "@e", "@r", "@a[gamemode=adventure]")
        private val PLAYER_NAME_REGEX = Regex("[a-zA-Z0-9_]{1,16}")

        /**
         * @param single Whether only one player can be specified or not
         * @return An argument which can only accept players
         */
        fun player(single: Boolean = false) = EntityArgument(EntityType.PLAYER, single)

        /**
         * @param single Whether only one entity can be specified or not
         * @return An argument which can accept all entities
         */
        fun entity(single: Boolean = false) = EntityArgument(EntityType.ENTITY, single)
    }

    enum class EntityType {
        ENTITY,
        PLAYER
    }

    data class EntityArg(val name: String, val value: String, val not: Boolean)

}

fun CommandContext<Sender>.entityArgument(name: String) = argument<EntityQuery>(name)

