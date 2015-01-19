package net.mcshockwave.ttt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;

public enum Role {

	Innocent(
		ChatColor.GREEN),
	Traitor(
		ChatColor.RED),
	Detective(
		ChatColor.BLUE);

	public ArrayList<String>	players	= new ArrayList<>();
	public ArrayList<String>	all		= new ArrayList<>();
	public ChatColor			color;

	Role(ChatColor color) {
		this.color = color;
	}

	public int getAmountForPlayers(int count) {
		if (this == Innocent) {
			return count - (Traitor.getAmountForPlayers(count) + Detective.getAmountForPlayers(count));
		} else if (this == Detective) {
			return count / 8;
		} else if (this == Traitor) {
			return (count / 6) + 1;
		}
		return 0;
	}

	public static Role getRole(Player p) {
		return getRole(p.getName());
	}

	public static Role getRole(String s) {
		for (Role r : values()) {
			if (r.players.contains(s)) {
				return r;
			}
		}
		return null;
	}

	public static Role getPastRole(Player p) {
		return getPastRole(p.getName());
	}

	public static Role getPastRole(String s) {
		for (Role r : values()) {
			if (r.all.contains(s)) {
				return r;
			}
		}
		return null;
	}

	public static Player getRandomPlayer(Role r) {
		ArrayList<Player> pool = GameManager.getPlayers(false);
		Collections.shuffle(pool);
		for (Player p : pool) {
			if (getRole(p) == null) {
				return p;
			}
		}
		return null;
	}

	public ArrayList<Player> getPlayers() {
		ArrayList<Player> ret = new ArrayList<>();
		for (String s : players) {
			if (Bukkit.getPlayer(s) != null) {
				ret.add(Bukkit.getPlayer(s));
			}
		}
		return ret;
	}

	public static void generate() {
		int count = GameManager.getPlayers(false).size();

		for (Role r : values()) {
			for (int i = 0; i < r.getAmountForPlayers(count); i++) {
				Player p = getRandomPlayer(r);
				if (p != null) {
					r.players.add(p.getName());
					r.all.add(p.getName());
				}
			}
		}
	}

}
