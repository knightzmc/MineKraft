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
                if(player.distance(source) < currentNearest.distance(source)) currentNearest = player
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
                            it.distance(source) <= distance
                        }
                    } else if (!arg.value.contains("..")) {
                        entities.filter {
                            it.distance(source).toInt() == arg.value.toInt()
                        }
                    } else {
                        val range = arg.value.toIntRange()
                        entities.filter {
                            it.distance(source) >= range.first && it.distance(source) <= range.last
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


