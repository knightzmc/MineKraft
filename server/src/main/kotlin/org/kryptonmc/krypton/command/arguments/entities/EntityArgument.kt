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
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import org.kryptonmc.api.adventure.toMessage
import org.kryptonmc.api.command.Sender
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.util.argument


class EntityArgument private constructor(
    val onlyPlayers: Boolean,
    val singleTarget: Boolean,
    val server: KryptonServer
) :
    ArgumentType<EntityQuery> {

    override fun parse(reader: StringReader) = if (reader.canRead() && reader.peek() == '@') {
        reader.skip()
        if (!reader.canRead()) throw MISSING_SELECTOR.createWithContext(reader)
        val position = reader.cursor
        EntityArgumentParser.parse(reader, reader.read(), position, onlyPlayers, singleTarget, server)
    } else {
        val input = reader.readString()

        if (input.matches(PLAYER_NAME_REGEX)) {
            EntityQuery(
                playerName = input,
                type = EntityQuery.SELECTOR.PLAYER,
                args = listOf()
            )
        } else {
            EntityQuery(args = listOf(), EntityQuery.SELECTOR.UNKNOWN)
        }
    }

    override fun getExamples() = EXAMPLES

    companion object {

        private val EXAMPLES = listOf("Player1", "Player2", "@a", "@e", "@r", "@a[gamemode=adventure]")
        private val PLAYER_NAME_REGEX = Regex("[a-zA-Z0-9_]{1,16}")


        /**
         * @return An argument which can only accept one player
         */
        fun player(server: KryptonServer) = EntityArgument(onlyPlayers = true, singleTarget = true, server = server)

        /**
         * @return An argument which can only accept players
         */
        fun players(server: KryptonServer) = EntityArgument(onlyPlayers = true, singleTarget = false, server = server)


        /**
         * @return An argument which can accept one entity
         */
        fun entity(server: KryptonServer) = EntityArgument(onlyPlayers = false, singleTarget = true, server = server)

        /**
         * @return An argument which can accept entities
         */
        fun entities(server: KryptonServer) = EntityArgument(onlyPlayers = false, singleTarget = false, server = server)
    }

    data class EntityArg(val name: String, val value: String, val not: Boolean)

}

fun CommandContext<Sender>.entityArgument(name: String) = argument<EntityQuery>(name)

val MISSING_SELECTOR = SimpleCommandExceptionType(translatable("argument.entity.selector.missing").toMessage())
val UNKNOWN_SELECTOR_EXCEPTION = DynamicCommandExceptionType { e ->
    translatable(
        "argument.entity.selector.unknown",
        listOf(text(e.toString()))
    ).toMessage()
}
val NOT_ALLOWED_EXCEPTION = SimpleCommandExceptionType(translatable("argument.entity.selector.not_allowed").toMessage())
val UNTERMINATED_EXCEPTION =
    SimpleCommandExceptionType(translatable("argument.entity.options.unterminated").toMessage())
val VALUELESS_EXCEPTION = DynamicCommandExceptionType { e ->
    translatable(
        "argument.entity.options.valueless",
        listOf(text(e.toString()))
    ).toMessage()
}
val INVALID_OPTION = DynamicCommandExceptionType { e ->
    translatable(
        "argument.entity.options.unknown",
        listOf(text(e.toString()))
    ).toMessage()
}
val PLAYER_NOT_FOUND = SimpleCommandExceptionType(translatable("argument.entity.notfound.player").toMessage())
val ENTITY_NOT_FOUND = SimpleCommandExceptionType(translatable("argument.entity.notfound.entity").toMessage())
val TOO_MANY_ENTITIES = SimpleCommandExceptionType(translatable("argument.entity.toomany").toMessage())
val TOO_MANY_PLAYERS = SimpleCommandExceptionType(translatable("argument.player.toomany").toMessage())
val ONLY_FOR_PLAYERS = SimpleCommandExceptionType(translatable("argument.player.entities").toMessage())
val PLAYER_NOT_EXISTS = SimpleCommandExceptionType(translatable("argument.player.unknown").toMessage())
val LIMIT_NULL = SimpleCommandExceptionType(translatable("argument.entity.options.limit.toosmall").toMessage())
