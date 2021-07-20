/*
 * This file is part of the Krypton API, licensed under the MIT license.
 *
 * Copyright (C) 2021 KryptonMC and the contributors to the Krypton project.
 *
 * This project is licensed under the terms of the MIT license.
 * For more details, please reference the LICENSE file in the api top-level directory.
 */
package org.kryptonmc.api.entity

import net.kyori.adventure.key.Key
import org.kryptonmc.api.entity.monster.Zombie
import org.kryptonmc.api.entity.player.Player
import org.kryptonmc.api.registry.Registries

/**
 * All the types of entities in the game.
 */
@Suppress("UndocumentedPublicProperty")
object EntityTypes {

    // TODO: Make each of these be of their respective entity types when they exist
    // @formatter:off
    @JvmField val AREA_EFFECT_CLOUD = register<AreaEffectCloud>("area_effect_cloud")
    @JvmField val ARMOR_STAND = register<Entity>("armor_stand")
    @JvmField val ARROW = register<Entity>("arrow")
    @JvmField val AXOLOTL = register<Entity>("axolotl")
    @JvmField val BAT = register<Entity>("bat")
    @JvmField val BEE = register<Entity>("bee")
    @JvmField val BLAZE = register<Entity>("blaze")
    @JvmField val BOAT = register<Entity>("boat")
    @JvmField val CAT = register<Entity>("cat")
    @JvmField val CAVE_SPIDER = register<Entity>("cave_spider")
    @JvmField val CHICKEN = register<Entity>("chicken")
    @JvmField val COD = register<Entity>("cod")
    @JvmField val COW = register<Entity>("cow")
    @JvmField val CREEPER = register<Entity>("creeper")
    @JvmField val DOLPHIN = register<Entity>("dolphin")
    @JvmField val DONKEY = register<Entity>("donkey")
    @JvmField val DRAGON_FIREBALL = register<Entity>("dragon_fireball")
    @JvmField val DROWNED = register<Entity>("drowned")
    @JvmField val ELDER_GUARDIAN = register<Entity>("elder_guardian")
    @JvmField val END_CRYSTAL = register<Entity>("end_crystal")
    @JvmField val ENDER_DRAGON = register<Entity>("ender_dragon")
    @JvmField val ENDERMAN = register<Entity>("enderman")
    @JvmField val ENDERMTIE = register<Entity>("endermite")
    @JvmField val EVOKER = register<Entity>("evoker")
    @JvmField val EVOKER_FANGS = register<Entity>("evoker_fangs")
    @JvmField val EXPERIENCE_ORB = register<Entity>("experience_orb")
    @JvmField val EYE_OF_ENDER = register<Entity>("eye_of_ender")
    @JvmField val FALLING_BLOCK = register<Entity>("falling_block")
    @JvmField val FIREWORK_ROCKET = register<Entity>("firework_rocket")
    @JvmField val FOX = register<Entity>("fox")
    @JvmField val GHAST = register<Entity>("ghast")
    @JvmField val GIANT = register<Entity>("giant")
    @JvmField val GLOW_ITEM_FRAME = register<Entity>("glow_item_frame")
    @JvmField val GLOW_SQUID = register<Entity>("glow_squid")
    @JvmField val GOAT = register<Entity>("goat")
    @JvmField val GUARDIAN = register<Entity>("guardian")
    @JvmField val HOGLIN = register<Entity>("hoglin")
    @JvmField val HORSE = register<Entity>("horse")
    @JvmField val HUSK = register<Entity>("husk")
    @JvmField val ILLUSIONER = register<Entity>("illusioner")
    @JvmField val IRON_GOLEM = register<Entity>("iron_golem")
    @JvmField val ITEM = register<Entity>("item")
    @JvmField val ITEM_FRAME = register<Entity>("item_frame")
    @JvmField val FIREBALL = register<Entity>("fireball")
    @JvmField val LEASH_KNOT = register<Entity>("leash_knot")
    @JvmField val LIGHTNING_BOLT = register<Entity>("lightning_bolt")
    @JvmField val LLAMA = register<Entity>("llama")
    @JvmField val LLAMA_SPIT = register<Entity>("llama_spit")
    @JvmField val MAGMA_CUBE = register<Entity>("magma_cube")
    @JvmField val MARKER = register<Entity>("marker")
    @JvmField val MINECART = register<Entity>("minecart")
    @JvmField val CHEST_MINECART = register<Entity>("chest_minecart")
    @JvmField val COMMAND_BLOCK_MINECART = register<Entity>("command_block_minecart")
    @JvmField val FURNACE_MINECART = register<Entity>("furnace_minecart")
    @JvmField val HOPPER_MINECART = register<Entity>("hopper_minecart")
    @JvmField val SPAWNER_MINECART = register<Entity>("spawner_minecart")
    @JvmField val TNT_MINECART = register<Entity>("tnt_minecart")
    @JvmField val MULE = register<Entity>("mule")
    @JvmField val MOOSHROOM = register<Entity>("mooshroom")
    @JvmField val OCELOT = register<Entity>("ocelot")
    @JvmField val PAINTING = register<Entity>("painting")
    @JvmField val PANDA = register<Entity>("panda")
    @JvmField val PARROT = register<Entity>("parrot")
    @JvmField val PHANTOM = register<Entity>("phantom")
    @JvmField val PIG = register<Entity>("pig")
    @JvmField val PIGLIN = register<Entity>("piglin")
    @JvmField val PIGLIN_BRUTE = register<Entity>("piglin_brute")
    @JvmField val PILLAGER = register<Entity>("pillager")
    @JvmField val POLAR_BEAR = register<Entity>("polar_bear")
    @JvmField val PRIMED_TNT = register<Entity>("tnt")
    @JvmField val PUFFERFISH = register<Entity>("pufferfish")
    @JvmField val RABBIT = register<Entity>("rabbit")
    @JvmField val RAVAGER = register<Entity>("ravager")
    @JvmField val SALMON = register<Entity>("salmon")
    @JvmField val SHEEP = register<Entity>("sheep")
    @JvmField val SHULKER = register<Entity>("shulker")
    @JvmField val SHULKER_BULLET = register<Entity>("shulker_bullet")
    @JvmField val SILVERFISH = register<Entity>("silverfish")
    @JvmField val SKELETON = register<Entity>("skeleton")
    @JvmField val SKELETON_HORSE = register<Entity>("skeleton_horse")
    @JvmField val SLIME = register<Entity>("slime")
    @JvmField val SMALL_FIREBALL = register<Entity>("small_fireball")
    @JvmField val SNOW_GOLEM = register<Entity>("snow_golem")
    @JvmField val SNOWBALL = register<Entity>("snowball")
    @JvmField val SPECTRAL_ARROW = register<Entity>("spectral_arrow")
    @JvmField val SPIDER = register<Entity>("spider")
    @JvmField val SQUID = register<Entity>("squid")
    @JvmField val STRAY = register<Entity>("stray")
    @JvmField val STRIDER = register<Entity>("strider")
    @JvmField val EGG = register<Entity>("egg")
    @JvmField val ENDER_PEARL = register<Entity>("ender_pearl")
    @JvmField val EXPERIENCE_BOTTLE = register<Entity>("experience_bottle")
    @JvmField val POTION = register<Entity>("potion")
    @JvmField val TRIDENT = register<Entity>("trident")
    @JvmField val TRADER_LLAMA = register<Entity>("trader_llama")
    @JvmField val TROPICAL_FISH = register<Entity>("tropical_fish")
    @JvmField val TURTLE = register<Entity>("turtle")
    @JvmField val VEX = register<Entity>("vex")
    @JvmField val VILLAGER = register<Entity>("villager")
    @JvmField val VINDICATOR = register<Entity>("vindicator")
    @JvmField val WANDERING_TRADER = register<Entity>("wandering_trader")
    @JvmField val WITCH = register<Entity>("witch")
    @JvmField val WITHER = register<Entity>("wither")
    @JvmField val WITHER_SKELETON = register<Entity>("wither_skeleton")
    @JvmField val WITHER_SKULL = register<Entity>("wither_skull")
    @JvmField val WOLF = register<Entity>("wolf")
    @JvmField val ZOGLIN = register<Entity>("zoglin")
    @JvmField val ZOMBIE = register<Zombie>("zombie")
    @JvmField val ZOMBIE_HORSE = register<Entity>("zombie_horse")
    @JvmField val ZOMBIE_VILLAGER = register<Entity>("zombie_villager")
    @JvmField val ZOMBIFIED_PIGLIN = register<Entity>("zombified_piglin")
    @JvmField val PLAYER = register<Player>("player", false)
    @JvmField val FISHING_HOOK = register<Entity>("fishing_bobber", false)
    // @formatter:on

    @Suppress("UNCHECKED_CAST") // This should never fail here
    private fun <T : Entity> register(name: String, isSummonable: Boolean = true): EntityType<T> =
        Registries.register(Registries.ENTITY_TYPE, name, EntityType<T>(Key.key(name), isSummonable)) as EntityType<T>
}
