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

import com.google.common.util.concurrent.MoreExecutors
import org.kryptonmc.krypton.util.reports.ReportedException
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinWorkerThread
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

private val WORKER_COUNT = AtomicInteger(1)
private val LOGGER = logger("Krypton")
val BOOTSTRAP_EXECUTOR = createExecutor("Bootstrap")

private fun createExecutor(name: String): ExecutorService {
    val parallelism = (Runtime.getRuntime().availableProcessors() - 1).clamp(1, 7)
    if (parallelism <= 0) return MoreExecutors.newDirectExecutorService()
    return ForkJoinPool(parallelism, {
        object : ForkJoinWorkerThread(it) {
            override fun onTermination(exception: Throwable) {
                LOGGER.warn("${getName()} died", exception)
                super.onTermination(exception)
            }
        }.apply { setName("Worker-$name-${WORKER_COUNT.getAndIncrement()}") }
    }, ::onThreadException, true)
}

private fun onThreadException(thread: Thread, throwable: Throwable) {
    var exception = throwable
    if (exception is CompletionException) exception = exception.cause!!
    if (exception is ReportedException) {
        println(exception.report.report)
        exitProcess(-1)
    }
    LOGGER.error("Caught exception in thread $thread!", exception)
}
