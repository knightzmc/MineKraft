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
import com.mojang.datafixers.TypeRewriteRule
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic
import org.kryptonmc.krypton.util.datafix.References
import java.util.function.Function

class ForcePOIRebuildFix(outputSchema: Schema, changesType: Boolean) : DataFix(outputSchema, changesType) {

    override fun makeRule(): TypeRewriteRule {
        val type = named(References.POI_CHUNK.typeName(), remainderType())
        check(type == inputSchema.getType(References.POI_CHUNK)) { "POI type is not what was expected! Expected $type, got ${inputSchema.getType(References.POI_CHUNK)}" }
        return fixTypeEverywhere("POI rebuild", type) { Function { pair -> pair.mapSecond { it.cap() } } }
    }

    private fun <T> Dynamic<T>.cap() = update("Sections") { sections -> sections.updateMapValues { pair -> pair.mapSecond { it.remove("Valid") } } }
}
