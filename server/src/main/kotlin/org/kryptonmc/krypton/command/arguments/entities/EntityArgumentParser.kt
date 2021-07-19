package org.kryptonmc.krypton.command.arguments.entities

import com.mojang.brigadier.StringReader

object EntityArgumentParser {

    fun parse(
        reader: StringReader,
        operation: Char,
    ) = when (operation) {
        'p' -> {
            EntityQuery(reader, EntityQuery.Operation.NEAREST_PLAYER)
        }
        'e' -> {
            EntityQuery(reader, EntityQuery.Operation.ALL_ENTITIES)
        }
        'r' -> {
            EntityQuery(reader, EntityQuery.Operation.RANDOM_PLAYER)
        }
        'a' -> {
            EntityQuery(reader, EntityQuery.Operation.ALL_PLAYERS)
        }
        's' -> {
            EntityQuery(reader, EntityQuery.Operation.EXECUTOR)
        }
        else -> EntityQuery(reader, EntityQuery.Operation.UNKNOWN)
    }


}
