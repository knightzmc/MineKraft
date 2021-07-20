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
package org.kryptonmc.krypton.locale

import com.google.gson.JsonObject
import me.bardy.gsonkt.fromJson
import net.kyori.adventure.key.Key
import net.kyori.adventure.translation.GlobalTranslator
import net.kyori.adventure.translation.TranslationRegistry
import org.kryptonmc.krypton.GSON
import java.text.MessageFormat
import java.util.Locale

object TranslationBootstrap {

    val REGISTRY = TranslationRegistry.create(Key.key("krypton", "minecraft_translations"))
    private val UNSUPPORTED_FORMAT_REGEX = "%(\\d+\\\$)?[\\d.]*[df]".toRegex()

    fun init() {
        val inputStream = Thread.currentThread().contextClassLoader.getResourceAsStream("en_us.json")
            ?: error("Unable to find built-in Minecraft locale file in JAR!")
        val json = GSON.fromJson<JsonObject>(inputStream.reader())
        json.entrySet().forEach {
            val key = it.key
            val value = it.value.asString.replace(UNSUPPORTED_FORMAT_REGEX, "%\$1s").replaceMinecraftPattern()
            REGISTRY.register(key, Locale.US, MessageFormat(value, Locale.US))
        }
        GlobalTranslator.get().addSource(REGISTRY)
    }
}

private val UNSUPPORTED_MINECRAFT_PATTERN = "(%+)((\\\\d+\\\$)?[\\\\d.]*[df]|s)".toRegex()

private fun String.replaceMinecraftPattern(): String {
    var index = -1
    return UNSUPPORTED_MINECRAFT_PATTERN.replace(this) {
        index++
        "{$index}"
    }
}
