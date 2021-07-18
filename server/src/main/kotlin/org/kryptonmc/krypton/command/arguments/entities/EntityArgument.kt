package org.kryptonmc.krypton.command.arguments.entities

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import org.kryptonmc.api.command.Sender
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.util.argument


class EntityArgument private constructor(val type: EntityType, val server: KryptonServer) :
    ArgumentType<EntityQuery> {

    override fun parse(reader: StringReader): EntityQuery {
        val startCursor = reader.cursor
        if (reader.canRead() && reader.peek() == '@') {
            reader.skip()
            return EntityArgumentParser.parse(reader, type, reader.read(), server)
        } else {
            val input = reader.readString()
            if (input.matches(PLAYER_NAME_REGEX)) return EntityQuery(
                listOf(server.player(input)!!),
                EntityQuery.Operation.PLAYER
            )
        }
        return EntityQuery(listOf(), EntityQuery.Operation.UNKNOWN)
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

