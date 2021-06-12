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

class RenameStatsFix(outputSchema: Schema, private val name: String, private val renames: Map<String, String>) : DataFix(outputSchema, false) {

    override fun makeRule(): TypeRewriteRule = TypeRewriteRule.seq(statRule, criteriaRule)

    private val criteriaRule: TypeRewriteRule
        get() {
            val newType = outputSchema.getType(References.OBJECTIVE)
            var type = inputSchema.getType(References.OBJECTIVE)
            val finder = type.findField("CriteriaType")
            val choice = finder.type().findChoiceType("type", -1).orElseThrow { IllegalStateException("Can't find choice type for criteria!") }
            type = choice.types()["minecraft:custom"]
            if (type == null) error("Failed to find custom criterion type variant!")
            val customFinder = DSL.namedChoice("minecraft:custom", type)
            val idFinder = DSL.fieldFinder("id", NamespacedSchema.NAMESPACED_STRING)
            return fixTypeEverywhereTyped(name, type, newType) { typed ->
                typed.updateTyped(finder) { typed2 ->
                    typed2.updateTyped(customFinder) { typed3 ->
                        typed3.update(idFinder) { renames.getOrDefault(it, it) }
                    }
                }
            }
        }

    private val statRule: TypeRewriteRule
        get() {
            val newType = outputSchema.getType(References.STATS)
            val type = inputSchema.getType(References.STATS)
            val statsFinder = type.findField("stats")
            val customFinder = statsFinder.type().findField("minecraft:custom")
            val finder = NamespacedSchema.NAMESPACED_STRING.finder()
            return fixTypeEverywhereTyped(name, type, newType) { typed ->
                typed.updateTyped(statsFinder) { typed2 ->
                    typed2.updateTyped(customFinder) { typed3 ->
                        typed3.update(finder) { renames.getOrDefault(it, it) }
                    }
                }
            }
        }
}
