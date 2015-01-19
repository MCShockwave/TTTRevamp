package net.mcshockwave.ttt.shop;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.HashMap;

public class ShopManager {

	public static HashMap<String, Integer>	credits	= new HashMap<>();

	public static int getCredits(String name) {
		return credits.get(name);
	}

	public static void setCredits(String name, int am) {
		credits.remove(name);
		credits.put(name, am);
	}

	public static void addCredits(String name, int am) {
		setCredits(name, getCredits(name) + am);
	}

	public static ItemMenu getMenu(Player p, boolean isTraitor) {
		int size = 9;
		ItemMenu m = new ItemMenu("Shop - " + (isTraitor ? "Traitor" : "Detective"), size);
		int id = 0;
		if (isTraitor) {
			for (final TraitorShop ts : TraitorShop.values()) {
				if (ts.timesBought.containsKey(p.getName()) && ts.timesBought.get(p.getName()) >= ts.limit) {
					continue;
				}
				Button b = new Button(true, ts.m, ts.am, ts.da, "§c" + ts.display, ts.lore);
				b.setOnClick(new ButtonRunnable() {
					public void run(Player p, InventoryClickEvent event) {
						ts.onClick(p);
						MCShockwave.send(ChatColor.RED, p, "Bought %s for %s credit" + (ts.cost == 1 ? "" : "s") + "!",
								ts.display, ts.cost);
						addCredits(p.getName(), -ts.cost);
						int tb = 0;
						if (ts.timesBought.containsKey(p.getName())) {
							tb = ts.timesBought.get(p.getName());
						}
						ts.timesBought.remove(p.getName());
						ts.timesBought.put(p.getName(), ++tb);
					}
				});
				m.addButton(b, id++);
			}
		} else {
			for (final DetectiveShop ds : DetectiveShop.values()) {
				if (ds.timesBought.containsKey(p.getName()) && ds.timesBought.get(p.getName()) > ds.limit) {
					continue;
				}
				Button b = new Button(true, ds.m, ds.am, ds.da, "§b" + ds.display, ds.lore);
				b.setOnClick(new ButtonRunnable() {
					public void run(Player p, InventoryClickEvent event) {
						ds.onClick(p);
						MCShockwave.send(ChatColor.AQUA, p,
								"Bought %s for %s credit" + (ds.cost == 1 ? "" : "s") + "!", ds.display, ds.cost);
						addCredits(p.getName(), -ds.cost);
						int tb = 0;
						if (ds.timesBought.containsKey(p.getName())) {
							tb = ds.timesBought.get(p.getName());
						}
						ds.timesBought.remove(p.getName());
						ds.timesBought.put(p.getName(), ++tb);
					}
				});
				m.addButton(b, id++);
			}
		}
		Button cre = new Button(false, Material.GOLD_INGOT, credits.get(p.getName()), 0, "§6Credits",
				"§7§oEarn credits by killing enemies!");
		m.addButton(cre, size - 1);
		return m;
	}
}
