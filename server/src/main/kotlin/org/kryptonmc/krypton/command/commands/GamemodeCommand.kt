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
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.world.Gamemode
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.command.InternalCommand
import org.kryptonmc.krypton.command.SuggestionProviders
import org.kryptonmc.krypton.command.arguments.EntityArgument
import org.kryptonmc.krypton.command.arguments.entityArgument
import org.kryptonmc.krypton.entity.KryptonEntity
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfo
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfo.PlayerAction.UPDATE_GAMEMODE
import org.kryptonmc.krypton.util.argument

internal class GamemodeCommand(private val server: KryptonServer) : InternalCommand {

    override fun register(dispatcher: CommandDispatcher<Sender>) {
        StringArgumentType.string()
        dispatcher.register(
            literal<Sender>("gamemode")
                .then(argument<Sender, String>("gamemode", string())
                    .suggests(SuggestionProviders.GAMEMODES)
                    .executes {
                        val sender = it.source as? KryptonPlayer ?: return@executes 1
                        val gamemode = Gamemode.fromName(it.argument<String>("gamemode")) ?: return@executes 1
                        updateGameMode(sender, gamemode)
                        1
                    }
                    .then(argument<Sender, KryptonEntity>("player", EntityArgument.singlePlayer(server))
                        .suggests(SuggestionProviders.PLAYERS(server))
                        .executes {
                            val sender = it.source as? KryptonPlayer ?: return@executes 1
                            val player = it.entityArgument("player") as KryptonPlayer ?: return@executes 1
                            val gamemode = Gamemode.fromName(it.argument<String>("gamemode")) ?: return@executes 1
                            updateGameMode(player, gamemode, sender)
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
