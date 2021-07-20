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

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import org.kryptonmc.krypton.KryptonServer.KryptonServerInfo
import org.kryptonmc.krypton.locale.Messages
import org.kryptonmc.krypton.locale.TranslationManager
import org.kryptonmc.krypton.util.Bootstrap
import org.kryptonmc.krypton.util.logger
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

// max memory in megabytes (bytes / 1024 / 1024)
private val MAX_MEMORY = Runtime.getRuntime().maxMemory() / 1024L / 1024L
private const val MEMORY_WARNING_THRESHOLD = 512

fun main(args: Array<String>) = KryptonCLI().main(args)

/**
 * The CLI handler for Krypton
 */
class KryptonCLI : CliktCommand() {

    private val version by option("-v", "--version").flag()
    private val locale by option("-l", "--locale").convert {
        val split = it.split("[_-]".toRegex())
        if (split.isEmpty()) return@convert Locale.ENGLISH
        if (split.size == 2) return@convert Locale(split[0], split[1])
        Locale(split[0])
    }.default(Locale.ENGLISH)

    override fun run() {
        TranslationManager.reload(locale)
        if (version) {
            Messages.VERSION_INFO.print(KryptonServerInfo.version, KryptonServerInfo.minecraftVersion)
            return
        }
        val logger = logger("Krypton")
        Messages.LOAD.info(logger, KryptonServerInfo.version, KryptonServerInfo.minecraftVersion)
        if (MAX_MEMORY < MEMORY_WARNING_THRESHOLD) Messages.LOAD_LOW_MEMORY.warn(logger, MEMORY_WARNING_THRESHOLD.toString(), KryptonServerInfo.version)

        // Run the bootstrap
        Bootstrap.preload()
        Bootstrap.validate()

        val reference = AtomicReference<KryptonServer>()
        val mainThread = Thread({ reference.get().start() }, "Server Thread").apply {
            setUncaughtExceptionHandler { _, exception -> logger<KryptonServer>().error("Caught previously unhandled exception", exception) }
        }
        val server = KryptonServer()
        reference.set(server)
        mainThread.start()
    }
}
