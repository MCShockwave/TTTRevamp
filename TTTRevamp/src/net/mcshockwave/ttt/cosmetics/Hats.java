package net.mcshockwave.ttt.cosmetics;

import net.mcshockwave.MCS.SQLTable;
import net.mcshockwave.MCS.Currency.PointsUtils;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.MCS.Menu.ItemMenu.ButtonRunnable;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.ttt.GameManager;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public enum Hats {

	BOOKSHELF(
		"Bookshelf",
		2500),
	CHEST(
		"Chest",
		5000),
	ANVIL(
		"Anvil",
		6000),
	ENDER_CHEST(
		"Ender Chest",
		7500),
	DISPENSER(
		"Dispenser",
		7500),
	DROPPER(
		"Dropper",
		7500),
	ICE(
		"Ice",
		8000),
	GLASS(
		"Glass",
		10000),
	EMERALD_BLOCK(
		"Emerald Block",
		20000);

	public String	name;
	public int		cost;

	Hats(String name, int cost) {
		this.name = name;
		this.cost = cost;
	}

	public static HashMap<String, Hats>	selectedHats	= new HashMap<>();

	public Material getMaterial() {
		return Material.valueOf(name());
	}

	public ItemStack getStack() {
		return ItemMetaUtils.setItemName(new ItemStack(getMaterial()), "§e" + name);
	}

	public boolean doesOwn(Player p) {
		if (!SQLTable.TTTCosmetics.has("Username", p.getName())) {
			SQLTable.TTTCosmetics.add("Username", p.getName());
		}
		return SQLTable.TTTCosmetics.get("Username", p.getName(), "Hats").contains("," + name());
	}

	public static ItemMenu getMenu(Player p) {
		ItemMenu m = new ItemMenu("Hats!", values().length);
		final int pnts = PointsUtils.getPoints(p);

		int slot = -1;
		for (final Hats h : values()) {
			ArrayList<String> lore = new ArrayList<>();
			if (h.doesOwn(p)) {
				lore.add("§3Owned!");
				lore.add("§aClick to put on!");
			} else {
				lore.add("§3Cost: " + (pnts >= h.cost ? "§2" : "§4") + h.cost + " points");
				lore.add(pnts >= h.cost ? "§aClick to buy!" : "§cYou don't have enough points!");
			}

			Button b = new Button(true, h.getMaterial(), 1, 0, "§e" + h.name, lore.toArray(new String[0]));
			b.setOnClick(new ButtonRunnable() {
				public void run(Player p, InventoryClickEvent event) {
					if (!h.doesOwn(p)) {
						if (PointsUtils.getPoints(p) >= h.cost) {
							PointsUtils.addPoints(p, -h.cost, "buying the " + h.name + " hat");
							String hat = SQLTable.TTTCosmetics.get("Username", p.getName(), "Hats");
							SQLTable.TTTCosmetics.set("Hats", hat + "," + h.name(), "Username", p.getName());
						}
					} else {
						selectedHats.put(p.getName(), h);
						if (!GameManager.specs.contains(p.getName())) {
							Hats.putOnHat(p);
						}
					}
				}
			});
			m.addButton(b, ++slot);
		}

		return m;
	}

	public static void putOnHat(Player p) {
		if (selectedHats.containsKey(p.getName())) {
			ItemStack h = selectedHats.get(p.getName()).getStack();
			p.getInventory().setHelmet(h);
		}
	}
}
