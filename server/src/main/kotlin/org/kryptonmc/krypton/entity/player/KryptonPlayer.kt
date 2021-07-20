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
package org.kryptonmc.krypton.entity.player

import kotlinx.coroutines.launch
import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.sound.SoundStop
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent.ShowEntity
import net.kyori.adventure.text.event.HoverEvent.showEntity
import net.kyori.adventure.title.Title
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBTInt
import org.jglrxavpok.hephaistos.nbt.NBTTypes
import org.kryptonmc.api.block.Block
import org.kryptonmc.api.effect.particle.ColorParticleData
import org.kryptonmc.api.effect.particle.DirectionalParticleData
import org.kryptonmc.api.effect.particle.NoteParticleData
import org.kryptonmc.api.effect.particle.ParticleEffect
import org.kryptonmc.api.entity.EntityTypes
import org.kryptonmc.api.entity.MainHand
import org.kryptonmc.api.entity.player.Abilities
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.registry.Registries
import org.kryptonmc.api.registry.RegistryKey
import org.kryptonmc.api.space.Direction
import org.kryptonmc.api.space.Position
import org.kryptonmc.api.space.Vector
import org.kryptonmc.api.world.Gamemode
import org.kryptonmc.api.world.Location
import org.kryptonmc.api.world.dimension.DimensionType
import org.kryptonmc.api.world.scoreboard.Scoreboard
import org.kryptonmc.krypton.IOScope
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.ServerInfo
import org.kryptonmc.krypton.auth.GameProfile
import org.kryptonmc.krypton.entity.KryptonEntity
import org.kryptonmc.krypton.entity.KryptonLivingEntity
import org.kryptonmc.krypton.entity.attribute.Attributes
import org.kryptonmc.krypton.entity.metadata.MetadataKeys
import org.kryptonmc.krypton.inventory.KryptonPlayerInventory
import org.kryptonmc.krypton.item.handler
import org.kryptonmc.krypton.packet.out.play.GameState
import org.kryptonmc.krypton.packet.out.play.PacketOutAbilities
import org.kryptonmc.krypton.packet.out.play.PacketOutActionBar
import org.kryptonmc.krypton.packet.out.play.PacketOutCamera
import org.kryptonmc.krypton.packet.out.play.PacketOutChangeGameState
import org.kryptonmc.krypton.packet.out.play.PacketOutChat
import org.kryptonmc.krypton.packet.out.play.PacketOutChunkData
import org.kryptonmc.krypton.packet.out.play.PacketOutClearTitles
import org.kryptonmc.krypton.packet.out.play.PacketOutMetadata
import org.kryptonmc.krypton.packet.out.play.PacketOutEntityPosition
import org.kryptonmc.krypton.packet.out.play.PacketOutEntityTeleport
import org.kryptonmc.krypton.packet.out.play.PacketOutNamedSoundEffect
import org.kryptonmc.krypton.packet.out.play.PacketOutOpenBook
import org.kryptonmc.krypton.packet.out.play.PacketOutParticle
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerListHeaderFooter
import org.kryptonmc.krypton.packet.out.play.PacketOutPluginMessage
import org.kryptonmc.krypton.packet.out.play.PacketOutSetSlot
import org.kryptonmc.krypton.packet.out.play.PacketOutSoundEffect
import org.kryptonmc.krypton.packet.out.play.PacketOutStopSound
import org.kryptonmc.krypton.packet.out.play.PacketOutSubTitle
import org.kryptonmc.krypton.packet.out.play.PacketOutTitle
import org.kryptonmc.krypton.packet.out.play.PacketOutTitleTimes
import org.kryptonmc.krypton.packet.out.play.PacketOutUnloadChunk
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateLight
import org.kryptonmc.krypton.packet.out.play.PacketOutUpdateViewPosition
import org.kryptonmc.krypton.network.Session
import org.kryptonmc.krypton.util.calculatePositionChange
import org.kryptonmc.krypton.util.chunkInSpiral
import org.kryptonmc.krypton.util.logger
import org.kryptonmc.krypton.util.nbt.NBTOps
import org.kryptonmc.krypton.util.toArea
import org.kryptonmc.krypton.item.toItemStack
import org.kryptonmc.krypton.packet.out.play.PacketOutPlayerInfo
import org.kryptonmc.krypton.world.KryptonWorld
import org.kryptonmc.krypton.world.bossbar.BossBarManager
import org.kryptonmc.krypton.world.chunk.ChunkPosition
import org.spongepowered.math.vector.Vector3i
import java.net.InetSocketAddress
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.function.UnaryOperator
import kotlin.math.abs
import kotlin.math.min

class KryptonPlayer(
    val session: Session,
    val profile: GameProfile,
    world: KryptonWorld,
    override val address: InetSocketAddress = InetSocketAddress("127.0.0.1", 1)
) : KryptonLivingEntity(world, EntityTypes.PLAYER), Player {

    override val name = profile.name
    override var uuid = profile.uuid

    override var abilities = Abilities()
    override val inventory = KryptonPlayerInventory(this)
    override var scoreboard: Scoreboard? = null
    override var locale: Locale? = null

    override var viewDistance = 10
    override var time = 0L

    private var camera: KryptonEntity = this
        set(value) {
            val old = field
            field = value
            if (old != field) {
                session.sendPacket(PacketOutCamera(field.id))
                teleport(field.location)
            }
        }

    var oldGamemode: Gamemode? = null
    override var gamemode = Gamemode.SURVIVAL
        set(value) {
            if (value == field) return
            oldGamemode = field
            field = value
            updateAbilities()
            onAbilitiesUpdate()
            session.sendPacket(PacketOutChangeGameState(GameState.CHANGE_GAMEMODE, value.ordinal.toFloat()))
            if (value != Gamemode.SPECTATOR) camera = this
        }
    override val direction: Direction
        get() = Direction.fromPitch(location.pitch.toDouble())

    override val dimensionType: DimensionType
        get() = world.dimensionType
    val dimension: RegistryKey<KryptonWorld>
        get() = world.dimension

    val viewableEntities: MutableSet<KryptonEntity> = ConcurrentHashMap.newKeySet()

    private var respawnPosition: Vector3i? = null
    private var respawnForced = false
    private var respawnAngle = 0F
    private var respawnDimension = KryptonWorld.OVERWORLD

    private var previousCentralX = 0
    private var previousCentralZ = 0
    private var hasLoadedChunks = false
    private val visibleChunks = mutableSetOf<ChunkPosition>()

    init {
        data += MetadataKeys.PLAYER.ADDITIONAL_HEARTS
        data += MetadataKeys.PLAYER.SCORE
        data += MetadataKeys.PLAYER.SKIN_FLAGS
        data += MetadataKeys.PLAYER.MAIN_HAND
        data += MetadataKeys.PLAYER.LEFT_SHOULDER
        data += MetadataKeys.PLAYER.RIGHT_SHOULDER
    }

    override fun load(tag: NBTCompound) {
        super.load(tag)
        uuid = profile.uuid
        oldGamemode = Gamemode.fromId((tag["previousPlayerGameType"] as? NBTInt)?.value ?: -1)
        gamemode = Gamemode.fromId(tag.getInt("playerGameType")) ?: Gamemode.SURVIVAL
        isOnGround = tag.getBoolean("OnGround")
        inventory.load(tag.getList("Inventory"))
        inventory.heldSlot = tag.getInt("SelectedItemSlot")
        score = tag.getInt("Score")
        if (tag.contains("abilities", NBTTypes.TAG_Compound)) abilities.load(tag.getCompound("abilities"))
        attributes[Attributes.MOVEMENT_SPEED]?.baseValue = abilities.walkSpeed.toDouble()

        // NBT data for entities sitting on the player's shoulders, e.g. parrots
        if (tag.contains("ShoulderEntityLeft", NBTTypes.TAG_Compound)) leftShoulder = tag.getCompound("ShoulderEntityLeft")
        if (tag.contains("ShoulderEntityRight", NBTTypes.TAG_Compound)) rightShoulder = tag.getCompound("ShoulderEntityRight")

        // Respawn data
        if (tag.contains("SpawnX", 99) && tag.contains("SpawnY", 99) && tag.contains("SpawnZ", 99)) {
            respawnPosition = Vector3i(tag.getInt("SpawnX"), tag.getInt("SpawnY"), tag.getInt("SpawnZ"))
            respawnForced = tag.getBoolean("SpawnForced")
            respawnAngle = tag.getFloat("SpawnAngle")
            if (tag.containsKey("SpawnDimension")) {
                val dimension = KryptonWorld.REGISTRY_KEY_CODEC.parse(NBTOps, tag["SpawnDimension"]!!)
                respawnDimension = dimension.resultOrPartial(LOGGER::error).orElse(KryptonWorld.OVERWORLD)
            }
        }
    }

    override fun save() = super.save()
        .setInt("DataVersion", ServerInfo.WORLD_VERSION)
        .set("Inventory", inventory.save())
        .setInt("SelectedItemSlot", inventory.heldSlot)
        .setInt("Score", score)
        .set("abilities", abilities.save())
        .apply {
            if (leftShoulder.size != 0) set("ShoulderEntityLeft", leftShoulder)
            if (rightShoulder.size != 0) set("ShoulderEntityRight", rightShoulder)
        }
        .setInt("playerGameType", gamemode.ordinal)
        .apply { oldGamemode?.let { setInt("previousPlayerGameType", it.ordinal) } }
        .setString("Dimension", dimension.location.asString())
        .apply {
            val respawnPosition = respawnPosition
            if (respawnPosition != null) {
                setInt("SpawnX", respawnPosition.x())
                setInt("SpawnY", respawnPosition.y())
                setInt("SpawnZ", respawnPosition.z())
                setBoolean("SpawnForced", respawnForced)
                setFloat("SpawnAngle", respawnAngle)
                KryptonWorld.REGISTRY_KEY_CODEC.encodeStart(NBTOps, respawnDimension).resultOrPartial(LOGGER::error).ifPresent { set("SpawnDimension", it) }
            }
        }

    override fun addViewer(player: KryptonPlayer): Boolean {
        if (player === this) return false
        player.session.sendPacket(PacketOutPlayerInfo(PacketOutPlayerInfo.PlayerAction.ADD_PLAYER, this))
        return super.addViewer(player)
    }

    override fun removeViewer(player: KryptonPlayer): Boolean {
        if (player === this || !super.removeViewer(player)) return false
        player.session.sendPacket(PacketOutPlayerInfo(PacketOutPlayerInfo.PlayerAction.REMOVE_PLAYER, this))
        return true
    }

    override fun spawnParticles(particleEffect: ParticleEffect, location: Location) {
        val packet = PacketOutParticle(particleEffect, location)
        when (particleEffect.data) {
            // Send multiple packets based on the quantity
            is DirectionalParticleData, is ColorParticleData, is NoteParticleData -> repeat(particleEffect.quantity) { session.sendPacket(packet) }
            // Send particles to player at location
            else -> session.sendPacket(packet)
        }
    }

    override fun teleport(position: Position) {
        val oldLocation = location
        location = Location(position.x, position.y, position.z)

        if (abs(location.x - oldLocation.x) > 8 || abs(location.y - oldLocation.y) > 8 || abs(location.z - oldLocation.z) > 8) {
            session.sendPacket(PacketOutEntityTeleport(id, location, isOnGround))
        } else {
            session.sendPacket(PacketOutEntityPosition(
                id,
                calculatePositionChange(location.x, oldLocation.x),
                calculatePositionChange(location.y, oldLocation.y),
                calculatePositionChange(location.z, oldLocation.z),
                isOnGround
            ))
        }
        updateChunks()
    }

    override fun teleport(player: Player) = teleport(player.location)

    override fun sendPluginMessage(channel: Key, message: ByteArray) {
        if (channel !in PREREGISTERED_CHANNELS) require(channel in server.channels) { "Channel must be registered with the server to have data sent over it!" }
        if (channel in DEBUG_CHANNELS) {
            SERVER_LOGGER.warn("A plugin attempted to send a plugin message on a debug channel. These channels will only function correctly with a modified client.")
        }
        session.sendPacket(PacketOutPluginMessage(channel, message))
    }

    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        session.sendPacket(PacketOutChat(message, type, source.uuid()))
    }

    override fun sendActionBar(message: Component) {
        session.sendPacket(PacketOutActionBar(message))
    }

    override fun sendPlayerListHeaderAndFooter(header: Component, footer: Component) {
        session.sendPacket(PacketOutPlayerListHeaderFooter(header, footer))
    }

    override fun showTitle(title: Title) {
        session.sendPacket(PacketOutTitle(title.title()))
        session.sendPacket(PacketOutSubTitle(title.subtitle()))
        title.times()?.let { session.sendPacket(PacketOutTitleTimes(it)) }
    }

    override fun clearTitle() {
        session.sendPacket(PacketOutClearTitles(false))
    }

    override fun resetTitle() {
        session.sendPacket(PacketOutClearTitles(true))
    }

    override fun showBossBar(bar: BossBar) = BossBarManager.addBar(bar, this)

    override fun hideBossBar(bar: BossBar) = BossBarManager.removeBar(bar, this)

    override fun playSound(sound: Sound) = playSound(sound, location.x, location.y, location.z)

    override fun playSound(sound: Sound, x: Double, y: Double, z: Double) {
        val type = Registries.SOUND_EVENT[sound.name()]
        session.sendPacket(if (type != null) PacketOutSoundEffect(sound, type, Vector(x, y, z)) else PacketOutNamedSoundEffect(sound, Vector(x, y, z)))
    }

    override fun stopSound(stop: SoundStop) {
        session.sendPacket(PacketOutStopSound(stop))
    }

    override fun openBook(book: Book) {
        val item = book.toItemStack(locale ?: Locale.ENGLISH)
        val slot = inventory.heldSlot
        session.sendPacket(PacketOutSetSlot(inventory.id, inventory.incrementStateId(), slot, item))
        session.sendPacket(PacketOutOpenBook(hand))
        session.sendPacket(PacketOutSetSlot(inventory.id, inventory.incrementStateId(), slot, inventory.mainHand))
    }

    override fun identity() = Identity.identity(uuid)

    override fun asHoverEvent(op: UnaryOperator<ShowEntity>) =
        showEntity(ShowEntity.of(key("minecraft", "player"), uuid, displayName))

    fun updateAbilities() {
        abilities = when (gamemode) {
            Gamemode.CREATIVE -> Abilities(
                isInvulnerable = true,
                canFly = true,
                canInstantlyBuild = true
            )
            Gamemode.SPECTATOR -> Abilities(
                isInvulnerable = true,
                canFly = true,
                isFlying = true,
                canInstantlyBuild = false
            )
            else -> Abilities()
        }
        abilities.canBuild = gamemode.canBuild
    }

    fun updateChunks() {
        var previousChunks: MutableSet<ChunkPosition>? = null
        val newChunks = mutableListOf<ChunkPosition>()

        val centralX = location.blockX shr 4
        val centralZ = location.blockZ shr 4
        val radius = min(server.config.world.viewDistance, 1 + viewDistance)

        if (!hasLoadedChunks) {
            hasLoadedChunks = true
            repeat(server.config.world.viewDistance.toArea()) { newChunks += chunkInSpiral(it, centralX, centralZ) }
        } else if (abs(centralX - previousCentralX) > radius || abs(centralZ - previousCentralZ) > radius) {
            visibleChunks.clear()
            repeat(server.config.world.viewDistance.toArea()) { newChunks += chunkInSpiral(it, centralX, centralZ) }
        } else if (previousCentralX != centralX || previousCentralZ != centralZ) {
            previousChunks = HashSet(visibleChunks)
            repeat(server.config.world.viewDistance.toArea()) {
                val position = chunkInSpiral(it, centralX, centralZ)
                if (position in visibleChunks) previousChunks.remove(position) else newChunks += position
            }
        } else {
            return
        }

        previousCentralX = centralX
        previousCentralZ = centralZ

        IOScope.launch {
            val loadedChunks = world.chunkManager.load(newChunks)
            visibleChunks += newChunks

            session.sendPacket(PacketOutUpdateViewPosition(ChunkPosition(centralX, centralZ)))
            loadedChunks.forEach {
                session.sendPacket(PacketOutUpdateLight(it))
                session.sendPacket(PacketOutChunkData(it))
            }

            previousChunks?.forEach {
                session.sendPacket(PacketOutUnloadChunk(it))
                visibleChunks -= it
            }
            previousChunks?.clear()
        }
    }

    override fun hasCorrectTool(block: Block): Boolean = !block.requiresCorrectTool || inventory.mainHand.type.handler.isCorrectTool(block)

    override fun getDestroySpeed(block: Block): Float {
        var speed = inventory.mainHand.getDestroySpeed(block)
        if (!isOnGround) speed /= 5F
        return speed
    }

    private fun onAbilitiesUpdate() {
        session.sendPacket(PacketOutAbilities(abilities))
        updateInvisibility()
        server.playerManager.sendToAll(PacketOutMetadata(id, data.dirty), world)
    }

    private fun updateInvisibility() {
        removeEffectParticles()
        isInvisible = gamemode == Gamemode.SPECTATOR
    }

    override var absorption: Float
        get() = data[MetadataKeys.PLAYER.ADDITIONAL_HEARTS]
        set(value) = data.set(MetadataKeys.PLAYER.ADDITIONAL_HEARTS, value)

    var score: Int
        get() = data[MetadataKeys.PLAYER.SCORE]
        set(value) = data.set(MetadataKeys.PLAYER.SCORE, value)

    var skinSettings: Byte
        get() = data[MetadataKeys.PLAYER.SKIN_FLAGS]
        set(value) = data.set(MetadataKeys.PLAYER.SKIN_FLAGS, value)

    override var mainHand: MainHand
        get() = if (data[MetadataKeys.PLAYER.MAIN_HAND] == 0.toByte()) MainHand.LEFT else MainHand.RIGHT
        set(value) = data.set(MetadataKeys.PLAYER.MAIN_HAND, if (value == MainHand.LEFT) 0 else 1)

    var leftShoulder: NBTCompound
        get() = data[MetadataKeys.PLAYER.LEFT_SHOULDER]
        set(value) = data.set(MetadataKeys.PLAYER.LEFT_SHOULDER, value)

    var rightShoulder: NBTCompound
        get() = data[MetadataKeys.PLAYER.RIGHT_SHOULDER]
        set(value) = data.set(MetadataKeys.PLAYER.RIGHT_SHOULDER, value)

    companion object {

        private val DEBUG_CHANNELS = setOf(
            key("debug/paths"),
            key("debug/neighbors_update"),
            key("debug/caves"),
            key("debug/structures"),
            key("debug/worldgen_attempt"),
            key("debug/poi_ticket_count"),
            key("debug/poi_added"),
            key("debug/poi_removed"),
            key("debug/village_sections"),
            key("debug/goal_selector"),
            key("debug/brain"),
            key("debug/bee"),
            key("debug/hive"),
            key("debug/game_test_add_marker"),
            key("debug/game_test_clear"),
            key("debug/raids")
        )
        private val PREREGISTERED_CHANNELS = DEBUG_CHANNELS + key("brand")
        private val LOGGER = logger<KryptonPlayer>()
        private val SERVER_LOGGER = logger<KryptonServer>()

        fun createAttributes() = KryptonLivingEntity.createAttributes()
            .add(Attributes.ATTACK_DAMAGE, 1.0)
            .add(Attributes.MOVEMENT_SPEED, 0.1)
            .add(Attributes.ATTACK_SPEED)
            .add(Attributes.LUCK)
    }
}
