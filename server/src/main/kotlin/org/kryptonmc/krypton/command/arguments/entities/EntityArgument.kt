package org.kryptonmc.krypton.command.arguments.entities

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.kyori.adventure.text.Component.text
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.command.suggest
import org.kryptonmc.krypton.util.argument
import java.util.concurrent.CompletableFuture


class EntityArgument private constructor(val type: EntityType, val server: KryptonServer) :
    ArgumentType<EntityQuery> {

    override fun parse(reader: StringReader): EntityQuery {
        val startCursor = reader.cursor
        if (reader.canRead() && reader.peek() == '@') {
            reader.skip()
            return EntityArgumentParser.parse(reader, reader.read())
        } else {
            val input = reader.readString()
            val entity = EntityQuery(
                reader,
                EntityQuery.Operation.PLAYER
            )
            entity.isPlayer = true to input
            if (input.matches(PLAYER_NAME_REGEX)) return entity
        }
        return EntityQuery(reader, EntityQuery.Operation.UNKNOWN)
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        if (context.source !is Player)
            (context.source as Player).sendMessage(text("${builder.remaining} | ${builder.input}"))
        val selector = if (type == EntityType.PLAYER) EntityArguments.SELECTOR_PLAYERS else EntityArguments.SELECTOR_ALL
        if (builder.input == "@") {
            return builder.suggest(selector)
        } else if (builder.input.startsWith("@") && selector.contains(builder.input.replace("@", ""))) {
            return builder.suggest(listOf("["))
        }
        return Suggestions.empty()
    }

    override fun getExamples() = EXAMPLES

    companion object {

        private val EXAMPLES = listOf("Player1", "Player2")
        private val PLAYER_NAME_REGEX = Regex("[a-zA-Z0-9_]{1,16}")

        fun singlePlayer(server: KryptonServer): EntityArgument {
            return EntityArgument(EntityType.PLAYER, server)
        }

        fun singleEntity(server: KryptonServer): EntityArgument {
            return EntityArgument(EntityType.ENTITY, server)
        }
    }

    enum class EntityType {
        ENTITY,
        PLAYER
    }

    data class EntityArg(val name: String, val value: String, val not: Boolean)

}

fun CommandContext<Sender>.entityArgument(name: String) = argument<EntityQuery>(name)

