package net.mcshockwave.ttt;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.ttt.utils.DamageCauseInfo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.WordUtils;

public class CorpseManager {

	public static HashMap<String, Entity>	corpses		= new HashMap<>();
	public static ArrayList<Corpse>			corpList	= new ArrayList<>();

	public static Corpse getCorpseFromName(String name) {
		return getCorpseFromEntity(corpses.get(name));
	}

	public static Corpse getCorpseFromEntity(Entity e) {
		for (Corpse c : corpList) {
			if (c.ent.equals(e)) {
				return c;
			}
		}
		return null;
	}

	public static class Corpse {
		public Skeleton				ent				= null;
		public boolean				identified		= false, byDet = false;
		public String				name			= null;
		public DamageCauseInfo		causeOfDeath	= null;
		public String				weaponName		= null;
		public Material				weaponMat		= null;
		public long					timeOfDeath		= 0;
		public String				closest			= null;
		public ArrayList<String>	kills			= new ArrayList<>();
		public int					c4id			= -1;

		public Corpse(Location l, String name, DamageCauseInfo causeOfDeath, String weaponName, Material weaponMat) {
			ent = (Skeleton) l.getWorld().spawnEntity(l, EntityType.SKELETON);
			ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10));
			final ItemStack air = new ItemStack(Material.AIR);
			new BukkitRunnable() {
				public void run() {
					ent.getEquipment().setArmorContents(
							new ItemStack[] { air.clone(), air.clone(), air.clone(), air.clone() });
					ent.getEquipment().setItemInHand(air.clone());
				}
			}.runTaskLater(TroubleInTerroristTown.ins, 1);
			ent.setCustomNameVisible(false);
			ent.setCustomName("§4Unidentified Corpse");
			this.name = name;
			this.causeOfDeath = causeOfDeath;
			this.weaponMat = weaponMat;
			this.weaponName = weaponName;
			this.identified = false;
			this.byDet = false;
			timeOfDeath = System.currentTimeMillis();
			double closDis = -1;
			Player closPl = null;
			for (Player p : GameManager.getPlayers(false)) {
				if (!p.getName().equalsIgnoreCase(name)) {
					if (closPl == null || p.getLocation().distanceSquared(l) < closDis || closDis == -1) {
						closPl = p;
						closDis = p.getLocation().distanceSquared(l);
					}
				}
			}
			closest = closPl.getName();

			if (DefaultListener.planterWire.containsKey(name)) {
				c4id = DefaultListener.planterWire.get(name);
			}
			if (GameManager.kills.containsKey(name)) {
				kills.addAll(GameManager.kills.get(name));
			}

			corpList.add(this);
			corpses.put(name, ent);
		}

		public Role getRole() {
			return Role.getPastRole(name) != null ? Role.getPastRole(name) : Role.Innocent;
		}

		public void onClick(Player p) {
			if (!identified && !GameManager.specs.contains(p.getName())) {
				identify(p);
			}
			if (Role.getRole(p) == Role.Detective) {
				byDet = true;
			}
			getMenu().open(p);
		}

		public void identify(Player id) {
			ent.setCustomName(getRole().color + name);
			identified = true;
			MCShockwave.broadcast(getRole().color, "%s has identified the body of %s! They were "
					+ getRole().getArticle() + " %s!", "§6§o" + id.getName(), name, getRole().name());
			if (Bukkit.getPlayer(name) != null) {
				GameManager.spectate(Bukkit.getPlayer(name), true);
			}
			GameManager.updatePlayerLists();
		}

		public ItemMenu getMenu() {
			ItemMenu m = new ItemMenu("Corpse of " + name, 9);

			Button role = new Button(false, Material.WOOL, 1, getRole().color == ChatColor.BLUE ? 11
					: getRole().color == ChatColor.RED ? 14 : 5, getRole().color + "This person was "
					+ getRole().getArticle() + " " + getRole().name());
			m.addButton(role, 0);

			String name = null;
			ArrayList<String> cod = new ArrayList<>();
			for (String s : causeOfDeath.info) {
				if (name == null)
					name = "§d" + s;
				else
					cod.add("§d" + s);
			}
			Button cause = new Button(false, causeOfDeath.icon, 1, 0, name, cod.toArray(new String[0]));
			m.addButton(cause, 2);

			if (weaponMat != null && weaponName != null) {
				Button wep = new Button(false, weaponMat, 1, 0, "§eAccording to the wounds, the weapon used was a"
						+ (GameManager.isVowel(weaponName.charAt(0)) ? "n" : ""), "    §e" + weaponName);
				m.addButton(wep, 3);
			}

			long totSec = (System.currentTimeMillis() - timeOfDeath) / 1000;
			long mins = totSec / 60;
			long sec = totSec % 60;
			Button time = new Button(false, Material.WATCH, 1, 0, "§bThis person has been dead for " + mins + "m" + sec
					+ "s");
			m.addButton(time, 4);

			if (kills.size() > 0) {
				Button kills = new Button(false, Material.EMPTY_MAP, 1, 0, "§3A list of confirmed kills is as follows",
						this.kills.toArray(new String[0]));
				m.addButton(kills, 6);
			}

			if (c4id != -1) {
				Button note = new Button(false, Material.PAPER, 1, 0, "§oThe mysterious note says to cut the "
						+ WordUtils.capitalizeFully(DefaultListener.c4Colors[c4id].name()) + " wire");
				m.addButton(note, 7);
			}

			if (byDet && closest != null) {
				Button cl = new Button(false, Material.COMPASS, 1, 0, "§6The closest person on death was " + closest);
				m.addButton(cl, 8);
			}

			return m;
		}
	}

}
