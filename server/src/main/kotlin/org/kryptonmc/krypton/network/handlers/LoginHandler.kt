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
package org.kryptonmc.krypton.network.handlers

import com.velocitypowered.natives.util.Natives
import io.netty.buffer.Unpooled
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import net.kyori.adventure.text.format.NamedTextColor
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.api.event.login.LoginEvent
import org.kryptonmc.krypton.IOScope
import org.kryptonmc.krypton.auth.GameProfile
import org.kryptonmc.krypton.auth.exceptions.AuthenticationException
import org.kryptonmc.krypton.auth.requests.SessionService
import org.kryptonmc.krypton.config.category.ForwardingMode
import org.kryptonmc.krypton.entity.player.KryptonPlayer
import org.kryptonmc.krypton.locale.Messages
import org.kryptonmc.krypton.packet.Packet
import org.kryptonmc.krypton.packet.`in`.handshake.BungeeCordHandshakeData
import org.kryptonmc.krypton.packet.`in`.login.PacketInEncryptionResponse
import org.kryptonmc.krypton.packet.`in`.login.PacketInLoginStart
import org.kryptonmc.krypton.packet.`in`.login.PacketInPluginResponse
import org.kryptonmc.krypton.packet.out.login.PacketOutEncryptionRequest
import org.kryptonmc.krypton.packet.out.login.PacketOutLoginSuccess
import org.kryptonmc.krypton.packet.out.login.PacketOutPluginRequest
import org.kryptonmc.krypton.packet.out.login.PacketOutSetCompression
import org.kryptonmc.krypton.network.Session
import org.kryptonmc.krypton.network.PacketState
import org.kryptonmc.krypton.network.netty.PacketCompressor
import org.kryptonmc.krypton.network.netty.PacketDecoder
import org.kryptonmc.krypton.network.netty.PacketDecompressor
import org.kryptonmc.krypton.network.netty.PacketDecrypter
import org.kryptonmc.krypton.network.netty.PacketEncoder
import org.kryptonmc.krypton.network.netty.PacketEncrypter
import org.kryptonmc.krypton.network.netty.SizeDecoder
import org.kryptonmc.krypton.network.netty.SizeEncoder
import org.kryptonmc.krypton.util.encryption.Encryption
import org.kryptonmc.krypton.util.logger
import org.kryptonmc.krypton.util.readVelocityData
import org.kryptonmc.krypton.util.verifyIntegrity
import java.net.InetSocketAddress
import java.util.UUID
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

/**
 * Handles all inbound packets in the [Login][org.kryptonmc.krypton.packet.state.PacketState.LOGIN] state.
 *
 * There are two inbound packets in this state that we handle:
 * - [Login Start][org.kryptonmc.krypton.packet.`in`.login.PacketInLoginStart] -
 *   sent to initiate the login sequence
 * - [Encryption Response][org.kryptonmc.krypton.packet.`in`.login.PacketInEncryptionResponse] -
 *   sent to confirm the client wants to enable encryption
 */
class LoginHandler(
    override val server: KryptonServer,
    override val session: Session,
    private val bungeecordData: BungeeCordHandshakeData?
) : PacketHandler {

    private val playerManager = server.playerManager

    private val velocityMessageId = Random.nextInt(Short.MAX_VALUE.toInt())
    private var name = ""

    private val verifyToken by lazy {
        val bytes = ByteArray(4)
        server.random.nextBytes(bytes)
        bytes
    }

    override fun handle(packet: Packet) = when (packet) {
        is PacketInLoginStart -> handleLoginStart(packet)
        is PacketInEncryptionResponse -> handleEncryptionResponse(packet)
        is PacketInPluginResponse -> handlePluginResponse(packet)
        else -> Unit
    }

    private fun handleLoginStart(packet: PacketInLoginStart) {
        val rawAddress = session.channel.remoteAddress() as InetSocketAddress
        val address = if (bungeecordData != null) InetSocketAddress(bungeecordData.forwardedIp, rawAddress.port) else rawAddress

        name = packet.name
        if (!server.isOnline) {
            if (server.config.proxy.mode == ForwardingMode.MODERN) {
                session.sendPacket(PacketOutPluginRequest(velocityMessageId, VELOCITY_CHANNEL_ID, ByteArray(0)))
                return
            }

            // Note: Per the protocol, offline players use UUID v3, rather than UUID v4.
            val uuid = bungeecordData?.uuid ?: UUID.nameUUIDFromBytes("OfflinePlayer:${packet.name}".encodeToByteArray())
            val profile = GameProfile(uuid, packet.name, bungeecordData?.properties ?: emptyList())

            val player = KryptonPlayer(session, profile, server.worldManager.default, address)
            if (!callLoginEvent(profile)) return
            finishLogin(player)
            return
        }

        session.sendPacket(PacketOutEncryptionRequest(Encryption.publicKey, verifyToken))
    }

    private fun handleEncryptionResponse(packet: PacketInEncryptionResponse) {
        if (!verifyToken(verifyToken, packet.verifyToken)) return
        val sharedSecret = Encryption.decrypt(packet.secret)
        val secretKey = SecretKeySpec(sharedSecret, "AES")
        enableEncryption(secretKey)

        IOScope.launch {
            val profile = try {
                val value = SessionService.authenticateUser(name, sharedSecret, server.config.server.ip)
                if (!callLoginEvent(value)) return@launch
                value
            } catch (exception: AuthenticationException) {
                session.disconnect(translatable("multiplayer.disconnect.unverified_username"))
                return@launch
            }
            enableCompression()

            val rawAddress = session.channel.remoteAddress() as InetSocketAddress
            val address = if (bungeecordData != null) InetSocketAddress(bungeecordData.forwardedIp, rawAddress.port) else rawAddress
            val player = KryptonPlayer(session, profile, server.worldManager.default, address)
            finishLogin(player)
        }
    }

    private fun handlePluginResponse(packet: PacketInPluginResponse) {
        if (!packet.isSuccessful) // not successful, we don't care for now
        if (packet.messageId != velocityMessageId || server.config.proxy.mode != ForwardingMode.MODERN) return // not Velocity, ignore (for now)
        if (packet.data.isEmpty()) error("Velocity sent no data in its login plugin response!")
        val secret = server.config.proxy.secret.encodeToByteArray()

        val buffer = Unpooled.copiedBuffer(packet.data)
        val isSuccess = buffer.verifyIntegrity(secret)
        if (!isSuccess) {
            session.disconnect(INVALID_VELOCITY_RESPONSE)
            return
        }

        val data = buffer.readVelocityData()
        val address = session.channel.remoteAddress() as InetSocketAddress

        LOGGER.debug("Detected Velocity login for ${data.uuid}")
        val profile = GameProfile(data.uuid, data.username, data.properties)
        val player = KryptonPlayer(session, profile, server.worldManager.default, InetSocketAddress(data.remoteAddress, address.port))
        finishLogin(player)
    }

    private fun finishLogin(player: KryptonPlayer) {
        session.sendPacket(PacketOutLoginSuccess(player.uuid, player.name))
        session.handler = PlayHandler(server, session, player)
        session.currentState = PacketState.PLAY
        playerManager.add(player, session)
    }

    private fun verifyToken(expected: ByteArray, actual: ByteArray): Boolean {
        val decryptedActual = Encryption.decrypt(actual)
        require(decryptedActual.contentEquals(expected)) {
            Messages.NETWORK.LOGIN.FAIL_VERIFY_ERROR.error(LOGGER, name, expected.contentToString(), decryptedActual.contentToString())
            session.disconnect(translatable("disconnect.loginFailedInfo", listOf(Messages.NETWORK.LOGIN.FAIL_VERIFY())))
            return false
        }
        return true
    }

    private fun enableEncryption(key: SecretKey) {
        val cipher = Natives.cipher.get()
        val encrypter = PacketEncrypter(cipher.forEncryption(key))
        val decrypter = PacketDecrypter(cipher.forDecryption(key))
        session.channel.pipeline().addBefore(SizeDecoder.NETTY_NAME, PacketDecrypter.NETTY_NAME, decrypter)
        session.channel.pipeline().addBefore(SizeEncoder.NETTY_NAME, PacketEncrypter.NETTY_NAME, encrypter)
    }

    private fun enableCompression() {
        val threshold = server.config.server.compressionThreshold
        if (threshold == -1) {
            try {
                session.channel.pipeline().remove(PacketCompressor.NETTY_NAME)
                session.channel.pipeline().remove(PacketDecompressor.NETTY_NAME)
                return
            } catch (exception: NoSuchElementException) {
                return
            }
        }

        var encoder = session.channel.pipeline()[PacketCompressor.NETTY_NAME] as? PacketCompressor
        var decoder = session.channel.pipeline()[PacketDecompressor.NETTY_NAME] as? PacketDecompressor
        if (encoder != null && decoder != null) {
            encoder.threshold = threshold
            decoder.threshold = threshold
            return
        }

        session.sendPacket(PacketOutSetCompression(threshold))
        val compressor = Natives.compress.get().create(4)
        encoder = PacketCompressor(compressor, threshold)
        decoder = PacketDecompressor(compressor, threshold)

        session.channel.pipeline().addBefore(PacketDecoder.NETTY_NAME, PacketDecompressor.NETTY_NAME, decoder)
        session.channel.pipeline().addBefore(PacketEncoder.NETTY_NAME, PacketCompressor.NETTY_NAME, encoder)
    }

    private fun callLoginEvent(profile: GameProfile): Boolean {
        val event = LoginEvent(profile.name, profile.uuid, session.channel.remoteAddress() as InetSocketAddress)
        val result = server.eventManager.fireSync(event).result
        if (!result.isAllowed) {
            val reason = if (result.reason !== Component.empty()) result.reason else translatable("multiplayer.disconnect.kicked")
            session.disconnect(reason)
            return false
        }
        return true
    }

    companion object {

        private const val VELOCITY_CHANNEL_ID = "velocity:player_info"
        private val INVALID_VELOCITY_RESPONSE = text("Invalid proxy response!", NamedTextColor.RED)
        private val LOGGER = logger<LoginHandler>()
    }
}
