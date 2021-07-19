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
