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
import com.mojang.datafixers.types.templates.TaggedChoice.TaggedChoiceType
import java.util.function.Function

class AddChoices(outputSchema: Schema, private val name: String, private val type: DSL.TypeReference) : DataFix(outputSchema, true) {

    @Suppress("UNCHECKED_CAST") // Did a lot of investigating, and there isn't really anything else we can do here
    override fun makeRule(): TypeRewriteRule {
        val inputType = inputSchema.findChoiceType(type) as TaggedChoiceType<Any>
        val outputType = outputSchema.findChoiceType(type) as TaggedChoiceType<Any>
        return cap(inputType, outputType)
    }

    private fun cap(type: TaggedChoiceType<Any>, newType: TaggedChoiceType<Any>): TypeRewriteRule {
        if (type.keyType != newType.keyType) error("Could not inject: Key type is not the same!")
        return fixTypeEverywhere(name, type, newType) { _ ->
            Function {
                require(newType.hasType(it.first)) { "Unknown type ${it.first} in ${this.type}!" }
                it
            }
        }
    }
}
