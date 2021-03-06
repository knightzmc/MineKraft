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
package org.kryptonmc.krypton.util.datafix.fixes.name

import com.mojang.datafixers.DSL
import com.mojang.datafixers.DataFixUtils
import com.mojang.datafixers.TypeRewriteRule
import com.mojang.datafixers.schemas.Schema
import org.kryptonmc.krypton.util.datafix.References
import kotlin.math.min

class RenameBlockWithJigsawFix(outputSchema: Schema, name: String, fixBlock: (String) -> String) : RenameBlockFix(outputSchema, name, fixBlock) {

    override fun makeRule(): TypeRewriteRule {
        val reference = References.BLOCK_ENTITY
        val jigsaw = "minecraft:jigsaw"
        val finder = DSL.namedChoice(jigsaw, inputSchema.getChoiceType(reference, jigsaw))
        val rule = fixTypeEverywhereTyped("$name for jigsaw state", inputSchema.getType(reference), outputSchema.getType(reference)) { typed -> typed.updateTyped(finder, outputSchema.getChoiceType(reference, jigsaw)) { outputTyped -> outputTyped.update(DSL.remainderFinder()) { dynamic -> dynamic.update("final_state") { dynamic2 -> DataFixUtils.orElse(dynamic2.asString().result().map {
            val firstSquareBracket = it.indexOf('[')
            val firstCurlyBracket = it.indexOf('{')
            var length = it.length
            if (firstSquareBracket > 0) length = min(length, firstSquareBracket)
            if (firstCurlyBracket > 0) length = min(length, firstCurlyBracket)
            fixBlock(it.substring(0, length)) + it.substring(length, it.length)
        }.map(dynamic::createString), dynamic2) } } } }
        return TypeRewriteRule.seq(super.makeRule(), rule)
    }
}
