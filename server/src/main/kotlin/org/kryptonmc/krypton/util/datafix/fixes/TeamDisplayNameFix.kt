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
package org.kryptonmc.krypton.util.datafix.fixes

import com.mojang.datafixers.DSL.named
import com.mojang.datafixers.DSL.remainderType
import com.mojang.datafixers.DataFix
import com.mojang.datafixers.DataFixUtils
import com.mojang.datafixers.TypeRewriteRule
import com.mojang.datafixers.schemas.Schema
import net.kyori.adventure.text.Component
import org.kryptonmc.api.adventure.toJsonString
import org.kryptonmc.krypton.util.datafix.References
import java.util.function.Function

class TeamDisplayNameFix(outputSchema: Schema, changesType: Boolean) : DataFix(outputSchema, changesType) {

    override fun makeRule(): TypeRewriteRule {
        val type = named(References.TEAM.typeName(), remainderType())
        check(inputSchema.getType(References.TEAM) == type) { "Team type is not what was expected!" }
        return fixTypeEverywhere("TeamDisplayNameFix", type) {
            Function { pair ->
                pair.mapSecond { second ->
                    second.update("DisplayName") { name ->
                        DataFixUtils.orElse(name.asString().map { Component.text(it).toJsonString() }.map(second::createString).result(), name)
                    }
                }
            }
        }
    }
}
