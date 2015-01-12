package net.mcshockwave.ttt;

import net.mcshockwave.Guns.addons.Addon;
import net.mcshockwave.Guns.descriptors.Category;
import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.Utils.ItemMetaUtils;
import net.mcshockwave.MCS.Utils.SchedulerUtils;
import net.mcshockwave.ttt.GameWorlds.GameMap;
import net.mcshockwave.ttt.manage.FileElements;
import net.mcshockwave.ttt.utils.PlayerUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Random;

public class GameManager {

	public static final int	minPlayers	= 4;

	public static Random	rand		= new Random();

	public static void enable() {
		GameWorlds.init();

		new BukkitRunnable() {
			public void run() {
				if (getPlayers().size() >= minPlayers) {
					startCount();
				}
			}
		}.runTaskLater(TroubleInTerroristTown.ins, 60);
	}

	public static SchedulerUtils	count	= null;

	public static GameMap			map		= null;

	public static void startCount() {
		state = GameState.LOBBY;

		count = SchedulerUtils.getNew();
		int[] broad = { 45, 30, 15, 10, 5, 4, 3, 2, 1 };
		for (int i = 0; i < broad.length; i++) {
			final int time = broad[i];
			count.add(new Runnable() {
				public void run() {
					MCShockwave.broadcast("Game starting in %s second" + (time == 1 ? "" : "s"), time);

					if (time == 15) {
						map = GameWorlds.mapList.get(rand.nextInt(GameWorlds.mapList.size()));
						GameWorlds.addWorld(map.world);

						MCShockwave.broadcast("Map Chosen: %s by %s", map.name, map.author);
					}

					if (time == 10) {
						World w = GameWorlds.getGameWorld();
						String[] gr = { "doMobSpawning:false", "doDaylightCycle:false", "doMobLoot:false",
								"doTileDrops:false", "mobGriefing:false" };
						for (String s : gr) {
							String[] ss = s.split(":");
							w.setGameRuleValue(ss[0], ss[1]);
						}

						w.setTime(FileElements.has("time", w.getName()) ? Integer.parseInt(FileElements.get("time",
								w.getName())) : 5000);
					}

					if (time <= 5) {
						for (Player p : getPlayers()) {
							p.playSound(p.getLocation(), Sound.ORB_PICKUP, 10, (float) time / 2.5f);
						}
					}
				}
			});
			int delay = (i >= broad.length - 1 ? 1 : (time - broad[i + 1])) * 20;
			count.add(delay);
		}
		count.add(new Runnable() {
			public void run() {
				prepare();
			}
		});
		count.execute();
	}

	public static int	time	= 0;

	public static void registerScoreboard(Player p) {
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

		Scoreboard s = p.getScoreboard();
		s.registerNewObjective("Sidebar", "dummy").setDisplaySlot(DisplaySlot.SIDEBAR);

		if (state == GameState.GAME) {
			Team det = s.registerNewTeam("Detectives");
			det.setAllowFriendlyFire(true);
			det.setCanSeeFriendlyInvisibles(false);
			det.setDisplayName("§9Detectives");
			det.setPrefix("§b§l[D] §9");
			for (Player d : Role.Detective.getPlayers()) {
				det.addEntry(d.getName());
			}

			if (Role.getRole(p) == Role.Traitor) {
				Team tr = s.registerNewTeam("Traitors");
				tr.setAllowFriendlyFire(true);
				tr.setCanSeeFriendlyInvisibles(true);
				tr.setDisplayName("§cTraitors");
				tr.setPrefix("§4§l[T] §c");

				for (Player t : Role.Traitor.getPlayers()) {
					tr.addEntry(t.getName());
				}
			}
		}
		updateScoreboard(p);
	}

	public static void updateScoreboard(Player p) {
		Scoreboard s = p.getScoreboard();
		Objective side = s.getObjective(DisplaySlot.SIDEBAR);

		String t = String.format("%d:%02d", time / 60, (time % 60));
		side.setDisplayName("§cTTT§7 " + t);

		Score blank = side.getScore("§f");
		blank.setScore(1);

		String prep = "§7Preparing";
		String role = Role.getRole(p) != null ? Role.getRole(p).color + Role.getRole(p).name() : "§8Spectator";

		if (state == GameState.PREPARING) {
			side.getScore(prep).setScore(1);
			side.getScore(prep).setScore(0);
		}
		if (state == GameState.GAME) {
			s.resetScores(prep);

			side.getScore(role).setScore(1);
			side.getScore(role).setScore(0);
		}
	}

	public static void registerScoreboards() {
		for (Player p : getPlayers()) {
			try {
				registerScoreboard(p);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public static void updateScoreboards() {
		for (Player p : getPlayers()) {
			try {
				updateScoreboard(p);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	public static void basicKit(Player p) {
		PlayerUtils.clearInv(p);
		p.getInventory().setItem(0, ItemMetaUtils.setItemName(new ItemStack(Material.IRON_SWORD), "§fCrowbar"));
		p.getInventory().addItem(
				Addon.Infinite_Ammo.add(Category.TTT.getGuns()[rand.nextInt(Category.TTT.getGuns().length)].getItem()));
	}

	public static void prepare() {
		state = GameState.PREPARING;

		MCShockwave.broadcast("Game %s!", "started");
		MCShockwave.broadcast(ChatColor.GRAY, "%s", "Preparing...");

		for (Player p : getPlayers()) {
			p.teleport(GameWorlds.getGameWorld().getSpawnLocation());
			registerScoreboards();
			basicKit(p);
		}

		count = SchedulerUtils.getNew();
		for (int t = 30; t >= 0; t--) {
			final int t2 = t;
			count.add(20);
			count.add(new Runnable() {
				public void run() {
					time = t2;
					updateScoreboards();
				}
			});
		}
		count.add(new Runnable() {
			public void run() {
				begin();
			}
		});
		count.execute();
	}

	public static void begin() {
		state = GameState.GAME;

		time = 0;

		Role.generate();

		for (Role r : Role.values()) {
			for (Player p : r.getPlayers()) {
				MCShockwave.send(r.color, p, "You are a" + (isVowel(r.name().charAt(0)) ? "n" : "") + " %s!", r.name());
			}
		}

		int tsi = Role.Traitor.players.size();
		MCShockwave.broadcast(ChatColor.RED, "There " + (tsi == 1 ? "is" : "are") + " %s traitor"
				+ (tsi == 1 ? "" : "s") + " this round", tsi);

		registerScoreboards();
	}

	public static boolean isVowel(char c) {
		return "AEIOUaeiou".indexOf(c) != -1;
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> ret = new ArrayList<>();

		ret.addAll(Bukkit.getOnlinePlayers());

		return ret;
	}

	public static ArrayList<Player> getPlayers(boolean includeSpecs) {
		if (includeSpecs) {
			return getPlayers();
		}
		ArrayList<Player> ret = new ArrayList<>();

		for (Player p : getPlayers()) {
			if (!specs.contains(p.getName())) {
				ret.add(p);
			}
		}

		return ret;
	}

	public static void stop(final String winner) {
		state = GameState.END;

		specs.clear();

		MCShockwave.broadcast("Game %s!", "ended");
		if (Role.Traitor.all.size() > 0) {
			int si = Role.Traitor.all.size();
			String ts = "";
			for (int i = 0; i < si; i++) {
				ts += "%s, ";
			}
			ts = ts.substring(0, ts.length() - 2);
			MCShockwave.broadcast(ChatColor.RED, "The traitor" + (si == 1 ? " was" : "s were") + " " + ts,
					Role.Traitor.all.toArray(new Object[0]));
		}

		GameWorlds.generateLobby(GameWorlds.Lobby.w);

		count = SchedulerUtils.getNew();
		count.add(60);
		count.add(new Runnable() {
			public void run() {
				for (Player p : getPlayers()) {
					p.teleport(GameWorlds.Lobby.w.getSpawnLocation());
					p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
					PlayerUtils.resetPlayer(p);

					for (Player p2 : getPlayers()) {
						p2.showPlayer(p);
					}
				}

				for (Role r : Role.values()) {
					r.players.clear();
					r.all.clear();
				}
				state = GameState.LOBBY;
			}
		});
		count.add(20);
		count.add(new Runnable() {
			public void run() {
				if (winner != null && getPlayers().size() >= minPlayers) {
					startCount();
				}
			}
		});
		count.execute();
	}

	public static ArrayList<String>	specs	= new ArrayList<>();

	public static void spectate(Player p, boolean hide) {
		specs.add(p.getName());

		PlayerUtils.resetPlayer(p);

		p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0));
		if (hide) {
			for (Player p2 : getPlayers(false)) {
				p2.hidePlayer(p);
			}
		}

		p.setAllowFlight(true);
	}

	public static void onDeath(Player p, PlayerDeathEvent event) {
		Role r = Role.getRole(p);

		if (r != null) {
			r.players.remove(p.getName());

			if (p.getKiller() != null && Role.getRole(p.getKiller()) != null) {
				MCShockwave.send(Role.getRole(p.getKiller()).color, p, "Person who killed you was a %s!",
						Role.getRole(p.getKiller()).name());
			}

			MCShockwave.broadcast(r.color, "%s was a" + (isVowel(r.name().charAt(0)) ? "n" : "") + " %s!", p.getName(),
					r.name());
		}

		if (state == GameState.GAME) {
			if (Role.Traitor.getPlayers().size() == 0) {
				MCShockwave.broadcast(Role.Innocent.color, "%s win!", "Innocents");
				stop(null);
			} else if (Role.Detective.getPlayers().size() == 0 && Role.Innocent.getPlayers().size() == 0) {
				MCShockwave.broadcast(Role.Traitor.color, "%s win!", "Traitors");
				stop(null);
			}
		}

		updateScoreboards();
	}

	public static GameState	state	= GameState.IDLE;

	public static enum GameState {
		IDLE,
		LOBBY,
		PREPARING,
		GAME,
		END;
	}

}
