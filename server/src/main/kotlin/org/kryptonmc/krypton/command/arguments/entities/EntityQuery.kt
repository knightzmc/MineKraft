package org.kryptonmc.krypton.command.arguments.entities

import com.mojang.brigadier.StringReader
import net.kyori.adventure.key.Key.key
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.krypton.entity.KryptonEntity
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.util.toIntRange

/**
 * Documentation [TODO]
 */
class EntityQuery(val reader: StringReader, val type: Operation) {

    internal var isPlayer = Pair(false, "")

    fun parse(
        player: KryptonPlayer,
    ): EntityResult {
        if (isPlayer.first) return EntityResult(listOf(player.server.player(isPlayer.second)!!), type)
        reader.skipWhitespace()
        val args = mutableListOf<EntityArgument.EntityArg>()
        while (reader.canRead() && reader.peek() != ']') {
            reader.skipWhitespace()
            reader.skip()
            var not = false
            val option = buildString {
                while (reader.canRead() && reader.peek() != '=') {
                    val c = reader.read()
                    append(c)
                    reader.skipWhitespace()
                }
            }
            reader.skipWhitespace()
            val value = buildString {
                if (reader.canRead() && reader.peek() == '=') {
                    reader.skip()
                    reader.skipWhitespace()
                    if (reader.peek() == '!') {
                        not = true
                        reader.skip()
                        reader.skipWhitespace()
                    }
                    while (reader.canRead() && reader.peek() != ',' && reader.peek() != ']') {
                        append(reader.read())
                        reader.skipWhitespace()
                    }
                }
                reader.skipWhitespace()
            }
            if (option in EntityArguments.ARGUMENTS) {
                args += EntityArgument.EntityArg(option, value, not)
            }
        }
        return EntityResult(applyArguments(args, player), type)
    }

    private fun applyArguments(args: List<EntityArgument.EntityArg>, player: KryptonPlayer): List<KryptonEntity> {
        var entities = (player.world.entities + player.server.players).toList()
        for (arg in args) {
            when (arg.name) {
                "x" -> {
                    entities = entities.filter {
                        it.location.blockX == arg.value.toInt()
                    }
                }
                "y" -> {
                    entities = entities.filter {
                        it.location.blockY == arg.value.toInt()
                    }
                }
                "z" -> {
                    entities = entities.filter {
                        it.location.blockY == arg.value.toInt()
                    }
                }
                "distance" -> {
                    entities = if (arg.value.startsWith("..")) {
                        val distance = arg.value.replace("..", "").toInt()
                        entities.filter {
                            it.getDistance(player) <= distance
                        }
                    } else if (!arg.value.contains("..")) {
                        entities.filter {
                            it.getDistance(player).toInt() == arg.value.toInt()
                        }
                    } else {
                        val range = arg.value.toIntRange()
                        entities.filter {
                            it.getDistance(player) >= range.first && it.getDistance(player) <= range.last
                        }
                    }
                }
                "dx" -> {
                    TODO()
                }
                "dy" -> {
                    TODO()
                }
                "dz" -> {
                    TODO()
                }
                "scores" -> {
                    TODO()
                }
                "tag" -> {
                    TODO()
                }
                "team" -> {
                    TODO()
                }
                "level" -> {

                }
                "gamemode" -> {

                }
                "name" -> {
                    entities = entities.filter {
                        it.name == arg.value
                    }
                }
                "x_rotation" -> {

                }
                "y_rotation" -> {

                }
                "type" -> {
                    entities = entities.filter {
                        it.type == Registries.ENTITY_TYPE[key(arg.value)]
                    }
                }
                "nbt" -> {

                }
                "advancements" -> {

                }
                "predicate" -> {

                }
                "sort" -> {

                }
                "limit" -> {
                    val limit = arg.value.toInt()
                    entities = if (entities.size > limit) entities.subList(0, limit - 1) else entities
                }
                else -> {
                    throw UnsupportedOperationException("Invalid operation")
                }
            }
        }
        return entities
    }

    enum class Operation {

        RANDOM_PLAYER,
        ALL_PLAYERS,
        EXECUTOR,
        ALL_ENTITIES,
        NEAREST_PLAYER,
        PLAYER,
        UNKNOWN;

        companion object {

            fun fromChar(operation: Char) = when (operation) {
                'p' -> NEAREST_PLAYER
                'r' -> RANDOM_PLAYER
                'a' -> ALL_PLAYERS
                'e' -> ALL_ENTITIES
                's' -> EXECUTOR
                else -> UNKNOWN
            }
        }

    }

}


