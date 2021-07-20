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
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.world.Gamemode
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.command.InternalCommand
import org.kryptonmc.krypton.command.arguments.entities.EntityArgument
import org.kryptonmc.krypton.command.arguments.entities.EntityQuery
import org.kryptonmc.krypton.command.arguments.entities.entityArgument
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfo
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfo.PlayerAction.UPDATE_GAMEMODE
import org.kryptonmc.krypton.util.argument

internal class GamemodeCommand(private val server: KryptonServer) : InternalCommand {

    override fun register(dispatcher: CommandDispatcher<Sender>) {
        val command = literal<Sender>("gamemode")

        for (gamemode in Gamemode.values()) {
            command
                .then(literal<Sender>(gamemode.name.lowercase())
                    .executes {
                        gameModeArgument(it, gamemode)
                    }
                    .then(argument<Sender, EntityQuery>("targets", EntityArgument.player())
                        .executes {
                            targetArgument(it, gamemode)
                        })
                )
        }

        command
            .then(argument<Sender, String>("gamemode", string())
                .executes {
                    val gamemode = Gamemode.fromShortName(it.argument("gamemode"))
                        ?: Gamemode.fromId(it.argument<String>("gamemode").toIntOrNull() ?: return@executes 1)
                        ?: return@executes 1
                    gameModeArgument(it, gamemode)
                }
                .then(argument<Sender, EntityQuery>("targets", EntityArgument.player())
                    .executes {
                        val gamemode = Gamemode.fromShortName(it.argument("gamemode")) ?: Gamemode.fromId(
                            it.argument<String>("gamemode").toIntOrNull() ?: return@executes 1
                        ) ?: return@executes 1
                        targetArgument(it, gamemode)
                    })
            )

        dispatcher.register(command)
    }

    private fun gameModeArgument(context: CommandContext<Sender>, gamemode: Gamemode): Int {
        val sender = context.source as? KryptonPlayer ?: return 1
        updateGameMode(listOf(sender), gamemode, sender)
        return 1
    }

    @Suppress("UNCHECKED_CAST")
    private fun targetArgument(context: CommandContext<Sender>, gamemode: Gamemode): Int {
        val sender = context.source as? KryptonPlayer ?: return 1
        val entities = context.entityArgument("targets").getEntities(sender)
        sender.sendMessage(text(entities.map { it.name }
            .toString())) //To see if all entities are getting parsed

        updateGameMode(entities as List<KryptonPlayer>, gamemode, sender)
        return 1
    }

    private fun updateGameMode(entities: List<KryptonPlayer>, mode: Gamemode, sender: KryptonPlayer) {
        for (entity in entities) {
            entity.gamemode = mode
            server.playerManager.sendToAll(PacketOutPlayerInfo(UPDATE_GAMEMODE, entity))
            sender.sendMessage(
                if (sender == sender) translatable(
                    "commands.gamemode.success.other",
                    listOf(text(entities[0].name), translatable("gameMode.${mode.name.lowercase()}"))
                ) else translatable(
                    "commands.gamemode.success.self",
                    listOf(text(entity.name), translatable("gameMode.${mode.name.lowercase()}"))
                )
            )
        }

    }

}
