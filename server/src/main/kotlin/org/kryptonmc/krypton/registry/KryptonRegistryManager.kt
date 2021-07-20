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
package org.kryptonmc.krypton.registry

import net.kyori.adventure.key.Key
import org.kryptonmc.api.registry.Registry
import org.kryptonmc.api.registry.RegistryKey
import org.kryptonmc.api.registry.RegistryManager

class KryptonRegistryManager : RegistryManager {

    override fun <T : Any> register(registry: Registry<T>, key: Key, value: T) = registry.register(RegistryKey(registry.key, key), value)

    override fun <T : Any> register(registry: Registry<T>, id: Int, key: Key, value: T) = registry.register(id, RegistryKey(registry.key, key), value)

    override fun <T : Any> create(key: RegistryKey<out Registry<T>>) = KryptonRegistry(key)

    override fun <T : Any> createDefaulted(
        key: RegistryKey<out Registry<T>>,
        defaultKey: Key
    ) = KryptonDefaultedRegistry(key, defaultKey)
}
