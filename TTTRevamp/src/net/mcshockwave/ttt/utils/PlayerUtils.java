package net.mcshockwave.ttt.utils;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

public class PlayerUtils {

	public static void clearInv(Player p) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
	}

	public static void clearEffects(Player p) {
		for (PotionEffect pe : p.getActivePotionEffects()) {
			p.removePotionEffect(pe.getType());
		}
	}

	public static void resetPlayer(Player p) {
		clearInv(p);
		clearEffects(p);
		p.setFireTicks(0);
		p.setAllowFlight(false);
		p.setHealth(p.getMaxHealth());
		if (p.getGameMode() != GameMode.SURVIVAL) {
			p.setGameMode(GameMode.SURVIVAL);
		}
	}

}
