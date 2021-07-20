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
package org.kryptonmc.krypton.util.datafix.fixes.entity

import com.google.gson.JsonParseException
import com.mojang.datafixers.DSL.remainderFinder
import com.mojang.datafixers.Typed
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic
import me.bardy.gsonkt.fromJson
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.kryptonmc.api.adventure.toJsonString
import org.kryptonmc.krypton.util.datafix.References

class StrictJsonSignTextFix(outputSchema: Schema, changesType: Boolean) : NamedEntityFix(outputSchema, changesType, "StrictJsonSignTextFix", References.BLOCK_ENTITY, "Sign") {

    override fun fix(typed: Typed<*>): Typed<*> = typed.update(remainderFinder()) {
        var temp = it.updateLine("Text1")
        temp = temp.updateLine("Text2")
        temp = temp.updateLine("Text3")
        temp.updateLine("Text4")
    }

    private fun Dynamic<*>.updateLine(lineName: String): Dynamic<*> {
        val line = get(lineName).asString("")
        val component = if (line != "null" && line.isNotEmpty()) {
            if (line.startsWith("\"") && line.endsWith("\"") || line.startsWith("{") && line.endsWith("}")) {
                try {
                    GSON.fromJson<Component>(line)
                } catch (exception: JsonParseException) {
                    Component.empty()
                }
            } else {
                Component.text(line)
            }
        } else {
            Component.empty()
        }
        return set(lineName, createString(component.toJsonString()))
    }

    companion object {

        private val GSON = GsonComponentSerializer.gson().serializer().newBuilder().setLenient().create()
    }
}
