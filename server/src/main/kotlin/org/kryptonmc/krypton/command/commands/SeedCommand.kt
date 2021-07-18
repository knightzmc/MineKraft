package org.kryptonmc.krypton.command.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.format.TextColor
import org.kryptonmc.api.command.Sender
import org.kryptonmc.krypton.command.InternalCommand
import org.kryptonmc.krypton.entity.player.KryptonPlayer

class SeedCommand : InternalCommand {

    override fun register(dispatcher: CommandDispatcher<Sender>) {
        dispatcher.register(literal<Sender>("seed")
            .executes {
                val sender = it.source as? KryptonPlayer ?: return@executes 1
                val text = text("[").append(text(sender.world.seed, TextColor.color(5635925))).append(text("]"))
                sender.sendMessage(translatable("commands.seed.success", listOf(text)))
                1
            })
    }
}
