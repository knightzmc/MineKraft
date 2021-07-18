package org.kryptonmc.krypton.command.arguments.entities

import org.kryptonmc.krypton.entity.KryptonEntity
import org.kryptonmc.krypton.entity.player.KryptonPlayer

/**
 * Documentation [TODO]
 */
data class EntityQuery(val entities: List<KryptonEntity>, val operation: Operation) {

    fun fillPlayer(sender: KryptonPlayer): EntityQuery {
        val entities = entities.toMutableList()
        if (operation == Operation.NEAREST_PLAYER) {
            var nearest = sender.server.players[0]
            for (player in sender.server.players) {
                if (player.getDistance(sender) < nearest.getDistance(sender)) nearest = player
            }
            entities.add(nearest)
        } else if (operation == Operation.EXECUTOR) entities.add(sender)
        return EntityQuery(entities, operation)
    }

    //   fun parse(sender: KryptonPlayer) = EntityArgumentParser.parse(sender, )

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


