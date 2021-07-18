package org.kryptonmc.krypton.command.arguments.entities

import com.mojang.brigadier.StringReader
import org.kryptonmc.api.world.Gamemode
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.entity.KryptonEntity
import org.kryptonmc.krypton.entity.player.KryptonPlayer

object EntityArgumentParser {

    fun parse(
        reader: StringReader,
        type: EntityArgument.EntityType,
        operation: Char,
        server: KryptonServer
    ): EntityQuery {
        val entities = mutableListOf<KryptonEntity>()

        when (operation) {
            'p' -> {
                return EntityQuery(entities.toList(), EntityQuery.Operation.NEAREST_PLAYER)
            }
            'e' -> {
                //if(type != EntityArgument.EntityType.PLAYER) entities.addAll(world.entities)
                return EntityQuery(entities.toList(), EntityQuery.Operation.ALL_ENTITIES)
            }
            'r' -> {
                entities.add(server.players.random())
                return EntityQuery(entities.toList(), EntityQuery.Operation.RANDOM_PLAYER)
            }
            'a' -> {
                if (reader.canRead() && reader.peek() == '[') {
                    parseArguments(reader, server, type, entities)
                } else {
                    entities.addAll(server.players)
                }
                return EntityQuery(entities.toList(), EntityQuery.Operation.ALL_PLAYERS)
            }
            's' -> {
                return EntityQuery(entities.toList(), EntityQuery.Operation.EXECUTOR)
            }
            else -> return EntityQuery(entities.toList(), EntityQuery.Operation.UNKNOWN)
        }
    }

    private fun parseArguments(
        reader: StringReader,
        server: KryptonServer,
        type: EntityArgument.EntityType,
        allEntities: MutableList<KryptonEntity>
    ) {
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
        var entities = when (type) {
            EntityArgument.EntityType.ENTITY -> (server.worldManager.default.entities + server.players).toList()
            EntityArgument.EntityType.PLAYER -> server.players.toList()
        }
        for (arg in args) {
            when (arg.name) {
                "x" -> {
                    entities = entities.filter {
                        it.location.x == arg.value.toDouble()
                    }
                }
                "y" -> {
                    entities = entities.filter {
                        it.location.y == arg.value.toDouble()
                    }
                }
                "z" -> {
                    entities = entities.filter {
                        it.location.z == arg.value.toDouble()
                    }
                }
                "distance" -> {
                    TODO()
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
                "sort" -> {
                    TODO()
                }
                "level" -> {
                    TODO()
                }
                "gamemode" -> {
                    entities = entities.filter {
                        if (it is KryptonPlayer) {
                            it.gamemode == Gamemode.fromName(arg.value)
                        } else {
                            false
                        }
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
                        it.type.key.asString() == arg.value
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
                "limit" -> {
                    entities = entities.subList(0, arg.value.toInt())
                }
            }
        }
        allEntities.addAll(entities)
    }

}
