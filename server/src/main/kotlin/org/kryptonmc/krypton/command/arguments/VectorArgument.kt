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
package org.kryptonmc.krypton.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.kyori.adventure.text.Component
import org.kryptonmc.api.adventure.toMessage
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.krypton.command.arguments.coordinates.Coordinates
import org.kryptonmc.krypton.command.arguments.coordinates.LocalCoordinates
import org.kryptonmc.krypton.command.arguments.coordinates.WorldCoordinates
import org.kryptonmc.krypton.command.suggestCoordinates
import org.kryptonmc.krypton.util.argument
import java.util.concurrent.CompletableFuture

class VectorArgument(private val correctCenter: Boolean = true) : ArgumentType<Coordinates> {

    override fun parse(reader: StringReader) =
        if (reader.canRead() && reader.peek() == '^') LocalCoordinates.parse(reader) else WorldCoordinates.parseDouble(reader, correctCenter)

    override fun <S> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        if (context.source !is Player) return Suggestions.empty()
        val remaining = builder.remaining
        val suggestions = listOf(if (remaining.isNotEmpty() && remaining[0] == '^') TextCoordinates.CENTER_LOCAL else TextCoordinates.CENTER_GLOBAL)
        return builder.suggestCoordinates(remaining, suggestions) {
            try {
                parse(StringReader(it))
                true
            } catch (exception: CommandSyntaxException) {
                false
            }
        }
    }

    override fun getExamples() = EXAMPLES

    companion object {

        private val EXAMPLES = listOf("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "0.1 -0.5 .9", "~0.5 ~1 ~-5")
    }
}

fun CommandContext<Sender>.vectorArgument(name: String) = argument<Coordinates>(name).position(source as Player)

val ERROR_NOT_COMPLETE = SimpleCommandExceptionType(Component.translatable("argument.pos3d.incomplete").toMessage())
val ERROR_MIXED_TYPE = SimpleCommandExceptionType(Component.translatable("argument.pos.mixed").toMessage())

data class TextCoordinates(val x: String, val y: String, val z: String) {

    companion object {

        val CENTER_LOCAL = TextCoordinates("^", "^", "^")
        val CENTER_GLOBAL = TextCoordinates("~", "~", "~")
    }
}
