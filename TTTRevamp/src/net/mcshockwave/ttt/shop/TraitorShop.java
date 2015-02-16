package net.mcshockwave.ttt.shop;

import net.mcshockwave.Guns.Gun;
import net.mcshockwave.Guns.addons.Addon;
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
		"Unlimited uses. 3 second chargeup."),
	Flare_Gun(
		"Flare Gun",
		Material.GOLD_PICKAXE,
		1,
		0,
		2,
		1,
		"Shoot corpses to burn their remains,",
		"removing all evidence of their death."),
	Poison_Dart_Gun(
		"Poison Dart Gun",
		Material.DIAMOND_PICKAXE,
		1,
		0,
		2,
		1,
		"Shoot someone to give them permanent wither,",
		"making them slowly die."),
	Poltergeist(
		"Poltergeist",
		Material.IRON_PICKAXE,
		1,
		0,
		1,
		1,
		"Shoot a corpse or player to launch them far",
		"away."),
	C4(
		"C4",
		Material.TNT,
		1,
		0,
		1,
		1,
		"Place to plant. It will slowly and quietly",
		"tick down for 45 seconds, increasing the",
		"number of ticks until it reaches 0, blowing",
		"up and killing anyone around it. Players can",
		"try to disarm it by clicking it and choosing",
		"a wire. If they fail, it blows up.");

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
		if (this == Flare_Gun) {
			p.getInventory().addItem(Gun.TTT_FLARE_GUN.getItem());
		}
		if (this == Poison_Dart_Gun) {
			p.getInventory().addItem(Gun.TTT_POISON_GUN.getItem());
		}
		if (this == Poltergeist) {
			p.getInventory().addItem(Addon.Bottomless_Clip.add(Gun.TTT_POLTERGEIST.getItem()));
		}
		if (this == C4) {
			p.getInventory().addItem(ItemMetaUtils.setItemName(new ItemStack(Material.TNT), "§cC4 - Place to plant"));
		}
	}

}
