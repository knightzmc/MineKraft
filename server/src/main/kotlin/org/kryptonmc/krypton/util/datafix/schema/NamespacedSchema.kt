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
package org.kryptonmc.krypton.util.datafix.schema

import com.mojang.datafixers.DSL
import com.mojang.datafixers.schemas.Schema
import com.mojang.datafixers.types.Type
import com.mojang.datafixers.types.templates.Const
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.codecs.PrimitiveCodec
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

open class NamespacedSchema(versionKey: Int, parent: Schema) : Schema(versionKey, parent) {

    override fun getChoiceType(type: DSL.TypeReference, choiceName: String): Type<*> = super.getChoiceType(type, choiceName.ensureNamespaced())

    companion object {

        val NAMESPACED_STRING_CODEC = object : PrimitiveCodec<String> {

            override fun <T> read(ops: DynamicOps<T>, input: T) = ops.getStringValue(input).map(String::ensureNamespaced)
            override fun <T> write(ops: DynamicOps<T>, value: String) = ops.createString(value)
            override fun toString() = "NamespacedString"
        }
        val NAMESPACED_STRING = Const.PrimitiveType(NAMESPACED_STRING_CODEC)
    }
}

fun String.ensureNamespaced() = try {
    val key = Key.key(this)
    key.asString()
} catch (exception: InvalidKeyException) {
    this
}
