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
package org.kryptonmc.krypton.item.meta

import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTList
import org.jglrxavpok.hephaistos.nbt.NBTString
import org.jglrxavpok.hephaistos.nbt.NBTTypes
import org.kryptonmc.api.adventure.toJsonString
import org.kryptonmc.api.block.Block
import org.kryptonmc.api.item.meta.MetaKey
import org.kryptonmc.api.item.meta.MetaKeys
import org.kryptonmc.krypton.adventure.toJsonComponent
import org.kryptonmc.krypton.item.ItemFlag
import org.kryptonmc.krypton.world.block.BLOCK_LOADER
import java.awt.Color

object MetaFactory {

    private val MAP = mapOf(
        MetaKeys.DAMAGE to ItemMetaSerializer(
            { it.getInt("Damage") },
            { nbt, damage -> nbt.setInt("Damage", damage) },
            { it.contains("Damage", NBTTypes.TAG_Int) }
        ),
        MetaKeys.UNBREAKABLE to ItemMetaSerializer(
            { it.getBoolean("Unbreakable") },
            { nbt, unbreakable -> nbt.setBoolean("Unbreakable", unbreakable) },
            { it.contains("Unbreakable", NBTTypes.TAG_Byte) }
        ),
        MetaKeys.CAN_DESTROY to BLOCK_LIST_META_SERIALIZER,
        MetaKeys.CUSTOM_MODEL_DATA to ItemMetaSerializer(
            { it.getInt("CustomModelData") },
            { nbt, data -> nbt.setInt("CustomModelData", data) },
            { it.contains("CustomModelData", NBTTypes.TAG_Int) }
        ),
        MetaKeys.CAN_PLACE_ON to BLOCK_LIST_META_SERIALIZER,
        MetaKeys.NAME to ItemMetaSerializer(
            { it.getCompound("display").getString("Name").toJsonComponent() },
            { nbt, name -> nbt.getCompound("display").setString("Name", name.toJsonString()) },
            { it.contains("display", NBTTypes.TAG_Compound) && it.getCompound("display").contains("Name", NBTTypes.TAG_String) }
        ),
        MetaKeys.LORE to ItemMetaSerializer(
            { nbt -> nbt.getCompound("display").getList<NBTString>("Lore").map { it.value.toJsonComponent() } },
            { nbt, lore -> nbt.getCompound("display")["Lore"] = NBTList<NBTString>(NBTTypes.TAG_String).apply { lore.forEach { add(NBTString(it.toJsonString())) } } },
            { it.contains("display", NBTTypes.TAG_Compound) && it.getCompound("display").contains("Lore", NBTTypes.TAG_List) }
        ),
        MetaKeys.COLOR to ItemMetaSerializer(
            { Color(it.getCompound("display").getInt("color")) },
            { nbt, color -> nbt.getCompound("display").setInt("color", color.rgb) },
            { it.contains("display", NBTTypes.TAG_Compound) && it.getCompound("display").contains("color", NBTTypes.TAG_Int) }
        ),
        MetaKeys.HIDE_ATTRIBUTES to hideFlagsSerializer(ItemFlag.ATTRIBUTES),
        MetaKeys.HIDE_CAN_DESTROY to hideFlagsSerializer(ItemFlag.CAN_DESTROY),
        MetaKeys.HIDE_CAN_PLACE_ON to hideFlagsSerializer(ItemFlag.CAN_PLACE),
        MetaKeys.HIDE_DYE to hideFlagsSerializer(ItemFlag.DYE),
        MetaKeys.HIDE_ENCHANTMENTS to hideFlagsSerializer(ItemFlag.ENCHANTMENTS),
        MetaKeys.HIDE_MISCELLANEOUS to hideFlagsSerializer(ItemFlag.MISCELLANEOUS),
        MetaKeys.HIDE_UNBREAKABLE to hideFlagsSerializer(ItemFlag.UNBREAKABLE)
    )

    @Suppress("UNCHECKED_CAST")
    fun <V : Any> get(key: MetaKey<V>, nbt: NBTCompound): V? = (MAP[key] as? ItemMetaSerializer<V>)?.reader?.invoke(nbt)

    @Suppress("UNCHECKED_CAST")
    fun <V : Any> set(key: MetaKey<V>, nbt: NBTCompound, value: V) {
        val serializer = MAP[key] as? ItemMetaSerializer<V> ?: return
        serializer.writer.invoke(nbt, value)
    }

    fun <V : Any> contains(key: MetaKey<V>, nbt: NBTCompound): Boolean = MAP[key]?.predicate?.invoke(nbt) ?: false
}

private fun hideFlagsSerializer(flag: ItemFlag) = ItemMetaSerializer(
    { it.getFlag(flag) },
    { nbt, value -> nbt.setFlag(flag, value) },
    { it.getFlag(flag) }
)

private val BLOCK_LIST_META_SERIALIZER = ItemMetaSerializer(
    { nbt ->
        val blocks = mutableListOf<Block>()
        nbt.getList<NBTString>("CanDestroy").forEach {
            val block = BLOCK_LOADER.fromKey(it.value)
            if (block != null) blocks.add(block)
        }
        blocks
    },
    { nbt, blocks -> nbt["CanDestroy"] = NBTList<NBTString>(NBTTypes.TAG_String).apply { blocks.forEach { add(NBTString(it.key.asString())) } } },
    { it.contains("CanDestroy", NBTTypes.TAG_List) }
)

private fun NBTCompound.getFlag(flag: ItemFlag) = contains("HideFlags", NBTTypes.PRIMITIVE) && getInt("HideFlags") and flag.mask == 0

private fun NBTCompound.setFlag(flag: ItemFlag, value: Boolean) {
    var flags = getInt("HideFlags")
    if (value) {
        setInt("HideFlags", flags or flag.mask)
    } else {
        flags = flags and flag.mask.inv()
        if (flags == 0) removeTag("HideFlags") else setInt("HideFlags", flags)
    }
}

class ItemMetaSerializer<V : Any>(
    val reader: (NBTCompound) -> V,
    val writer: (NBTCompound, V) -> Unit,
    val predicate: (NBTCompound) -> Boolean
)
