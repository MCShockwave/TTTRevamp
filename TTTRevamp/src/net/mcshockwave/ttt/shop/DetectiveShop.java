package net.mcshockwave.ttt.shop;

import net.mcshockwave.Guns.Gun;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public enum DetectiveShop {

	Healer(
		"Healer",
		Material.BOAT,
		1,
		0,
		2,
		2,
		"Place down to allow others to heal",
		"themselves by punching it.",
		"200 uses, each use heals 1/2 heart"),
	Teleporter(
		"Teleporter",
		Material.CLAY_BALL,
		1,
		0,
		1,
		1,
		"Left click to set a waypoint,",
		"Right click to go to that waypoint.",
		"Unlimited uses. 3 second chargeup."),
	Binoculars(
		"Binoculars",
		Material.QUARTZ,
		1,
		0,
		1,
		1,
		"Right click a body from a distance to",
		"inspect it."),
	Golden_Gun(
		"Golden Gun",
		Material.GOLD_AXE,
		1,
		0,
		2,
		1,
		"Shoot at a player to instantly kill them",
		"if they are a traitor. If they are innocent,",
		"you die."),
	UMP_Prototype(
		"UMP Prototype",
		Material.STONE_HOE,
		1,
		0,
		2,
		1,
		"A gun similar to the Mac10, but gives nausea",
		"when you hit someone."),
	Wire_Cutters(
		"Wire Cutters",
		Material.FLINT,
		1,
		0,
		1,
		3,
		"Disarm a C4 instantly and safely by right-clicking.");

	public String					display, desc[];
	public int						cost;

	public Material					m;
	public int						am, da;
	public String[]					lore;

	public int						limit;

	public HashMap<String, Integer>	timesBought	= new HashMap<>();

	private DetectiveShop(String display, Material m, int am, int da, int cost, int limit, String... desc) {
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
		if (this == Healer) {
			p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.BOAT), "§6Healer"));
		}
		if (this == Teleporter) {
			p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.CLAY_BALL), "§6Teleporter"));
		}
		if (this == Binoculars) {
			p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.QUARTZ), "§6Binoculars"));
		}
		if (this == Golden_Gun) {
			p.getInventory().addItem(Gun.TTT_GOLDEN_GUN.getItem());
		}
		if (this == UMP_Prototype) {
			p.getInventory().addItem(Gun.TTT_UMP.getItem());
		}
		if (this == Wire_Cutters) {
			p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.FLINT), "§6Wire Cutters"));
		}
	}

}
