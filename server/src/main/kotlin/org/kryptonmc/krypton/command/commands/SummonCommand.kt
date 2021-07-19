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
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.space.Position
import org.kryptonmc.krypton.adventure.toMessage
import org.kryptonmc.krypton.command.InternalCommand
import org.kryptonmc.krypton.command.SuggestionProviders
import org.kryptonmc.krypton.command.arguments.NBTCompoundArgument
import org.kryptonmc.krypton.command.arguments.SummonEntityArgument
import org.kryptonmc.krypton.command.arguments.VectorArgument
import org.kryptonmc.krypton.command.arguments.coordinates.Coordinates
import org.kryptonmc.krypton.command.arguments.entitySummonArgument
import org.kryptonmc.krypton.command.arguments.vectorArgument
import org.kryptonmc.krypton.entity.EntityFactory
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.util.argument
import org.kryptonmc.krypton.util.isInSpawnableBounds

object SummonCommand : InternalCommand {

    private val ERROR_FAILED = SimpleCommandExceptionType(Component.translatable("commands.summon.failed").toMessage())
    private val ERROR_INVALID_POSITION = SimpleCommandExceptionType(Component.translatable("commands.summon.invalidPosition").toMessage())

    override fun register(dispatcher: CommandDispatcher<Sender>) {
        dispatcher.register(literal<Sender>("summon")
            .then(argument<Sender, Key>("entity", SummonEntityArgument())
                .suggests(SuggestionProviders.SUMMONABLE_ENTITIES)
                .executes {
                    val sender = it.source as? KryptonPlayer ?: return@executes 1
                    spawnEntity(sender, it.entitySummonArgument("entity"), sender.location, NBTCompound())
                    1
                }
                .then(argument<Sender, Coordinates>("position", VectorArgument())
                    .executes {
                        val sender = it.source as? KryptonPlayer ?: return@executes 1
                        spawnEntity(
                            sender,
                            it.entitySummonArgument("entity"),
                            it.vectorArgument("position"),
                            NBTCompound()
                        )
                        1
                    }
                    .then(argument<Sender, NBTCompound>("nbt", NBTCompoundArgument())
                        .executes {
                            val sender = it.source as? KryptonPlayer ?: return@executes 1
                            spawnEntity(
                                sender,
                                it.entitySummonArgument("entity"),
                                it.vectorArgument("position"),
                                it.argument("nbt")
                            )
                            1
                        })))
        )
    }

    private fun spawnEntity(player: KryptonPlayer, entityType: Key, position: Position, nbt: NBTCompound) {
        if (!position.isInSpawnableBounds) throw ERROR_INVALID_POSITION.create()
        val tag = nbt.deepClone().setString("id", entityType.asString())
        val world = player.world
        val entity = EntityFactory.create(world, tag)?.apply {
            location = location.copy(position.x, position.y, position.z)
        } ?: throw ERROR_FAILED.create()
        world.spawnEntity(entity)
        player.sendMessage(Component.translatable("commands.summon.success", entity.displayName))
    }
}
