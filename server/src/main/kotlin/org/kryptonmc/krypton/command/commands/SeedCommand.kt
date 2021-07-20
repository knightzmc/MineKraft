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
