package net.mcshockwave.ttt;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Entities.CustomEntityRegistrar;
import net.mcshockwave.MCS.Menu.ItemMenu;
import net.mcshockwave.MCS.Menu.ItemMenu.Button;
import net.mcshockwave.ttt.utils.DamageCauseInfo;
import net.minecraft.server.v1_7_R4.EntityCreature;
import net.minecraft.server.v1_7_R4.EntityInsentient;
import net.minecraft.server.v1_7_R4.EntitySkeleton;
import net.minecraft.server.v1_7_R4.GenericAttributes;
import net.minecraft.server.v1_7_R4.PathfinderGoal;
import net.minecraft.server.v1_7_R4.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_7_R4.Vec3D;
import net.minecraft.server.v1_7_R4.World;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;

public class CorpseManager {

	public static HashMap<String, Entity>	corpses		= new HashMap<>();
	public static ArrayList<Corpse>			corpList	= new ArrayList<>();

	public static Corpse getCorpseFromName(String name) {
		return getCorpseFromEntity(corpses.get(name));
	}

	public static Corpse getCorpseFromEntity(Entity e) {
		for (Corpse c : corpList) {
			if (c.ent.getBukkitEntity().equals(e)) {
				return c;
			}
		}
		return null;
	}

	public static class Corpse {
		public CorpseEntity		ent				= null;
		public boolean			identified		= false, byDet = false;
		public String			name			= null;
		public DamageCauseInfo	causeOfDeath	= null;
		public String			weaponName		= null;
		public Material			weaponMat		= null;
		public long				timeOfDeath		= 0;
		public String			closest			= null;

		public Corpse(Location l, String name, DamageCauseInfo causeOfDeath, String weaponName, Material weaponMat) {
			ent = spawnCorpseEntity(l, "§4Unidentified Corpse");
			((Skeleton) ent.getBukkitEntity()).addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
					Integer.MAX_VALUE, 10));
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

			corpList.add(this);
			corpses.put(name, ent.getBukkitEntity());
		}

		public Role getRole() {
			return Role.getPastRole(name) != null ? Role.getPastRole(name) : Role.Innocent;
		}

		public void identify(Player id) {
			ent.setCustomName(getRole().color + name);
			identified = true;
			MCShockwave.broadcast(getRole().color, "%s has identified the body of %s! They were "
					+ getRole().getArticle() + " %s!", "§6§o" + id.getName(), name, getRole().name());
			GameManager.updatePlayerLists();
		}

		public ItemMenu getMenu() {
			ItemMenu m = new ItemMenu("Corpse of " + name, 9);

			int detOff = byDet && closest != null ? 0 : 1;

			Button role = new Button(false, Material.EMPTY_MAP, 1, 0, getRole().color + "This person was "
					+ getRole().getArticle() + " " + getRole().name());
			m.addButton(role, 0 + detOff);

			Button cause = new Button(false, causeOfDeath.icon, 1, 0, "§d" + causeOfDeath.info);
			m.addButton(cause, 2 + detOff);

			if (weaponMat != null && weaponName != null) {
				Button wep = new Button(false, weaponMat, 1, 0, "§eAccording to the wounds, the weapon used was a"
						+ (GameManager.isVowel(weaponName.charAt(0)) ? "n" : ""), "    §e" + weaponName);
				m.addButton(wep, 4 + detOff);
			}

			long totSec = (System.currentTimeMillis() - timeOfDeath) / 1000;
			long mins = totSec / 60;
			long sec = totSec % 60;
			Button time = new Button(false, Material.WATCH, 1, 0, "§bThis person has been dead for " + mins + "m" + sec
					+ "s");
			m.addButton(time, 6 + detOff);

			if (byDet && closest != null) {
				Button cl = new Button(false, Material.COMPASS, 1, 0, "§6The closest person on death was " + closest);
				m.addButton(cl, 8);
			}

			return m;
		}
	}

	public static CorpseEntity spawnCorpseEntity(Location l, String name) {
		CorpseEntity ce = (CorpseEntity) CustomEntityRegistrar.spawnCustomEntity(CorpseEntity.class, l);
		ce.setCustomName(name);
		return ce;
	}

	public static class CorpseEntity extends EntitySkeleton {
		public PathfinderGoalGoToBlock	pathfind	= new PathfinderGoalGoToBlock(this, 0.75f, 0, 0, 0);

		public CorpseEntity(World world) {
			super(world);
			this.a(0.6F, 1.8F);
			this.getNavigation().b(true);
			this.getNavigation().a(true);
			Location l = getBukkitEntity().getLocation();
			pathfind.x = l.getX();
			pathfind.y = l.getY();
			pathfind.z = l.getZ();
			this.goalSelector.a(0, pathfind);
			this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
			this.getAttributeInstance(GenericAttributes.b).setValue(100);
		}

	}

	public static class PathfinderGoalGoToBlock extends PathfinderGoal {

		private EntityCreature	a;
		private double			b;
		private double			c;
		private double			d;
		private double			e;
		public double			x, y, z;

		public PathfinderGoalGoToBlock(EntityCreature entitycreature, double s, double x, double y, double z) {
			this.a = entitycreature;
			this.e = s;
			this.a(1);
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public boolean a() {
			Vec3D vec3d = Vec3D.a(x, y, z);

			if (vec3d == null) {
				return false;
			} else {
				this.b = vec3d.a;
				this.c = vec3d.b;
				this.d = vec3d.c;
				return true;
			}
		}

		public boolean b() {
			return !this.a.getNavigation().g();
		}

		public void c() {
			this.a.getNavigation().a(this.b, this.c, this.d, this.e);
		}
	}

}
