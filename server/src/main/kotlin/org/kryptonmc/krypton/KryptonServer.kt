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
package org.kryptonmc.krypton

import com.google.gson.Gson
import kotlinx.coroutines.launch
import me.bardy.gsonkt.newBuilder
import me.bardy.gsonkt.registerTypeAdapter
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.util.Services
import org.apache.logging.log4j.LogManager
import org.kryptonmc.api.Server
import org.kryptonmc.api.event.server.ServerStartEvent
import org.kryptonmc.api.event.server.ServerStopEvent
import org.kryptonmc.api.event.ticking.TickEndEvent
import org.kryptonmc.api.event.ticking.TickStartEvent
import org.kryptonmc.api.registry.RegistryManager
import org.kryptonmc.api.status.StatusInfo
import org.kryptonmc.api.world.Difficulty
import org.kryptonmc.api.world.Gamemode
import org.kryptonmc.krypton.auth.MojangUUIDSerializer
import org.kryptonmc.krypton.command.KryptonCommandManager
import org.kryptonmc.krypton.command.commands.DebugCommand.Companion.DEBUG_FOLDER
import org.kryptonmc.krypton.config.KryptonConfig
import org.kryptonmc.krypton.console.KryptonConsoleSender
import org.kryptonmc.krypton.console.KryptonConsole
import org.kryptonmc.krypton.item.KryptonItemManager
import org.kryptonmc.krypton.locale.Messages
import org.kryptonmc.krypton.locale.MetadataResponse
import org.kryptonmc.krypton.locale.TranslationRepository
import org.kryptonmc.krypton.network.PacketLoader
import org.kryptonmc.krypton.packet.out.play.PacketOutTimeUpdate
import org.kryptonmc.krypton.plugin.KryptonEventManager
import org.kryptonmc.krypton.plugin.KryptonPluginManager
import org.kryptonmc.krypton.registry.json.RegistryBlock
import org.kryptonmc.krypton.registry.json.RegistryBlockState
import org.kryptonmc.krypton.scheduling.KryptonScheduler
import org.kryptonmc.krypton.serializers.DifficultySerializer
import org.kryptonmc.krypton.serializers.GamemodeSerializer
import org.kryptonmc.krypton.server.PlayerManager
import org.kryptonmc.krypton.service.KryptonServicesManager
import org.kryptonmc.krypton.util.Bootstrap
import org.kryptonmc.krypton.util.concurrent.DefaultUncaughtExceptionHandler
import org.kryptonmc.krypton.util.createDirectories
import org.kryptonmc.krypton.util.createDirectory
import org.kryptonmc.krypton.util.logger
import org.kryptonmc.krypton.util.profiling.ServerProfiler
import org.kryptonmc.krypton.util.profiling.DeadProfiler
import org.kryptonmc.krypton.util.profiling.Profiler
import org.kryptonmc.krypton.util.profiling.SingleTickProfiler
import org.kryptonmc.krypton.util.profiling.results.ProfileResults
import org.kryptonmc.krypton.util.profiling.decorate
import org.kryptonmc.krypton.util.reports.CrashReport
import org.kryptonmc.krypton.util.reports.ReportedException
import org.kryptonmc.krypton.world.KryptonWorldManager
import org.kryptonmc.krypton.world.block.KryptonBlockManager
import org.kryptonmc.krypton.world.scoreboard.KryptonScoreboard
import org.spongepowered.configurate.hocon.HoconConfigurationLoader
import org.spongepowered.configurate.kotlin.extensions.get
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.file.Path
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Properties
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.math.max
import kotlin.system.measureTimeMillis

class KryptonServer : Server {

    internal val config = loadConfig()

    override val info = KryptonServerInfo
    override val status = KryptonStatusInfo(config.status.maxPlayers, config.status.motd)

    override val isOnline = config.server.onlineMode
    override val isHardcore = config.world.hardcore
    override val difficulty = config.world.difficulty
    override val gamemode = config.world.gamemode

    override val address = InetSocketAddress(config.server.ip, config.server.port)

    val playerManager = PlayerManager(this)

    override val players = playerManager.players
    override val channels: MutableSet<Key> = ConcurrentHashMap.newKeySet()

    override fun player(uuid: UUID) = playerManager.playersByUUID[uuid]
    override fun player(name: String) = playerManager.playersByName[name]

    override val console = KryptonConsoleSender

    override var scoreboard: KryptonScoreboard? = null
        private set

    private val nettyProcess = NettyProcess(this)
    internal val random: SecureRandom = SecureRandom()

    override val worldManager = KryptonWorldManager(this, config.world.name)
    override val commandManager = KryptonCommandManager(this)
    override val pluginManager = KryptonPluginManager(this)
    override val eventManager = KryptonEventManager(pluginManager)
    override val servicesManager = KryptonServicesManager
    override val registryManager = Services.service(RegistryManager::class.java).get()
    override val blockManager = KryptonBlockManager
    override val itemManager = KryptonItemManager
    override val scheduler = KryptonScheduler(pluginManager)

    @Volatile
    internal var isRunning = true
        private set

    @Volatile
    internal var lastTickTime = 0L
        private set

    private var lastOverloadWarning = 0L
    private var tickCount = 0
    private var oversleepFactor = 0L

    internal val tickTimes = LongArray(100)
    internal var averageTickTime = 0F
        private set

    val continuousProfiler = ServerProfiler(this::tickCount)
    private var profiler: Profiler = DeadProfiler

    @Volatile
    private var delayProfilerStart = false

    internal fun start() = try {
        Messages.START.INITIAL.info(LOGGER, config.server.ip, config.server.port)
        val startTime = System.nanoTime()

        // Start up the console handler
        LOGGER.debug("Starting console handler")
        Thread(KryptonConsole(this)::start, "Console Handler").apply {
            uncaughtExceptionHandler = DefaultUncaughtExceptionHandler(logger("CONSOLE"))
            isDaemon = true
        }.start()

        // Load the packets
        LOGGER.debug("Loading packets...")
        PacketLoader.loadAll()

        // Register the commands and console translations, and schedule a refresh of the translation repository
        // (query data.kryptonmc.org for translation metadata and download the new translations)
        LOGGER.debug("Registering commands and translations...")
        commandManager.registerBuiltins()
        TranslationRepository.scheduleRefresh()

        // Start the metrics system
        LOGGER.debug("Starting bStats metrics")
        System.setProperty("bstats.relocatecheck", "false") // Avoid relocating bStats since we want to expose it later
        KryptonMetrics.initialize(this, config.other.metrics)

        // Warn about piracy being unsupported and then load plugins
        if (!config.server.onlineMode) {
            LOGGER.info("-----------------------------------------------------------------------------------")
            Messages.PIRACY_WARNING.info(LOGGER)
            LOGGER.info("-----------------------------------------------------------------------------------")
        }
        loadPlugins()

        // Fire the event that signals the server starting. We fire it here so that plugins can listen to it as part
        // of their lifecycle, and we call it sync so plugins can finish initialising before we do
        eventManager.fireAndForgetSync(ServerStartEvent())

        // Set the last tick time (avoids initial check sending an overload warning every time)
        lastTickTime = System.currentTimeMillis()

        // Start accepting connections
        LOGGER.debug("Starting Netty...")
        IOScope.launch { nettyProcess.run() }

        // Add the shutdown hook to stop the server
        Runtime.getRuntime().addShutdownHook(Thread(::stop, "Shutdown Handler").apply { isDaemon = false })

        Messages.START.DONE.info(LOGGER, "%.3fs".format(Locale.ROOT, (System.nanoTime() - startTime) / 1.0E9))

        // Start ticking the server
        while (isRunning) {
            val nextTickTime = System.currentTimeMillis() - lastTickTime
            if (nextTickTime > 2000L && lastTickTime - lastOverloadWarning >= 15_000L) {
                Messages.TICK_OVERLOAD_WARNING.warn(LOGGER, nextTickTime, nextTickTime / 50)
                lastOverloadWarning = lastTickTime
            }
            // start profiler
            val singleTickProfiler = if (config.other.saveThreshold > 0) {
                SingleTickProfiler(config.other.saveThreshold * 1_000_000_000L, DEBUG_FOLDER.createDirectory())
            } else null
            startProfilerTick(singleTickProfiler)
            profiler.start()

            // start tick
            profiler.push("tick start event")
            eventManager.fireAndForgetSync(TickStartEvent(tickCount))
            profiler.pop()

            profiler.push("tick")
            val tickTime = measureTimeMillis(::tick)
            profiler.pop()

            // store historical tick time and update average
            profiler.push("tallying")
            val finishTime = System.currentTimeMillis()
            tickTimes[tickCount % 100] = tickTime
            averageTickTime = averageTickTime * 0.8F + tickTime * 0.19999999F
            profiler.pop()

            // end tick
            profiler.push("tick end event")
            eventManager.fireAndForgetSync(TickEndEvent(tickCount, tickTime, finishTime))
            profiler.pop()
            lastTickTime = finishTime
            profiler.end()
            endProfilerTick(singleTickProfiler)

            val sleepTime = measureTimeMillis { Thread.sleep(max(0, TICK_INTERVAL - tickTime - oversleepFactor)) }
            oversleepFactor = sleepTime - (TICK_INTERVAL - tickTime)
        }
    } catch (exception: Throwable) {
        LOGGER.error("Encountered an unexpected exception", exception)
        val report = fillReport(if (exception is ReportedException) exception.report else CrashReport("Exception in server tick loop", exception))
        val file = CURRENT_DIRECTORY.resolve("crash-reports").resolve("crash-$TIME_NOW-server.txt")
        LOGGER.error(if (report.save(file)) "This crash report has been saved to ${file.absolutePathString()}" else "Unable to save crash report to disk!")
    } finally {
        restart()
    }

    // TODO: Register plugin instances as event listeners by default
    private fun loadPlugins() {
        Messages.PLUGIN.LOAD.INITIAL.info(LOGGER)

        try {
            val pluginPath = Path.of("plugins")
            if (!pluginPath.exists()) pluginPath.createDirectory()
            if (!pluginPath.isDirectory()) {
                Messages.PLUGIN.NOT_DIRECTORY.warn(LOGGER, pluginPath)
                return
            }

            pluginManager.loadPlugins(pluginPath)
        } catch (exception: Exception) {
            Messages.PLUGIN.LOAD.FAIL.error(LOGGER, exception)
        }
        Messages.PLUGIN.LOAD.DONE.info(LOGGER, pluginManager.plugins.size)
    }

    private fun tick() {
        tickCount++
        if (playerManager.players.isEmpty()) return // don't tick if there are no players on the server
        val time = System.currentTimeMillis()

        worldManager.worlds.forEach { (_, world) ->
            profiler.push { "$world in ${world.dimensionType}" }
            if (tickCount % 20 == 0) {
                profiler.push("time sync")
                playerManager.sendToAll(PacketOutTimeUpdate(world.time, world.dayTime), world)
                profiler.pop()
            }
            world.tick(profiler)
            profiler.pop()
        }
        playerManager.tick(time)
        if (config.world.autosaveInterval > 0 && tickCount % config.world.autosaveInterval == 0) {
            profiler.push("autosave")
            Messages.AUTOSAVE.STARTED.info(LOGGER)
            worldManager.saveAll()
            Messages.AUTOSAVE.FINISHED.info(LOGGER)
            profiler.pop()
        }
    }

    fun startProfiling() {
        delayProfilerStart = true
    }

    fun finishProfiling(): ProfileResults {
        val results = continuousProfiler.results
        continuousProfiler.disable()
        return results
    }

    fun saveDebugReport(path: Path) {
        val worldsPath = path.resolve("worlds")
        worldManager.worlds.forEach { (name, world) ->
            val worldPath = worldsPath.resolve("krypton").resolve(name)
            worldPath.createDirectories()
            world.saveDebugReport(worldPath)
        }
    }

    private fun startProfilerTick(profiler: SingleTickProfiler?) {
        if (delayProfilerStart) {
            delayProfilerStart = false
            continuousProfiler.enable()
        }
        this.profiler = continuousProfiler.profiler.decorate(profiler)
    }

    private fun endProfilerTick(profiler: SingleTickProfiler?) {
        profiler?.end()
        this.profiler = continuousProfiler.profiler
    }

    override fun registerChannel(channel: Key) {
        require(channel !in RESERVED_CHANNELS) { "Cannot register reserved channel with name \"minecraft:register\" or \"minecraft:unregister\"!" }
        channels += channel
    }

    override fun unregisterChannel(channel: Key) {
        channels -= channel
        require(channel !in RESERVED_CHANNELS) { "Cannot unregister reserved channels with name \"minecraft:register\" or \"minecraft:unregister\"!" }
    }

    override fun broadcast(message: Component, permission: String?) {
        if (permission != null) {
            playerManager.players.filter { it.hasPermission(permission) }.forEach { it.sendMessage(message) }
            console.sendMessage(message)
            return
        }
        sendMessage(message)
    }

    override fun audiences() = players + console

    private fun loadConfig(): KryptonConfig {
        val configFile = CURRENT_DIRECTORY.resolve(KryptonConfig.FILE_NAME)
        val loader = HoconConfigurationLoader.builder()
            .path(configFile)
            .defaultOptions(KryptonConfig.OPTIONS)
            .build()

        val node = loader.load()
        val config = node.get<KryptonConfig>() ?: throw IOException("Unable to load configuration!")
        loader.save(node)
        return config
    }

    internal fun stop(halt: Boolean = true) {
        if (!isRunning) return // Ensure we cannot accidentally run this twice

        // stop server and shut down session manager (disconnecting all players)
        Messages.STOP.INITIAL.info(LOGGER)
        isRunning = false
        playerManager.disconnectAll()

        // save player, world and region data
        Messages.STOP.SAVE.info(LOGGER)
        worldManager.saveAll()
        playerManager.shutdown()

        // shut down plugins and unregister listeners
        Messages.STOP.PLUGINS.info(LOGGER)
        eventManager.fireAndForgetSync(ServerStopEvent())
        eventManager.shutdown()

        // shut down scheduler
        scheduler.shutdown()
        Messages.STOP.GOODBYE.info(LOGGER)

        // manually shut down Log4J 2 here so it doesn't shut down before we've finished logging
        LogManager.shutdown()

        // Finally, halt the JVM if we should (avoids shutdown hooks running)
        if (halt) Runtime.getRuntime().halt(0)
    }

    internal fun restart() {
        stop(false) // avoid halting there because we halt here
        val split = config.other.restartScript.split(" ")
        if (split.isNotEmpty()) {
            if (!Path.of(split[0]).isRegularFile()) {
                Messages.RESTART.NO_SCRIPT.print(split[0])
                Runtime.getRuntime().halt(0)
            }
            Messages.RESTART.ATTEMPT.print(split[0])
            val os = System.getProperty("os.name").lowercase()
            Runtime.getRuntime().exec((if ("win" in os) "cmd /c start " else "sh ") + config.other.restartScript)
        }
        Runtime.getRuntime().halt(0)
    }

    private fun fillReport(report: CrashReport): CrashReport = report.apply {
        systemDetails["Player Count"] = { "${playerManager.players.size} / ${status.maxPlayers}" }
    }

    object KryptonServerInfo : Server.ServerInfo {

        override val name = "Krypton"
        override val version: String
        override val minecraftVersion: String

        init {
            val infoProperties = Properties().apply {
                load(Thread.currentThread().contextClassLoader.getResourceAsStream("META-INF/krypton.properties"))
            }
            version = infoProperties.getProperty("krypton.version")
            minecraftVersion = infoProperties.getProperty("minecraft.version")
        }
    }

    companion object {

        val REGISTER_CHANNEL_KEY = key("register")
        private val UNREGISTER_CHANNEL_KEY = key("unregister")
        private val RESERVED_CHANNELS = setOf(REGISTER_CHANNEL_KEY, UNREGISTER_CHANNEL_KEY)

        private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")
        private val TIME_NOW: String get() = LocalDateTime.now().format(DATE_FORMAT)

        private const val TICK_INTERVAL = 1000L / 20L // milliseconds in a tick
        private val LOGGER = logger<KryptonServer>()
    }
}

data class KryptonStatusInfo(
    override val maxPlayers: Int,
    override val motd: Component
) : StatusInfo

val CURRENT_DIRECTORY: Path = Path.of("").toAbsolutePath()

val GSON: Gson = GsonComponentSerializer.gson().serializer().newBuilder {
    registerTypeAdapter<UUID>(MojangUUIDSerializer)
    registerTypeAdapter<RegistryBlock>(RegistryBlock.Companion)
    registerTypeAdapter<RegistryBlockState>(RegistryBlockState.Companion)
    registerTypeAdapter<Gamemode>(GamemodeSerializer)
    registerTypeAdapter<Difficulty>(DifficultySerializer)
    registerTypeAdapter<MetadataResponse>(MetadataResponse)
}
