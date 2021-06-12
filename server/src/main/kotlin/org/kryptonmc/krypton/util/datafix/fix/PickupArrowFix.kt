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
import com.mojang.datafixers.Typed
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.Dynamic
import org.kryptonmc.krypton.util.datafix.References

class PickupArrowFix(outputSchema: Schema) : DataFix(outputSchema, false) {

    override fun makeRule(): TypeRewriteRule = fixTypeEverywhereTyped(
        "PickupArrowFix",
        inputSchema.getType(References.ENTITY)
    ) { it.updateProjectiles() }

    private fun Typed<*>.updateProjectiles(): Typed<*> {
        var typed = updateEntity("minecraft:arrow") { it.updatePickup() }
        typed = typed.updateEntity("minecraft:spectral_arrow") { it.updatePickup() }
        typed = typed.updateEntity("minecraft:trident") { it.updatePickup() }
        return typed
    }

    private fun Dynamic<*>.updatePickup(): Dynamic<*> {
        if (get("pickup").result().isPresent) return this
        val player = get("player").asBoolean(true)
        return set("pickup", createByte(if (player) 1 else 0)).remove("player")
    }

    private fun Typed<*>.updateEntity(name: String, pickupFunction: (Dynamic<*>) -> Dynamic<*>): Typed<*> {
        val inputType = inputSchema.getChoiceType(References.ENTITY, name)
        val outputType = outputSchema.getChoiceType(References.ENTITY, name)
        return updateTyped(DSL.namedChoice(name, inputType), outputType) { it.update(DSL.remainderFinder(), pickupFunction) }
    }
}
