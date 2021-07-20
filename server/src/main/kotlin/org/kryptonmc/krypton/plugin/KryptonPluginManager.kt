/*
 * This file is part of the Krypton project, and originates from the Velocity project,
 * licensed under the GNU General Public License v3.0
 *
 * Copyright (C) 2018 Velocity Contributors
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
 *
 * For the original file that this file is derived from, see here:
 * https://github.com/VelocityPowered/Velocity/blob/dev/1.1.0/proxy/src/main/java/com/velocitypowered/proxy/plugin/VelocityPluginManager.java
 */
package org.kryptonmc.krypton.plugin

import com.google.inject.Module
import org.kryptonmc.api.plugin.PluginContainer
import org.kryptonmc.api.plugin.PluginDescription
import org.kryptonmc.api.plugin.PluginManager
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.locale.Messages
import org.kryptonmc.krypton.module.GlobalModule
import org.kryptonmc.krypton.module.PluginModule
import org.kryptonmc.krypton.plugin.loader.LoadedPluginDescription
import org.kryptonmc.krypton.plugin.loader.PluginLoader
import org.kryptonmc.krypton.util.forEachDirectoryEntry
import org.kryptonmc.krypton.util.logger
import java.nio.file.Path
import java.util.IdentityHashMap
import kotlin.io.path.isRegularFile

class KryptonPluginManager(private val server: KryptonServer) : PluginManager {

    private val pluginMap = LinkedHashMap<String, PluginContainer>()
    private val pluginInstances = IdentityHashMap<Any, PluginContainer>()

    override val plugins: Collection<PluginContainer>
        get() = pluginMap.values

    fun loadPlugins(directory: Path) {
        val found = mutableListOf<PluginDescription>()

        directory.forEachDirectoryEntry({ it.isRegularFile() && it.toString().endsWith(".jar") }, {
            try {
                found += PluginLoader.loadDescription(it)
            } catch (exception: Exception) {
                Messages.PLUGIN.LOAD.ERROR.UNABLE.error(LOGGER, it, exception)
            }
        })
        if (found.isEmpty()) return // no plugins

        val sortedPlugins = found.sortCandidates()
        val loadedPluginsById = mutableSetOf<String>()
        val pluginContainers = LinkedHashMap<PluginContainer, Module>()

        // Load the plugins!
        sortedPlugins.forEach pluginLoad@{ candidate ->
            // Verify dependencies
            candidate.dependencies.forEach {
                if (it.isOptional || it.id in loadedPluginsById) return@forEach
                Messages.PLUGIN.LOAD.ERROR.MISSING_DEPENDENCY.error(LOGGER, candidate.id, it.id)
                return@pluginLoad
            }

            try {
                val description = PluginLoader.loadPlugin(candidate)
                val container = KryptonPluginContainer(description)
                pluginContainers[container] = PluginModule(description, container, directory)
                loadedPluginsById += description.id
            } catch (exception: Exception) {
                Messages.PLUGIN.LOAD.ERROR.CREATE_MODULE.error(LOGGER, candidate.id, exception)
            }
        }
        val commonModule = GlobalModule(server, pluginContainers.keys)

        pluginContainers.forEach { (container, module) ->
            val description = container.description as LoadedPluginDescription

            try {
                PluginLoader.createPlugin(container, module, commonModule)
            } catch (exception: Exception) {
                Messages.PLUGIN.LOAD.ERROR.CREATE_PLUGIN.error(LOGGER, description.id, exception)
                return@forEach
            }

            Messages.PLUGIN.LOAD.SUCCESS.info(LOGGER, description.id, description.version, description.authors.joinToString())
            registerPlugin(container)
        }
    }

    private fun registerPlugin(plugin: PluginContainer) {
        pluginMap[plugin.description.id] = plugin
        plugin.instance?.let { pluginInstances[it] = plugin }
    }

    override fun fromInstance(instance: Any): PluginContainer? {
        if (instance is PluginContainer) return instance
        return pluginInstances[instance]
    }

    override fun plugin(id: String) = pluginMap[id]

    override fun isLoaded(id: String) = id in pluginMap

    override fun addToClasspath(plugin: Any, path: Path) {
        val container = requireNotNull(fromInstance(plugin)) { "Plugin is not loaded!" }
        val instance = requireNotNull(container.instance) { "Plugin has no instance!" }

        val loader = instance.javaClass.classLoader
        if (loader !is PluginClassLoader) throw UnsupportedOperationException("Operation is not supported for non-Krypton plugins!")
        loader.addPath(path)
    }

    companion object {

        private val LOGGER = logger("PluginManager")
    }
}
