package net.mcshockwave.ttt.utils;

import org.bukkit.Material;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public enum DamageCauseInfo {

	BLOCK_EXPLOSION(
		DamageCause.BLOCK_EXPLOSION,
		"It seems they were killed by a large explosion",
		Material.TNT),
	CONTACT(
		DamageCause.CONTACT,
		"The spikes on their body suggests a cactus caused their demise",
		Material.CACTUS),
	CUSTOM(
		DamageCause.CUSTOM,
		"Their cause of death is unknown, it seems a supernatural force killed them",
		Material.BEACON),
	DROWNING(
		DamageCause.DROWNING,
		"Their lungs are filled with water, it seems that they drowned",
		Material.WATER_BUCKET),
	ENTITY_ATTACK(
		DamageCause.ENTITY_ATTACK,
		"It seems they were murdered by a living being",
		Material.IRON_SWORD),
	ENTITY_EXPLOSION(
		DamageCause.ENTITY_EXPLOSION,
		"The singed body suggests an explosion was the cause of death",
		Material.TNT),
	FALL(
		DamageCause.FALL,
		"It seems that this person fell from a high location, because of the broken legs",
		Material.LEATHER_BOOTS),
	FALLING_BLOCK(
		DamageCause.FALLING_BLOCK,
		"The severe head injuries suggest a large block to the head caused this death",
		Material.ANVIL),
	FIRE(
		DamageCause.FIRE,
		"The scorched skin and burnt clothes proposes fire was the method of demise",
		Material.FLINT_AND_STEEL),
	FIRE_TICK(
		DamageCause.FIRE_TICK,
		"This person seems to have burnt to death, because of the burns on their body",
		Material.FLINT_AND_STEEL),
	LAVA(
		DamageCause.LAVA,
		"The fact the body is in lava is enough to determine the cause of death",
		Material.LAVA_BUCKET),
	LIGHTNING(
		DamageCause.LIGHTNING,
		"I'm not sure how someone could get electrocuted to death, possibly lightning?",
		Material.FIREBALL),
	MAGIC(
		DamageCause.MAGIC,
		"It seems a magical force caused this person's death",
		Material.CAULDRON_ITEM),
	MELTING(
		DamageCause.MELTING,
		"How does someone melt to death?",
		Material.ICE),
	POISON(
		DamageCause.POISON,
		"The game broke, it seems poison killed this person",
		Material.SPIDER_EYE),
	PROJECTILE(
		DamageCause.PROJECTILE,
		"A projectile, maybe a bullet, was the cause of death",
		Material.ARROW),
	STARVATION(
		DamageCause.STARVATION,
		"This person ran out of food somehow",
		Material.COOKED_BEEF),
	SUFFOCATION(
		DamageCause.SUFFOCATION,
		"Their face is blue, it seems the method of death was suffocation",
		Material.SAND),
	SUICIDE(
		DamageCause.SUICIDE,
		"This person was tired of the world and took their own life",
		Material.GOLD_SWORD),
	THORNS(
		DamageCause.THORNS,
		"How does someone get thorns in TTT?",
		Material.CACTUS),
	VOID(
		DamageCause.VOID,
		"The body is ripped to shreds, maybe they fell into the endless abyss",
		Material.ENDER_PEARL),
	WITHER(
		DamageCause.WITHER,
		"A magical force governed by the Wither itself was this person's demise",
		Material.SOUL_SAND);

	public DamageCause	cause;
	public String		info;
	public Material		icon;

	private DamageCauseInfo(DamageCause cause, String info, Material icon) {
		this.cause = cause;
		this.info = info;
		this.icon = icon;
	}

	public static DamageCauseInfo getInfoFor(DamageCause cause) {
		for (DamageCauseInfo info : values()) {
			if (info.cause == cause) {
				return info;
			}
		}
		return null;
	}

}
