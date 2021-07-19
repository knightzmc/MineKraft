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
        val entity = EntityQuery(
            reader,
            EntityQuery.Operation.PLAYER
        )
        entity.isPlayer = true to input
        if (input.matches(PLAYER_NAME_REGEX)) {
            entity
        } else {
            EntityQuery(reader, EntityQuery.Operation.UNKNOWN)
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
        fun singlePlayer(single: Boolean = false) = EntityArgument(EntityType.PLAYER, single)

        /**
         * @param single Whether only one entity can be specified or not
         * @return An argument which can accept all entities
         */
        fun singleEntity(single: Boolean = false) = EntityArgument(EntityType.ENTITY, single)
    }

    enum class EntityType {
        ENTITY,
        PLAYER
    }

    data class EntityArg(val name: String, val value: String, val not: Boolean)

}

fun CommandContext<Sender>.entityArgument(name: String) = argument<EntityQuery>(name)

