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
package org.kryptonmc.krypton.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.ParseResults
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.LiteralCommandNode
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.util.TriState
import org.apache.commons.lang3.StringUtils
import org.kryptonmc.api.adventure.AdventureMessage
import org.kryptonmc.api.command.BrigadierCommand
import org.kryptonmc.api.command.CommandManager
import org.kryptonmc.api.command.RawCommand
import org.kryptonmc.api.command.Sender
import org.kryptonmc.api.command.SimpleCommand
import org.kryptonmc.api.event.play.PermissionCheckEvent
import org.kryptonmc.krypton.KryptonServer
import org.kryptonmc.krypton.command.commands.DebugCommand
import org.kryptonmc.krypton.command.commands.GamemodeCommand
import org.kryptonmc.krypton.command.commands.ListCommand
import org.kryptonmc.krypton.command.commands.MeCommand
import org.kryptonmc.krypton.command.commands.RestartCommand
import org.kryptonmc.krypton.command.commands.SayCommand
import org.kryptonmc.krypton.command.commands.SeedCommand
import org.kryptonmc.krypton.command.commands.StopCommand
import org.kryptonmc.krypton.command.commands.SummonCommand
import org.kryptonmc.krypton.command.commands.TeleportCommand
import org.kryptonmc.krypton.locale.Messages
import java.util.concurrent.CompletableFuture

class KryptonCommandManager(private val server: KryptonServer) : CommandManager {

    internal val dispatcher = CommandDispatcher<Sender>()

    override fun register(command: BrigadierCommand) = dispatcher.root.addChild(command.node)

    override fun register(command: SimpleCommand) {
        val node = buildRawArgumentsLiteral(
            command.name,
            { execute(command, it) },
            { context, builder -> suggest(command, context, builder) }
        )
        dispatcher.root.addChild(node)

        command.aliases.forEach { dispatcher.root.addChild(node.buildRedirect(it)) }
    }

    override fun register(command: RawCommand) {
        val node = buildRawArgumentsLiteral(
            command.name,
            { command.execute(it.source, it.input); 1 },
            { context, builder ->
                command.suggest(context.source, context.rawArguments).forEach { builder.suggest(it) }
                builder.buildFuture()
            }
        )
        dispatcher.root.addChild(node)

        command.aliases.forEach { dispatcher.root.addChild(node.buildRedirect(it)) }
    }

    override fun dispatch(sender: Sender, command: String) {
        try {
            if (dispatcher.execute(command.removePrefix("/"), sender) != 1) sender.sendMessage(DEFAULT_NO_PERMISSION)
        } catch (exception: CommandSyntaxException) {
            val message = exception.rawMessage
            sender.sendMessage(if (message is AdventureMessage) message.wrapped else text(exception.message.orEmpty()))
        }
    }

    /**
     * Retrieves command completion suggestions from the specified parse results
     */
    fun suggest(parseResults: ParseResults<Sender>): CompletableFuture<Suggestions> =
        dispatcher.getCompletionSuggestions(parseResults)

    private fun dispatchCommand(command: SimpleCommand, sender: Sender, args: Array<String>): Int {
        CommandScope.launch { command.execute(sender, args) }
        return 1
    }

    private fun dispatchPermissionCheck(sender: Sender, permission: String?): TriState {
        val event = PermissionCheckEvent(sender, permission, permission?.let { sender.hasPermission(it) } ?: true)
        return server.eventManager.fireSync(event).result
    }

    private fun execute(command: SimpleCommand, context: CommandContext<Sender>): Int {
        val sender = context.source
        val args = context.splitArguments

        if (command.permission == null) {
            dispatchPermissionCheck(sender, null)
            return dispatchCommand(command, sender, args)
        }

        return when (dispatchPermissionCheck(sender, command.permission)) {
            TriState.TRUE -> dispatchCommand(command, sender, args)
            TriState.FALSE -> 0
            TriState.NOT_SET -> 0
        }
    }

    private fun suggest(
        command: SimpleCommand,
        context: CommandContext<Sender>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val args = context.splitArguments
        val hasPermission = command.permission?.let(context.source::hasPermission) ?: true
        if (!hasPermission) return builder.buildFuture()

        command.suggest(context.source, args).forEach { builder.suggest(it) }
        return builder.buildFuture()
    }

    private fun LiteralCommandNode<Sender>.buildRedirect(alias: String) =
        literal<Sender>(alias.lowercase())
            .requires(requirement)
            .forward(redirect, redirectModifier, isFork)
            .executes(command)
            .apply { children.forEach { then(it) } }
            .build()

    private fun buildRawArgumentsLiteral(
        alias: String,
        brigadierCommand: com.mojang.brigadier.Command<Sender>,
        suggestionProvider: SuggestionProvider<Sender>
    ) = literal<Sender>(alias.lowercase())
        .then(argument<Sender, String>("arguments", greedyString())
            .suggests(suggestionProvider)
            .executes(brigadierCommand))
        .executes(brigadierCommand)
        .build()

    internal fun registerBuiltins() {
        register(StopCommand(server))
        register(RestartCommand(server))
        DebugCommand(server).register(dispatcher)
        TeleportCommand.register(dispatcher)
        SummonCommand.register(dispatcher)
        GamemodeCommand(server).register(dispatcher)
        ListCommand().register(dispatcher)
        SeedCommand().register(dispatcher)
        SayCommand().register(dispatcher)
        MeCommand().register(dispatcher)
    }

    companion object {

        private val DEFAULT_NO_PERMISSION = Messages.COMMAND.NO_PERMISSION().color(NamedTextColor.RED)
    }
}

private val CommandContext<Sender>.rawArguments: String
    get() {
        val firstSpace = input.indexOf(' ')
        return if (firstSpace == -1) "" else input.substring(firstSpace + 1)
    }

private val CommandContext<Sender>.splitArguments: Array<String>
    get() {
        val raw = rawArguments
        return if (raw.isEmpty()) emptyArray() else StringUtils.split(raw)
    }
