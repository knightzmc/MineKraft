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
package org.kryptonmc.krypton.command.arguments

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.kryptonmc.api.adventure.toMessage
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.krypton.adventure.toMessage
import org.kryptonmc.krypton.util.argument
import org.kryptonmc.krypton.util.readKey

class SummonEntityArgument : ArgumentType<Key> {

    override fun parse(reader: StringReader) = reader.readKey().ensureSummonable()

    override fun getExamples() = EXAMPLES

    companion object {

        private val EXAMPLES = setOf("minecraft:pig", "cow")

        fun id() = SummonEntityArgument()
    }
}

fun CommandContext<Sender>.summonableEntity(argumentName: String) = getArgument(argumentName, Key::class.java).ensureSummonable()

private val ERROR_UNKNOWN_ENTITY = DynamicCommandExceptionType { Component.translatable("entity.notFound", listOf(Component.text(it.toString()))).toMessage() }

private fun Key.ensureSummonable() = Registries.ENTITY_TYPE[this].takeIf { it.isSummonable }?.key ?: throw ERROR_UNKNOWN_ENTITY.create(this)

fun CommandContext<Sender>.entitySummonArgument(name: String) = argument<Key>(name).ensureSummonable()
