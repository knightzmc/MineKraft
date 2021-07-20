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
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import org.kryptonmc.api.command.Sender
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.command.InternalCommand
import org.kryptonmc.krypton.entity.player.KryptonPlayer

class ListCommand : InternalCommand {

    override fun register(dispatcher: CommandDispatcher<Sender>) {
        dispatcher.register(
            literal<Sender>("list")
                .executes {
                    val sender = it.source as? KryptonPlayer ?: return@executes 1
                    sendNames(sender, sender.server)
                    1
                }
                .then(literal<Sender>("uuids")
                    .executes {
                        val sender = it.source as? KryptonPlayer ?: return@executes 1
                        sendNamesWithUUID(sender, sender.server)
                        1
                    })
        )
    }

    private fun sendNames(sender: KryptonPlayer, server: KryptonServer) {
        val names = server.players.map(KryptonPlayer::name)
        sender.sendMessage(
            translatable(
                "commands.list.players",
                listOf(text(names.size), text(server.status.maxPlayers), text(names.joinToString(separator = "\n")))
            )
        )
    }

    private fun sendNamesWithUUID(sender: KryptonPlayer, server: KryptonServer) {
        val names = server.players.map { it.name + " (${it.uuid})" }
        sender.sendMessage(
            translatable(
                "commands.list.players",
                listOf(text(names.size), text(server.status.maxPlayers), text(names.joinToString(separator = "\n")))
            )
        )
    }
}
