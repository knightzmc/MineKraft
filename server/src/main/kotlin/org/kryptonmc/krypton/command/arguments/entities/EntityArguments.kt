package org.kryptonmc.krypton.command.arguments.entities

object EntityArguments {

    val SELECTOR_ALL = listOf("@p", "@r", "@a", "@e", "@s")
    val SELECTOR_PLAYERS = listOf("@p", "@r", "@a", "@s")
    val ARGUMENTS = listOf(
        "x", "y", "z",
        "distance", "dx", "dy", "dz",
        "scores", "tag", "team", "limit", "sort", "level", "gamemode", "name",
        "x_rotation", "y_rotation", "type", "nbt", "advancements", "predicate"
    )

}
