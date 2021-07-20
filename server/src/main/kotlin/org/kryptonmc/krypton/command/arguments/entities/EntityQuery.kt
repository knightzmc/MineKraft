package org.kryptonmc.krypton.command.arguments.entities

import com.mojang.brigadier.StringReader
import net.kyori.adventure.key.Key.key
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.api.world.Gamemode
import org.kryptonmc.krypton.entity.KryptonEntity
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.util.toIntRange

/**
 * Documentation [TODO]
 */
class EntityQuery(private val args: List<EntityArgument.EntityArg>, val type: Operation, private val playerName: String = "") {

    fun getEntities(source: KryptonPlayer)= when(type) {
        Operation.RANDOM_PLAYER -> {
            //parse args
            listOf(source.server.players.random())
        }
        Operation.ALL_PLAYERS -> {
            //parse args
            source.server.players
        }
        Operation.EXECUTOR -> listOf(source)
        Operation.ALL_ENTITIES -> {
            // parse args
            (source.server.players + source.world.entities).toList()
        }
        Operation.NEAREST_PLAYER -> {
            // parse args
            var currentNearest = source.server.players[0]
            for (player in source.server.players) {
                if(player.getDistance(source) < currentNearest.getDistance(source)) currentNearest = player
            }
            listOf(currentNearest)
        }
        Operation.PLAYER -> listOf(source.server.player(playerName)!!)
        Operation.UNKNOWN -> listOf()
    }

    private fun applyArguments(source: KryptonPlayer): List<KryptonEntity> {
        var entities = (source.world.entities + source.server.players).toList()
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
                            it.getDistance(source) <= distance
                        }
                    } else if (!arg.value.contains("..")) {
                        entities.filter {
                            it.getDistance(source).toInt() == arg.value.toInt()
                        }
                    } else {
                        val range = arg.value.toIntRange()
                        entities.filter {
                            it.getDistance(source) >= range.first && it.getDistance(source) <= range.last
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

                }
                "tag" -> {
                    TODO()
                }
                "team" -> {
                    TODO()
                }
                "level" -> {
                    TODO()
                }
                "gamemode" -> {
                    entities = entities.filter {
                        it is KryptonPlayer && it.gamemode == Gamemode.fromName(arg.value)
                    }
                }
                "name" -> {
                    entities = entities.filter {
                        it.name == arg.value
                    }
                }
                "x_rotation" -> {
                    TODO()
                }
                "y_rotation" -> {
                    TODO()
                }
                "type" -> {
                    entities = entities.filter {
                        it.type == Registries.ENTITY_TYPE[key(arg.value)]
                    }
                }
                "nbt" -> {
                    TODO()
                }
                "advancements" -> {
                    TODO()
                }
                "predicate" -> {
                    TODO()
                }
                "sort" -> {
                    TODO()
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


