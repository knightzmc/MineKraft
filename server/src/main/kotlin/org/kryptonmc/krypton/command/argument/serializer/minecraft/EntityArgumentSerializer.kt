package org.kryptonmc.krypton.command.argument.serializer.minecraft

import io.netty.buffer.ByteBuf
import org.kryptonmc.krypton.command.argument.serializer.ArgumentSerializer
import org.kryptonmc.krypton.command.arguments.EntityArgument

/**
 * A serializer for [EntityArgument]
 */
class EntityArgumentSerializer : ArgumentSerializer<EntityArgument> {

    override fun write(argument: EntityArgument, buf: ByteBuf) {
        val read = buf.readInt()
        val onlySingleEntity = read and 0x01 != 0
    }

}
