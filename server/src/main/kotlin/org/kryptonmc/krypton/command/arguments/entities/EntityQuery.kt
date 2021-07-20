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
class EntityQuery(
    private val args: List<EntityArgument.EntityArg>,
    val type: SELECTOR,
    private val playerName: String = ""
) {

    fun getEntities(source: KryptonPlayer) = when (type) {
        SELECTOR.RANDOM_PLAYER -> {
            listOf(source.server.players.random())
        }
        SELECTOR.ALL_PLAYERS -> {
            if (args.isNotEmpty()) {
                val entities = applyArguments((source.server.players + source.world.entities).toList(), source)
                if (entities.isEmpty()) throw PLAYER_NOT_FOUND.create()
                entities
            } else {
                source.server.players
            }
        }
        SELECTOR.EXECUTOR -> listOf(source)
        SELECTOR.ALL_ENTITIES -> {
            if (args.isNotEmpty()) {
                val entities = applyArguments((source.server.players + source.world.entities).toList(), source)
                if (entities.isEmpty()) throw ENTITY_NOT_FOUND.create()
                entities
            } else {
                (source.server.players + source.world.entities).toList()
            }
        }
        SELECTOR.NEAREST_PLAYER -> {
            var currentNearest = source.server.players[0]
            for (player in source.server.players) {
                if (player.distance(source) < currentNearest.distance(source)) currentNearest = player
            }
            listOf(currentNearest)
        }
        SELECTOR.PLAYER -> listOf(source.server.player(playerName) ?: throw PLAYER_NOT_EXISTS.create())
        SELECTOR.UNKNOWN -> throw UNKNOWN_SELECTOR_EXCEPTION.create("")
    }

    fun getEntity(source: KryptonPlayer): KryptonEntity = getEntities(source)[0]

    fun getPlayers(source: KryptonPlayer) =
        getEntities(source).map { if (it !is KryptonPlayer) throw UnsupportedOperationException("You cannot call .getPlayers() if there is an entity in the arguments") else it }

    fun getPlayer(source: KryptonPlayer) =
        getEntities(source).map { if (it !is KryptonPlayer) throw UnsupportedOperationException("You cannot call .getPlayers() if there is an entity in the arguments") else it }[0]


    private fun applyArguments(originalEntities: List<KryptonEntity>, source: KryptonPlayer): List<KryptonEntity> {
        var entities = originalEntities
        for (arg in args) {
            when (arg.name) {
                "x" -> {
                    entities = entities.filter {
                        it.location.blockX == arg.value.toString().toInt()
                    }
                }
                "y" -> {
                    entities = entities.filter {
                        it.location.blockY == arg.value.toString().toInt()
                    }
                }
                "z" -> {
                    entities = entities.filter {
                        it.location.blockY == arg.value.toString().toInt()
                    }
                }
                "distance" -> {
                    entities = if (arg.value.toString().startsWith("..")) {
                        val distance = arg.value.toString().replace("..", "").toInt()
                        entities.filter {
                            it.distance(source) <= distance
                        }
                    } else if (!arg.value.toString().contains("..")) {
                        entities.filter {
                            it.distance(source).toInt() == arg.value.toString().toInt()
                        }
                    } else {
                        val range = arg.value.toString().toIntRange()
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
                    TODO()
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
                        it is KryptonPlayer && it.gamemode == Gamemode.fromName(arg.value.toString())
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
                        it.type == Registries.ENTITY_TYPE[key(arg.value.toString())]
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
                    val limit = arg.value.toString().toInt()
                    if (limit <= 0) throw LIMIT_NULL.create()
                    entities = if (entities.size > limit) entities.subList(0, limit - 1) else entities
                }
                else -> {
                    throw UnsupportedOperationException("Invalid operation")
                }
            }
        }
        return entities
    }

    enum class SELECTOR {

        RANDOM_PLAYER,
        ALL_PLAYERS,
        EXECUTOR,
        ALL_ENTITIES,
        NEAREST_PLAYER,
        PLAYER,
        UNKNOWN;

        companion object {

            /**
             * Get the target selector from it's short name
             * you can find these at the [Minecraft Wiki](https://minecraft.fandom.com/wiki/Target_selectors)
             */
            fun fromChar(selector: Char) = when (selector) {
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


