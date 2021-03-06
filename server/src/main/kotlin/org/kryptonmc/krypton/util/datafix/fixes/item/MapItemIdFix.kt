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
package org.kryptonmc.krypton.util.datafix.fixes.item

import com.mojang.datafixers.DSL.fieldFinder
import com.mojang.datafixers.DSL.named
import com.mojang.datafixers.DSL.remainderFinder
import com.mojang.datafixers.DataFix
import com.mojang.datafixers.TypeRewriteRule
import com.mojang.datafixers.schemas.Schema
import org.kryptonmc.krypton.util.datafix.References
import org.kryptonmc.krypton.util.datafix.schema.NamespacedSchema

class MapItemIdFix(outputSchema: Schema, changesType: Boolean) : DataFix(outputSchema, changesType) {

    override fun makeRule(): TypeRewriteRule {
        val itemType = inputSchema.getType(References.ITEM_STACK)
        val idFinder = fieldFinder("id", named(References.ITEM_NAME.typeName(), NamespacedSchema.NAMESPACED_STRING))
        val tagFinder = itemType.findField("tag")
        return fixTypeEverywhereTyped("MapIdFix", itemType) {
            val id = it.getOptional(idFinder)
            if (id.isEmpty || id.get().second != "minecraft:filled_map") return@fixTypeEverywhereTyped it
            val data = it[remainderFinder()]
            val tagTyped = it.getOrCreateTyped(tagFinder)
            var tag = tagTyped[remainderFinder()]
            tag = tag.set("map", tag.createInt(data["Damage"].asInt(0)))
            it.set(tagFinder, tagTyped.set(remainderFinder(), tag))
        }
    }
}
