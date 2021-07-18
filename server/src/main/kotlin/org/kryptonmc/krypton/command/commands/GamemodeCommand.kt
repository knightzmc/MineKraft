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
package org.kryptonmc.krypton.command.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.word
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.world.Gamemode
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.command.InternalCommand
import org.kryptonmc.krypton.command.SuggestionProviders
import org.kryptonmc.krypton.command.arguments.entities.EntityArgument
import org.kryptonmc.krypton.command.arguments.entities.EntityQuery
import org.kryptonmc.krypton.command.arguments.entities.entityArgument
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfo
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfo.PlayerAction.UPDATE_GAMEMODE
import org.kryptonmc.krypton.util.argument

internal class GamemodeCommand(private val server: KryptonServer) : InternalCommand {

    override fun register(dispatcher: CommandDispatcher<Sender>) {
        dispatcher.register(
            literal<Sender>("gamemode")
                .then(argument<Sender, String>("gamemode", word())
                    .suggests(SuggestionProviders.GAMEMODES)
                    .executes {
                        val sender = it.source as? KryptonPlayer ?: return@executes 1
                        val gamemode = Gamemode.fromName(it.argument<String>("gamemode")) ?: return@executes 1
                        updateGameMode(sender, gamemode)
                        1
                    }
                    .then(
                        argument<Sender, EntityQuery>("target", EntityArgument.singlePlayer(server))
                          //  .suggests(SuggestionProviders.ENTITIES(server, EntityArgument.EntityType.PLAYER))
                            .executes {
                                val sender = it.source as? KryptonPlayer ?: return@executes 1
                                val (entities, operation) = it.entityArgument("target").parse(sender)
                                if (operation == EntityQuery.Operation.UNKNOWN) return@executes 1

                                sender.sendMessage(text(entities.map { it.name }
                                    .toString())) //To see if all entities are getting parsed

                                val gamemode = Gamemode.fromName(it.argument<String>("gamemode")) ?: return@executes 1

                                for (entity in entities) {
                                    updateGameMode(entity as KryptonPlayer, gamemode, sender)
                                }
                                1
                        })
                )
        )
    }

    private fun updateGameMode(player: KryptonPlayer, mode: Gamemode, sender: Sender = player) {
        player.gamemode = mode
        server.playerManager.sendToAll(PacketOutPlayerInfo(UPDATE_GAMEMODE, player))
        sender.sendMessage(
            if (player != sender) translatable(
                "commands.gamemode.success.other",
                listOf(text(player.name), translatable("gameMode.${mode.name.lowercase()}"))
            ) else translatable(
                "commands.gamemode.success.self",
                listOf(translatable("gameMode.${mode.name.lowercase()}"))
            )
        )
    }

}
