package org.kryptonmc.krypton.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.kyori.adventure.text.Component.text
import org.kryptonmc.api.command.Sender
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.adventure.toMessage
import org.kryptonmc.krypton.entity.KryptonEntity
import org.kryptonmc.krypton.util.argument


class EntityArgument private constructor(val type: EntityType, val server: KryptonServer) :
    ArgumentType<KryptonEntity> {

    override fun parse(reader: StringReader): KryptonEntity {
        return when (type) {
            EntityType.PLAYER -> {
                server.player(reader.readString())!!
            }
            EntityType.ENTITY -> {
                TODO()
            }
        }
    }

    override fun getExamples() = EXAMPLES

    companion object {

        private val EXAMPLES = listOf("Player1", "Player2")

        private val ERROR_EXPECTED_VALUE = SimpleCommandExceptionType(text("Player not found").toMessage())
        private val ERROR_INVALID_PLAYER = SimpleCommandExceptionType(text("Invalid player name").toMessage())

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

}

fun CommandContext<Sender>.entityArgument(name: String) = argument<KryptonEntity>(name)

