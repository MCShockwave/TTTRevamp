package net.mcshockwave.ttt.shop;

import net.mcshockwave.MCS.Utils.ItemMetaUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public enum TraitorShop {

	Knife(
		"Knife",
		Material.GOLD_SWORD,
		1,
		0,
		1,
		1,
		"One-hit kill anyone. One use."),
	Jihad_Bomb(
		"Jihad Bomb",
		Material.SULPHUR,
		1,
		0,
		1,
		1,
		"Right click to charge an explosive",
		"on your body. Once it explodes",
		"it will kill anyone nearby."),
	Teleporter(
		"Teleporter",
		Material.CLAY_BALL,
		1,
		0,
		1,
		1,
		"Left click to set a waypoint,",
		"Right click to go to that waypoint.",
		"Unlimited uses. 3 second chargeup.");

	public String					display, desc[];
	public int						cost;

	public Material					m;
	public int						am, da;
	public String[]					lore;

	public HashMap<String, Integer>	timesBought	= new HashMap<>();

	public int						limit;

	private TraitorShop(String display, Material m, int am, int da, int cost, int limit, String... desc) {
		this.display = display;
		this.cost = cost;
		this.desc = desc;
		ArrayList<String> lor = new ArrayList<>();
		for (String s : desc) {
			lor.add("§7§o" + s);
		}
		lor.add("");
		lor.add("§6Cost: §e" + cost + " credits");
		this.m = m;
		this.am = am;
		this.da = da;
		this.limit = limit;
		this.lore = lor.toArray(new String[0]);
	}

	public void onClick(Player p) {
		if (this == Knife) {
			p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.GOLD_SWORD), "§fKnife"));
		}
		if (this == Jihad_Bomb) {
			p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.SULPHUR), "§cJihad Bomb"));
		}
		if (this == Teleporter) {
			p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.CLAY_BALL), "§6Teleporter"));
		}
	}

}
