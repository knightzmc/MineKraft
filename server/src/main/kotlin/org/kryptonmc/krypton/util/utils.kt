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
package org.kryptonmc.krypton.util

import com.google.common.net.InetAddresses
import it.unimi.dsi.fastutil.Hash
import java.math.BigInteger
import java.net.InetAddress
import java.security.AccessController
import java.security.MessageDigest
import java.security.PrivilegedAction
import java.util.*

// Avoid lookups
fun String.toInetAddress(): InetAddress = InetAddresses.forString(this)

fun String.toIntRange(): IntRange {
    return if (startsWith("..")) {
        val string = replace("..", "").toInt()
        IntRange(360, string)
    } else {
        val values = split("..")
        IntRange(values[0].toInt(), values[1].toInt())
    }
}

fun calculatePositionChange(new: Double, old: Double) = ((new * 32 - old * 32) * 128).toInt().toShort()

// This is Yggdrasil's strange way of digesting to hex
fun MessageDigest.hexDigest(): String = BigInteger(digest()).toString(16)

fun <T> doPrivileged(action: () -> T): T = AccessController.doPrivileged(PrivilegedAction(action))

fun notSupported(message: String): Nothing = throw UnsupportedOperationException(message)

fun <T : Map<K, V>, K, V> Optional<T>.forEachPresent(action: (K, V) -> Unit) = ifPresent { it.forEach(action) }

@Suppress("UNCHECKED_CAST")
fun <K> identityStrategy(): Hash.Strategy<K> = IdentityStrategy as Hash.Strategy<K>

private object IdentityStrategy : Hash.Strategy<Any> {

    override fun hashCode(o: Any?) = System.identityHashCode(o)
    override fun equals(a: Any?, b: Any?) = a === b
}
