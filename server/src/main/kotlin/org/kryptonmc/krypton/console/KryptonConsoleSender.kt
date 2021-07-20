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
package org.kryptonmc.krypton.console

import net.kyori.adventure.audience.MessageType
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.translation.GlobalTranslator
import org.kryptonmc.api.command.ConsoleSender
import org.kryptonmc.krypton.adventure.toSectionText
import org.kryptonmc.krypton.util.logger
import java.util.Locale

/**
 * Our implementation of the API's [ConsoleSender].
 */
object KryptonConsoleSender : ConsoleSender {

    private val LOGGER = logger("CONSOLE")

    override fun sendMessage(source: Identity, message: Component, type: MessageType) {
        val component = when (message) {
            is TranslatableComponent -> GlobalTranslator.render(message, Locale.ENGLISH)
            is TextComponent -> message
            else -> return
        }
        LOGGER.info(component.toSectionText())
    }
}
