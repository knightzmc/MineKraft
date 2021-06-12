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
package org.kryptonmc.krypton.util.datafix.fix

import com.mojang.datafixers.DSL
import com.mojang.datafixers.DataFix
import com.mojang.datafixers.TypeRewriteRule
import com.mojang.datafixers.schemas.Schema
import org.kryptonmc.krypton.util.datafix.References
import org.kryptonmc.krypton.util.datafix.schema.NamespacedSchema
import java.util.function.Function

class RenameItemFix(outputSchema: Schema, private val name: String, private val fixItem: (String) -> String) : DataFix(outputSchema, false) {

    override fun makeRule(): TypeRewriteRule {
        val type = DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.NAMESPACED_STRING)
        if (inputSchema.getType(References.ITEM_NAME) != type) error("Item name type is not what was expected!")
        return fixTypeEverywhere(name, type) { _ -> Function { pair -> pair.mapSecond { fixItem(it) } } }
    }
}
