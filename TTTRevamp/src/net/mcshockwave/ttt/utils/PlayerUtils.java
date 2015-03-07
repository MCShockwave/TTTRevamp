package net.mcshockwave.ttt.utils;

import net.mcshockwave.ttt.cosmetics.Hats;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PlayerUtils {

	public static void clearInv(Player p, boolean leaveHat) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		if (leaveHat) {
			Hats.putOnHat(p);
		}
	}

	public static void clearEffects(Player p) {
		for (PotionEffect pe : p.getActivePotionEffects()) {
			p.removePotionEffect(pe.getType());
		}
	}

	public static void resetPlayer(Player p, boolean leaveHat) {
		clearInv(p, leaveHat);
		clearEffects(p);
		p.setFireTicks(0);
		p.setAllowFlight(false);
		p.setMaxHealth(20);
		p.setHealth(p.getMaxHealth());
		if (p.getGameMode() != GameMode.SURVIVAL) {
			p.setGameMode(GameMode.SURVIVAL);
		}
	}

}
