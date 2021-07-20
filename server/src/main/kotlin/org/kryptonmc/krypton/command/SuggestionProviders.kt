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
package org.kryptonmc.krypton.command

import com.mojang.brigadier.suggestion.SuggestionProvider
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.text.Component.translatable
import org.kryptonmc.api.adventure.toMessage
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.entity.EntityType
import org.kryptonmc.api.registry.Registries

// TODO: Use this later
object SuggestionProviders {

    private val PROVIDERS_BY_NAME = mutableMapOf<Key, SuggestionProvider<Sender>>()
    private val DEFAULT_NAME = key("ask_server")

    val ASK_SERVER = register(DEFAULT_NAME) { _, _ -> null }
    val SUMMONABLE_ENTITIES = register(key("summonable_entities")) { _, builder ->
        Registries.ENTITY_TYPE.values.filter { it.isSummonable }.suggestKey(builder, EntityType<*>::key) {
            val key = Registries.ENTITY_TYPE[it]
            translatable("entity.${key.namespace()}.${key.value().replace("/", ".")}").toMessage()
        }
    }

    fun register(key: Key, provider: SuggestionProvider<Sender>): SuggestionProvider<Sender> {
        require(key !in PROVIDERS_BY_NAME) { "A command suggestion provider is already registered with the given key $key!" }
        PROVIDERS_BY_NAME[key] = provider
        return Wrapper(key, provider)
    }

    fun provider(key: Key) = PROVIDERS_BY_NAME.getOrDefault(key, ASK_SERVER)

    fun name(provider: SuggestionProvider<Sender>) = if (provider is Wrapper) provider.name else DEFAULT_NAME

    private class Wrapper(val name: Key, private val delegate: SuggestionProvider<Sender>) : SuggestionProvider<Sender> by delegate
}
