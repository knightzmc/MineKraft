package org.kryptonmc.krypton.command.argument.serializer.minecraft

import io.netty.buffer.ByteBuf
import org.kryptonmc.krypton.command.argument.serializer.ArgumentSerializer
import org.kryptonmc.krypton.command.arguments.entities.EntityArgument

/**
 * A serialiser for [EntityArgument]
 */
class EntityArgumentSerializer : ArgumentSerializer<EntityArgument> {

    override fun write(argument: EntityArgument, buf: ByteBuf) {
        val mask = when (argument.type) {
            EntityArgument.EntityType.ENTITY -> 0 or 0x01
            EntityArgument.EntityType.PLAYER -> 0 or 0x02
        }
        buf.writeByte(mask)
    }

}
