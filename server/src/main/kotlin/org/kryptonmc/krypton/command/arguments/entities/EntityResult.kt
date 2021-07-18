package org.kryptonmc.krypton.command.arguments.entities

import org.kryptonmc.krypton.entity.KryptonEntity

data class EntityResult(val entities: List<KryptonEntity>, val type: EntityQuery.Operation)
