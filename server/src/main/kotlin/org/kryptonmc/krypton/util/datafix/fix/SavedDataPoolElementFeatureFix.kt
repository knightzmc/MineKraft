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

import com.mojang.datafixers.DataFix
import com.mojang.datafixers.TypeRewriteRule
import com.mojang.datafixers.schemas.Schema
import com.mojang.serialization.DataResult
import com.mojang.serialization.Dynamic
import com.mojang.serialization.OptionalDynamic
import org.kryptonmc.krypton.util.datafix.References
import java.util.Optional
import java.util.function.Function
import java.util.stream.Stream

private val INDEX_REGEX = "\\[(\\d+)]".toRegex()
private val PIECE_TYPE = setOf("minecraft:jigsaw", "minecraft:nvi", "minecraft:pcp", "minecraft:bastionremnant", "minecraft:runtime")
private val FEATURES = setOf("minecraft:tree", "minecraft:flower", "minecraft:block_pile", "minecraft:random_patch")

class SavedDataPoolElementFeatureFix(outputSchema: Schema) : DataFix(outputSchema, false) {

    override fun makeRule(): TypeRewriteRule = writeFixAndRead(
        "SavedDataPoolElementFeatureFix",
        inputSchema.getType(References.STRUCTURE_FEATURE),
        outputSchema.getType(References.STRUCTURE_FEATURE),
        Dynamic<*>::fixTag
    )
}

private fun <T> Dynamic<T>.fixTag() = update("Children") { it.updateChildren() }

private fun <T> Dynamic<T>.updateChildren() = asStreamOpt().map(Stream<Dynamic<T>>::updateChildren).map { createList(it) }.result().orElse(this)

private fun <T> Stream<Dynamic<T>>.updateChildren() = map { dynamic ->
    val id = dynamic.get("id").asString("")
    if (id !in PIECE_TYPE) return@map dynamic
    val element = dynamic.get("pool_element")
    if (element.get("element_type").asString("") != "minecraft:feature_pool_element") return@map dynamic
    if (!element.get("feature").get("name").result().isPresent) return@map dynamic
    dynamic.update("pool_element") { it.update("feature", Dynamic<*>::fixFeature) }
}

private fun Dynamic<*>.fixFeature(): Dynamic<*> {
    val replacement = get("type").asString("").replacement(
        paths("name").asString(""),
        paths("config", "state_provider", "type").asString(""),
        paths("config", "state_provider", "state", "Name").asString(""),
        paths("config", "state_provider", "entries", "[0]", "data", "Name").asString(""),
        paths("config", "foliage_placer", "type").asString(""),
        paths("config", "leaves_provider", "state", "Name").asString("")
    )
    return if (replacement.isPresent) createString(replacement.get()) else this
}

private fun <T> Dynamic<T>.paths(vararg paths: String): OptionalDynamic<T> {
    require(paths.isNotEmpty()) { "Missing path!" }
    var dynamic = get(paths[0])
    for (i in 1 until paths.size) {
        val path = paths[i]
        if (!INDEX_REGEX.matches(path)) {
            dynamic = dynamic.get(path)
            continue
        }
        val results = INDEX_REGEX.matchEntire(path)!!
        val digit = results.groups[1]!!.value.toInt()
        val list = dynamic.asList(Function.identity())
        dynamic = if (digit in 0 until list.size) OptionalDynamic(ops, DataResult.success(list[digit])) else OptionalDynamic(ops, DataResult.error("Missing id: $digit!"))
    }
    return dynamic
}

private fun String.replacement(name: String, providerType: String, providerName: String, providerFirstEntryName: String, foliagePlacerType: String, leavesProviderName: String): Optional<String> {
    val string = ifEmpty {
        if (name.isEmpty()) return Optional.empty()
        if (name == "minecraft:normal_tree") "minecraft:tree" else name
    }
    if (string !in FEATURES) return Optional.empty()

    if (string == "minecraft:random_patch") {
        if (providerType == "minecraft:simple_state_provider") {
            if (providerName == "minecraft:sweet_berry_bush") return Optional.of("minecraft:patch_berry_bush")
            if (providerName == "minecraft:patch_cactus") return Optional.of("minecraft:patch_cactus")
        }
        if (providerType == "minecraft:weighted_state_provider" && (providerFirstEntryName == "minecraft:grass" || providerFirstEntryName == "minecraft:fern")) return Optional.of("minecraft:patch_taiga_grass")
    }
    if (string == "minecraft:block_pile") {
        if (providerType != "minecraft:simple_state_provider" && providerType != "minecraft:rotated_block_provider") {
            if (providerType == "minecraft:weighted_state_provider") {
                if (providerFirstEntryName == "minecraft:packed_ice" || providerFirstEntryName == "minecraft:blue_ice") return Optional.of("minecraft:pile_ice")
                if (providerFirstEntryName == "minecraft:jack_o_lantern" || providerFirstEntryName == "minecraft:pumpkin") return Optional.of("minecraft:pile_pumpkin")
            }
        }
        if (providerName == "minecraft:hay_block") return Optional.of("minecraft:pile_hay")
        if (providerName == "minecraft:melon") return Optional.of("minecraft:pile_melon")
        if (providerName == "minecraft:snow") return Optional.of("minecraft:pile_snow")
    }
    if (string == "minecraft:flower") return Optional.of("minecraft:flower_plain")
    if (string == "minecraft:tree") {
        if (foliagePlacerType == "minecraft:acacia_foliage_placer") return Optional.of("minecraft:acacia")
        if (foliagePlacerType == "minecraft:blob_foliage_placer" && leavesProviderName == "minecraft:oak_leaves") return Optional.of("minecraft:oak")
        if (foliagePlacerType == "minecraft:pine_foliage_placer") return Optional.of("minecraft:pine")
        if (foliagePlacerType == "minecraft:spruce_foliage_placer") return Optional.of("minecraft:spruce")
    }
    return Optional.empty()
}
