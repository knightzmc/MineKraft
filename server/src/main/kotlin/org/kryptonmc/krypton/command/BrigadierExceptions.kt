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

import com.mojang.brigadier.exceptions.BuiltInExceptionProvider
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.Component.translatable
import org.kryptonmc.api.adventure.toMessage

// Built-in types using vanilla's translation keys
object BrigadierExceptions : BuiltInExceptionProvider {

    // Out of range/incorrect errors
    private val INTEGER_TOO_SMALL = dynamic2ExceptionType("argument.integer.low")
    private val INTEGER_TOO_LARGE = dynamic2ExceptionType("argument.integer.big")
    private val LONG_TOO_SMALL = dynamic2ExceptionType("argument.long.low")
    private val LONG_TOO_LARGE = dynamic2ExceptionType("argument.long.big")
    private val FLOAT_TOO_SMALL = dynamic2ExceptionType("argument.float.low")
    private val FLOAT_TOO_LARGE = dynamic2ExceptionType("argument.float.big")
    private val DOUBLE_TOO_SMALL = dynamic2ExceptionType("argument.double.low")
    private val DOUBLE_TOO_LARGE = dynamic2ExceptionType("argument.double.big")
    private val LITERAL_INCORRECT = dynamicExceptionType("argument.literal.incorrect")

    // Reader invalid/expected errors
    private val READER_INVALID_ESCAPE = dynamicExceptionType("parsing.quote.escape")
    private val READER_INVALID_BOOLEAN = dynamicExceptionType("parsing.bool.invalid")
    private val READER_INVALID_INTEGER = dynamicExceptionType("parsing.int.invalid")
    private val READER_INVALID_LONG = dynamicExceptionType("parsing.long.invalid")
    private val READER_INVALID_FLOAT = dynamicExceptionType("parsing.float.invalid")
    private val READER_INVALID_DOUBLE = dynamicExceptionType("parsing.double.invalid")
    private val READER_EXPECTED_BOOLEAN = simpleExceptionType("parsing.bool.expected")
    private val READER_EXPECTED_INTEGER = simpleExceptionType("parsing.int.expected")
    private val READER_EXPECTED_LONG = simpleExceptionType("parsing.long.expected")
    private val READER_EXPECTED_FLOAT = simpleExceptionType("parsing.float.expected")
    private val READER_EXPECTED_DOUBLE = simpleExceptionType("parsing.double.expected")
    private val READER_EXPECTED_START_OF_QUOTE = simpleExceptionType("parsing.quote.expected.start")
    private val READER_EXPECTED_END_OF_QUOTE = simpleExceptionType("parsing.quote.expected.end")
    private val READER_EXPECTED_SYMBOL = dynamicExceptionType("parsing.expected")

    // Dispatcher errors
    private val DISPATCHER_UNKNOWN_COMMAND = simpleExceptionType("command.unknown.command")
    private val DISPATCHER_UNKNOWN_ARGUMENT = simpleExceptionType("command.unknown.argument")
    private val DISPATCHER_EXPECTED_SEPARATOR = simpleExceptionType("command.expected.separator")
    private val DISPATCHER_PARSE_EXCEPTION = dynamicExceptionType("command.exception")

    override fun integerTooLow() = INTEGER_TOO_SMALL
    override fun integerTooHigh() = INTEGER_TOO_LARGE
    override fun longTooLow() = LONG_TOO_SMALL
    override fun longTooHigh() = LONG_TOO_LARGE
    override fun floatTooLow() = FLOAT_TOO_SMALL
    override fun floatTooHigh() = FLOAT_TOO_LARGE
    override fun doubleTooLow() = DOUBLE_TOO_SMALL
    override fun doubleTooHigh() = DOUBLE_TOO_LARGE
    override fun literalIncorrect() = LITERAL_INCORRECT
    override fun readerInvalidEscape() = READER_INVALID_ESCAPE
    override fun readerInvalidBool() = READER_INVALID_BOOLEAN
    override fun readerInvalidInt() = READER_INVALID_INTEGER
    override fun readerInvalidLong() = READER_INVALID_LONG
    override fun readerInvalidFloat() = READER_INVALID_FLOAT
    override fun readerInvalidDouble() = READER_INVALID_DOUBLE
    override fun readerExpectedBool() = READER_EXPECTED_BOOLEAN
    override fun readerExpectedInt() = READER_EXPECTED_INTEGER
    override fun readerExpectedLong() = READER_EXPECTED_LONG
    override fun readerExpectedFloat() = READER_EXPECTED_FLOAT
    override fun readerExpectedDouble() = READER_EXPECTED_DOUBLE
    override fun readerExpectedStartOfQuote() = READER_EXPECTED_END_OF_QUOTE
    override fun readerExpectedEndOfQuote() = READER_EXPECTED_END_OF_QUOTE
    override fun readerExpectedSymbol() = READER_EXPECTED_SYMBOL
    override fun dispatcherUnknownCommand() = DISPATCHER_UNKNOWN_COMMAND
    override fun dispatcherUnknownArgument() = DISPATCHER_UNKNOWN_ARGUMENT
    override fun dispatcherExpectedArgumentSeparator() = DISPATCHER_EXPECTED_SEPARATOR
    override fun dispatcherParseException() = DISPATCHER_PARSE_EXCEPTION
}

private fun translatable(key: String, vararg arguments: Any) =
    translatable(key, arguments.map { text(it.toString()) }).toMessage()

private fun simpleExceptionType(key: String) = SimpleCommandExceptionType(translatable(key).toMessage())

private fun dynamicExceptionType(key: String) = DynamicCommandExceptionType { translatable(key, it) }

private fun dynamic2ExceptionType(key: String) = Dynamic2CommandExceptionType { a, b -> translatable(key, a, b) }
