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

open class RenameBlockFix(
    outputSchema: Schema,
    val name: String,
    val fixBlock: (String) -> String
) : DataFix(outputSchema, false) {

    override fun makeRule(): TypeRewriteRule {
        val type = inputSchema.getType(References.BLOCK_NAME)
        val namedType = DSL.named(References.BLOCK_NAME.typeName(), NamespacedSchema.NAMESPACED_STRING)
        if (type != namedType) error("Block type is not what was expected!")

        val blockRewriteRule = fixTypeEverywhere("$name for block", namedType) { _ -> Function { pair -> pair.mapSecond { fixBlock(it) } } }
        val stateRewriteRule = fixTypeEverywhereTyped("$name for block_state", inputSchema.getType(References.BLOCK_STATE)) { typed ->
            typed.update(DSL.remainderFinder()) {
                val name = it.get("Name").asString().result()
                if (name.isPresent) it.set("Name", it.createString(fixBlock(name.get()))) else it
            }
        }
        return TypeRewriteRule.seq(blockRewriteRule, stateRewriteRule)
    }
}
