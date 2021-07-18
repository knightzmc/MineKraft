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
package org.kryptonmc.krypton.entity

import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent.ShowEntity
import net.kyori.adventure.text.event.HoverEvent.showEntity
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.jglrxavpok.hephaistos.nbt.*
import org.kryptonmc.api.entity.Entity
import org.kryptonmc.api.entity.EntityType
import org.kryptonmc.api.space.Vector
import org.kryptonmc.api.world.Location
import org.kryptonmc.krypton.ServerStorage
import org.kryptonmc.krypton.command.KryptonSender
import org.kryptonmc.krypton.entity.metadata.MetadataHolder
import org.kryptonmc.krypton.entity.metadata.MetadataKey
import org.kryptonmc.krypton.entity.metadata.MetadataKeys
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.util.nbt.Serializable
import org.kryptonmc.krypton.util.nbt.containsUUID
import org.kryptonmc.krypton.util.nbt.getUUID
import org.kryptonmc.krypton.util.nbt.setUUID
import org.kryptonmc.krypton.util.nextUUID
import org.kryptonmc.krypton.world.KryptonWorld
import java.util.*
import java.util.function.UnaryOperator
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

@Suppress("LeakingThis")
abstract class KryptonEntity(
    override var world: KryptonWorld,
    override val type: EntityType<out Entity>
) : KryptonSender(world.server), Entity, Serializable<NBTCompound> {

    val id = ServerStorage.NEXT_ENTITY_ID.incrementAndGet()
    val data = MetadataHolder(this)

    override var uuid = Random.nextUUID()
    override var location = Location.ZERO
    override var velocity = Vector.ZERO
    override var isOnGround = true
    override var isDead = false
    override var isPersistent = false
    override var ticksLived = 0
    override val name: String
        get() = LegacyComponentSerializer.legacySection().serialize(displayName)

    open val maxAirTicks = 300

    init {
        data += MetadataKeys.FLAGS
        data.add(MetadataKeys.AIR_TICKS, maxAirTicks)
        data += MetadataKeys.DISPLAY_NAME
        data += MetadataKeys.DISPLAY_NAME_VISIBILITY
        data += MetadataKeys.SILENT
        data += MetadataKeys.NO_GRAVITY
        data += MetadataKeys.POSE
        data += MetadataKeys.FROZEN_TICKS
    }

    open fun onDataUpdate(key: MetadataKey<*>) {
        // TODO: Data updating
    }

    override fun load(tag: NBTCompound) {
        val position = tag.getList<NBTDouble>("Pos")
        val motion = tag.getList<NBTDouble>("Motion")
        val rotation = tag.getList<NBTFloat>("Rotation")
        val motionX = motion.getOrNull(0)?.value?.takeIf { abs(it) <= 10.0 } ?: 0.0
        val motionY = motion.getOrNull(1)?.value?.takeIf { abs(it) <= 10.0 } ?: 0.0
        val motionZ = motion.getOrNull(2)?.value?.takeIf { abs(it) <= 10.0 } ?: 0.0
        velocity = Vector(motionX, motionY, motionZ)
        location = Location(
            position.getOrNull(0)?.value ?: 0.0,
            position.getOrNull(1)?.value ?: 0.0,
            position.getOrNull(2)?.value ?: 0.0,
            rotation.getOrNull(0)?.value ?: 0F,
            rotation.getOrNull(0)?.value ?: 0F
        )
        if (tag.containsKey("Air")) airTicks = tag.getShort("Air").toInt()
        isOnGround = tag.getBoolean("OnGround")
        if (tag.containsUUID("UUID")) uuid = tag.getUUID("UUID")

        if (!location.x.isFinite() || !location.y.isFinite() || !location.z.isFinite()) error("Entity has invalid coordinates! Coordinates must be finite!")
        if (!location.yaw.isFinite() || !location.pitch.isFinite()) error("Entity has invalid rotation!")
        if (tag.contains("CustomName", NBTTypes.TAG_String)) displayName = GsonComponentSerializer.gson().deserialize(tag.getString("CustomName"))
        isDisplayNameVisible = tag.getBoolean("CustomNameVisible")
        isSilent = tag.getBoolean("Silent")
        hasGravity = !tag.getBoolean("NoGravity")
        isGlowing = tag.getBoolean("Glowing")
    }

    override fun save() = NBTCompound()
        .setShort("Air", airTicks.toShort())
        .apply {
            if (isDisplayNameVisible) {
                setBoolean("CustomNameVisible", true)
                setString("CustomName", GsonComponentSerializer.gson().serialize(displayName))
            }
            if (isSilent) setBoolean("Silent", true)
            if (!hasGravity) setBoolean("NoGravity", true)
            if (isGlowing) setBoolean("Glowing", true)
            if (frozenTicks > 0) setInt("TicksFrozen", frozenTicks)
            if (this@KryptonEntity !is KryptonPlayer) setString("id", type.key.asString())
        }
        .set("Motion", NBTList<NBTDouble>(NBTTypes.TAG_Double).apply {
            add(NBTDouble(velocity.x))
            add(NBTDouble(velocity.y))
            add(NBTDouble(velocity.z))
        })
        .setBoolean("OnGround", isOnGround)
        .set("Pos", NBTList<NBTDouble>(NBTTypes.TAG_Double).apply {
            add(NBTDouble(location.x))
            add(NBTDouble(location.y))
            add(NBTDouble(location.z))
        })
        .set("Rotation", NBTList<NBTFloat>(NBTTypes.TAG_Float).apply {
            add(NBTFloat(location.yaw))
            add(NBTFloat(location.pitch))
        })
        .setUUID("UUID", uuid)

    private fun getSharedFlag(flag: Int) = data[MetadataKeys.FLAGS].toInt() and (1 shl flag) != 0

    private fun setSharedFlag(flag: Int, state: Boolean) {
        val flags = data[MetadataKeys.FLAGS].toInt()
        data[MetadataKeys.FLAGS] = (if (state) flags or (1 shl flag) else flags and (1 shl flag).inv()).toByte()
    }

    override fun remove() = Unit // TODO: Make this do something

    override fun identity() = Identity.identity(uuid)

    override fun asHoverEvent(op: UnaryOperator<ShowEntity>) =
        showEntity(ShowEntity.of(type.key, uuid, displayName.takeIf { it !== Component.empty() }))

    open fun getDistance(entity: KryptonEntity): Double {
        val e1 = location
        val e2 = entity.location

        val x = (e2.x - e1.x) * (e2.x - e1.x)
        val y = (e2.y - e1.y) * (e2.y - e1.y)
        val z = (e2.z - e1.z) * (e2.z - e1.z)

        return sqrt(x + y + z)
    }

    override var isOnFire: Boolean
        get() = getSharedFlag(0)
        set(value) = setSharedFlag(0, value)

    override var isCrouching: Boolean
        get() = getSharedFlag(1)
        set(value) = setSharedFlag(1, value)

    override var isSprinting: Boolean
        get() = getSharedFlag(3)
        set(value) = setSharedFlag(3, value)

    override var isSwimming: Boolean
        get() = getSharedFlag(4)
        set(value) = setSharedFlag(4, value)

    override var isInvisible: Boolean
        get() = getSharedFlag(5)
        set(value) = setSharedFlag(5, value)

    override var isGlowing: Boolean
        get() = getSharedFlag(6)
        set(value) = setSharedFlag(6, value)

    override var isFlying: Boolean
        get() = getSharedFlag(7)
        set(value) = setSharedFlag(7, value)

    override var airTicks: Int
        get() = data[MetadataKeys.AIR_TICKS]
        set(value) = data.set(MetadataKeys.AIR_TICKS, value)

    override var displayName: Component
        get() = data[MetadataKeys.DISPLAY_NAME].orElse(type.name)
        set(value) = data.set(MetadataKeys.DISPLAY_NAME, Optional.ofNullable(value.takeIf { it != Component.empty() }))

    override var isDisplayNameVisible: Boolean
        get() = data[MetadataKeys.DISPLAY_NAME_VISIBILITY]
        set(value) = data.set(MetadataKeys.DISPLAY_NAME_VISIBILITY, value)

    override var isSilent: Boolean
        get() = data[MetadataKeys.SILENT]
        set(value) = data.set(MetadataKeys.SILENT, value)

    override var hasGravity: Boolean
        get() = !data[MetadataKeys.NO_GRAVITY]
        set(value) = data.set(MetadataKeys.NO_GRAVITY, !value)

    var pose: Pose
        get() = data[MetadataKeys.POSE]
        set(value) = data.set(MetadataKeys.POSE, value)

    var frozenTicks: Int
        get() = data[MetadataKeys.FROZEN_TICKS]
        set(value) = data.set(MetadataKeys.FROZEN_TICKS, value)
}
