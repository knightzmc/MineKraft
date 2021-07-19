package org.kryptonmc.krypton.command.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import org.kryptonmc.api.command.Sender
import org.kryptonmc.krypton.command.InternalCommand
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.util.argument

class MeCommand : InternalCommand {

    override fun register(dispatcher: CommandDispatcher<Sender>) {
        dispatcher.register(
            literal<Sender>("me")
                .then(argument<Sender, String>("action", string())
                    .executes {
                        val sender = it.source as? KryptonPlayer ?: return@executes 1
                        sender.server.broadcast(
                            translatable(
                                "chat.type.emote",
                                listOf(text(sender.name), text(it.argument<String>("action")))
                            )
                        )
                        1
                    })
        )
    }
}
