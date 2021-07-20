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
package org.kryptonmc.krypton.command.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import org.kryptonmc.api.adventure.toMessage
import org.kryptonmc.api.command.Sender
import org.kryptonmc.krypton.CURRENT_DIRECTORY
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.command.InternalCommand
import org.kryptonmc.krypton.locale.Messages
import org.kryptonmc.krypton.util.createDirectories
import org.kryptonmc.krypton.util.logger
import java.io.IOException
import java.nio.file.Path
import java.nio.file.spi.FileSystemProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal class DebugCommand(private val server: KryptonServer) : InternalCommand {

    override fun register(dispatcher: CommandDispatcher<Sender>) {
        dispatcher.register(literal<Sender>("debug")
            .then(literal<Sender>("start").executes { start(it.source); 1 })
            .then(literal<Sender>("stop").executes { stop(it.source); 1 })
            .then(literal<Sender>("report").executes { report(it.source); 1 })
        )
    }

    private fun start(sender: Sender) {
        if (server.continuousProfiler.isEnabled) throw ERROR_ALREADY_RUNNING.create()
        server.startProfiling()
        sender.sendMessage(translatable("commands.debug.started", listOf(text("Started the debug profiler. Type '/debug stop' to stop it."))))
    }

    private fun stop(sender: Sender) {
        if (!server.continuousProfiler.isEnabled) throw ERROR_NOT_RUNNING.create()
        val results = server.finishProfiling()
//        val file = File(DEBUG_FOLDER, "profile-results-$TIME_NOW_FORMATTED.txt")
        results.save(DEBUG_FOLDER.resolve("profile-results-$TIME_NOW_FORMATTED.txt"))

        val secondsDuration = results.duration / 1.0E9F
        val ticksPerSecond = results.durationTicks / secondsDuration
        sender.sendMessage(translatable("commands.debug.stopped", listOf(
            text("%.2f".format(Locale.ROOT, secondsDuration)),
            text(results.durationTicks),
            text("%.2f".format(ticksPerSecond))
        )))
    }

    private fun report(sender: Sender) {
        val reportFileName = "debug-report-$TIME_NOW_FORMATTED"
        try {
            val folderPath = DEBUG_FOLDER.apply { createDirectories() }
            if (ZIP_FILE_SYSTEM_PROVIDER == null) {
                val debugPath = folderPath.resolve(reportFileName)
                server.saveDebugReport(debugPath)
            } else {
                val debugPath = folderPath.resolve("$reportFileName.zip")
                ZIP_FILE_SYSTEM_PROVIDER.newFileSystem(debugPath, mapOf("create" to "true")).use { server.saveDebugReport(it.getPath("/")) }
            }
            sender.sendMessage(translatable("commands.debug.reportSaved", listOf(text(reportFileName))))
        } catch (exception: IOException) {
            Messages.COMMANDS.DEBUG_SAVE_ERROR.error(LOGGER, exception)
            sender.sendMessage(translatable("commands.debug.reportFailed"))
        }
    }

    companion object {

        private val ERROR_NOT_RUNNING = SimpleCommandExceptionType(translatable("commands.debug.notRunning").toMessage())
        private val ERROR_ALREADY_RUNNING = SimpleCommandExceptionType(translatable("commands.debug.alreadyRunning").toMessage())
        private val ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders().firstOrNull { it.scheme.equals("jar", true) }

        private val TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")
        private val TIME_NOW_FORMATTED: String get() = LocalDateTime.now().format(TIME_FORMAT)

        val DEBUG_FOLDER: Path = CURRENT_DIRECTORY.resolve("debug")
        private val LOGGER = logger<DebugCommand>()
    }
}
