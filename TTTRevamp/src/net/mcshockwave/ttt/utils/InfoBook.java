package net.mcshockwave.ttt.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class InfoBook {

	public static String[]	infoPages	= {
										// Intro
			ChatColor.DARK_GREEN + "Welcome to TTT!\n" + ChatColor.BLACK + "Table of Contents:\n" + "I: Basic Info\n"
					+ "II: How to Play\n" + "III: Weapons\n" + "IV: Grenades\n" + "V: Innocents\n" + "VI: Traitors\n"
					+ "VII: Traitor Shop\n" + "VIII: Detectives\n" + "IX: Detective Shop\n" + "X: Bodies\n",
			// Part 1
			ChatColor.BLUE
					+ "I. BASIC INFORMATION:\n"
					+ ChatColor.BLACK
					+ "The goal of the game is to kill all the traitors, for the innocents. "
					+ "Traitors must find and kill all innocents. "
					+ "But there's a catch: Traitors are outnumbered, and innocents have no idea who is innocent and who is a traitor.",
			// Part 2
			ChatColor.BLUE + "II. HOW TO PLAY:\n" + ChatColor.BLACK + "The game starts after 45 seconds in the lobby. "
					+ "You have a 30 second grace period at the start: Use this time to prepare. "
					+ "After grace period, your role is chosen and the game starts.",
			// Part 3
			ChatColor.BLUE + "III. WEAPONS:\n" + ChatColor.BLACK + "NAME | DMG | ACC | RoF\n§nPrimary:\n§0"
					+ "Rifle §46 §5HIGH §6SLOW\n§0Shotgun §46.5 §5POOR §6SLOW\n§0"
					+ "Mac10 §42 §5GOOD §6AUTO\n§0HUGE 249 §43.5 §5OK §6AUTO\n§0"
					+ "AK47 §43.5 §5GOOD §6AUTO\n§0§nSecondary:\n§0"
					+ "Deagle §46 §5GOOD §6MED\n§0Glock §44 §5GOOD §6FAST",
			// Part 4
			ChatColor.BLUE + "IV. GRENADES:\n" + ChatColor.BLACK
					+ "§nIncendiary Grenade:§0\n\nSets nearby players on fire\n\n§nSmoke Grenade:§0\n"
					+ "Blinds nearby players§0\n\n§nDiscombobulator:§0\nLaunches things in the air",
			// Part 5
			ChatColor.BLUE + "V. INNOCENTS:\n" + ChatColor.BLACK + "Innocents are the majority of the game's players."
					+ " They try to find the traitors and murder them.\n" + "As an innocent, " + ChatColor.RED
					+ "TRUST NO ONE\n§0Note: Randomly killing people is against the rules, look out for proof!",
			// Part 6
			ChatColor.BLUE + "VI. TRAITORS:\n" + ChatColor.BLACK
					+ "Traitors are massively outnumbered: 6 innocents to 1 traitor."
					+ " They know who is a traitor and who is innocent."
					+ " They target the innocents and murder them in cold blood.",
			// Part 7
			ChatColor.BLUE
					+ "VII. TRAITOR SHOP:\n"
					+ ChatColor.BLACK
					+ "The traitors have a shop, accessed by opening their inventory and clicking the icon."
					+ " In order to buy things, you need credits. Credits are obtained by killing innocents (+1) or detectives (+2)."
					+ " Simply click on an item to buy it.",
			// Part 8
			ChatColor.BLUE + "VIII. DETECTIVES:\n" + ChatColor.BLACK
					+ "Detective are innocents, but have extra perks. "
					+ "For instance, they can see the nearest person when someone died when inspecting a body. "
					+ "However, they are a bigger target to traitors. There is 1 detective per 8 people.",
			// Part 9
			ChatColor.BLUE + "IX. DETECTIVE SHOP:\n" + ChatColor.BLACK
					+ "The detectives have a shop, accessed by opening their inventory and clicking the icon."
					+ " In order to buy things, you need credits. Credits are obtained by killing traitors (+2)."
					+ " Simply click on an item to buy it.",
			// Part 10
			ChatColor.BLUE
					+ "X. Bodies:\n"
					+ ChatColor.BLACK
					+ "When you die, you leave a body (skeleton) behind."
					+ " You are not shown as dead in the player list (paper in inventory) until your body is inspected."
					+ " Inspecting bodies (right-click) yields useful information about how a person died." };

	public static ItemStack getBookItem() {
		ItemStack ret = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bm = (BookMeta) ret.getItemMeta();
		bm.setAuthor("§6§oMCShockwave");
		bm.setTitle("§eTutorial: TTT");
		bm.setPages(infoPages);
		ret.setItemMeta(bm);
		return ret;
	}

}
