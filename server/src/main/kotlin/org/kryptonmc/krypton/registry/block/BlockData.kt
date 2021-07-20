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
package org.kryptonmc.krypton.registry.block

import com.google.gson.JsonObject
import net.kyori.adventure.key.Key
import org.kryptonmc.api.util.toKey

class BlockData(
    val key: Key,
    private val main: JsonObject,
    private val override: JsonObject
) {

    val id = int("id")
    val stateId = int("stateId")
    val hardness = double("hardness")
    val resistance = double("explosionResistance")
    val friction = double("friction")
    val speedFactor = double("speedFactor")
    val jumpFactor = double("jumpFactor")
    val air = boolean("air")
    val solid = boolean("solid")
    val liquid = boolean("liquid")
    val blockEntity = boolean("blockEntity")
    val lightEmission = int("lightEmission")
    val occludes = boolean("occludes")
    val blocksMotion = boolean("blocksMotion")
    val flammable = boolean("flammable")
    val gravity = boolean("gravity")
    val translationKey = string("translationKey")
    val replaceable = boolean("replaceable")
    val dynamicShape = boolean("dynamicShape")
    val useShapeForOcclusion = boolean("useShapeForLightOcclusion")
    val propagatesLightDown = boolean("propagatesSkylightDown")
    val lightBlock = int("lightBlock")
    val conditionallyFullyOpaque = boolean("conditionallyFullyOpaque")
    val solidRender = boolean("solidRender")
    val opacity = int("opacity")
    val largeCollisionShape = boolean("largeCollisionShape")
    val canRespawnIn = boolean("canRespawnIn")
    val toolRequired = boolean("toolRequired")
    val itemKey = (override["correspondingItem"] ?: main["correspondingItem"])?.asString?.toKey()

    private fun string(name: String) = element(name).asString
    private fun double(name: String) = element(name).asDouble
    private fun int(name: String) = element(name).asInt
    private fun boolean(name: String) = element(name).asBoolean
    private fun element(name: String) = override[name] ?: main[name]!!
}
