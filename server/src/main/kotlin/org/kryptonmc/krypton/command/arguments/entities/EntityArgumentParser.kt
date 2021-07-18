package org.kryptonmc.krypton.command.arguments.entities

import com.mojang.brigadier.StringReader

object EntityArgumentParser {

    fun parse(
        reader: StringReader,
        operation: Char,
    ): EntityQuery {
        when (operation) {
            'p' -> {
                return EntityQuery(reader, EntityQuery.Operation.NEAREST_PLAYER)
            }
            'e' -> {
                return EntityQuery(reader, EntityQuery.Operation.ALL_ENTITIES)
            }
            'r' -> {
                return EntityQuery(reader, EntityQuery.Operation.RANDOM_PLAYER)
            }
            'a' -> {
                return EntityQuery(reader, EntityQuery.Operation.ALL_PLAYERS)
            }
            's' -> {
                return EntityQuery(reader, EntityQuery.Operation.EXECUTOR)
            }
            else -> return EntityQuery(reader, EntityQuery.Operation.UNKNOWN)
        }
    }


}
