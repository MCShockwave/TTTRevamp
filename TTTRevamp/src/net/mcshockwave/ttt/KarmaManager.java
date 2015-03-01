package net.mcshockwave.ttt;

import net.mcshockwave.MCS.MCShockwave;
import net.mcshockwave.MCS.SQLTable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class KarmaManager {

	public static final int						KARMA_LIMIT	= 600, MAX_KARMA = 1200;

	public static HashMap<String, KarmaChange>	karmaChange	= new HashMap<>();

	public static void changeKarmaFor(String name, boolean sendMsgs) {
		if (sendMsgs && Bukkit.getPlayer(name) != null) {
			Player p = Bukkit.getPlayer(name);
			int ch = karmaChange.containsKey(name) ? karmaChange.get(name).getNet() : 0;
			if (karmaChange.containsKey(name)) {
				for (Entry<Role, Integer> re : karmaChange.get(name).reasons.entrySet()) {
					Role r = re.getKey();
					int d = re.getValue();
					MCShockwave.send(d < 0 ? ChatColor.RED : ChatColor.GREEN, p, (d < 0 ? "" : "+")
							+ "%s karma for damaging " + r.color + r.name() + "s", d);
				}
				p.sendMessage("§8-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
			}
			MCShockwave.send(ch < 0 ? ChatColor.RED : ChatColor.GREEN, p, ch == 0 ? "No %s change this round"
					: (ch < 0 ? "" : "+") + "%s karma this round", ch == 0 ? "Karma" : ch);
			int k = getKarma(name, true);
			if (k <= KARMA_LIMIT) {
				p.kickPlayer("§cYour karma has gotten too low!");
			} else if (k < KARMA_LIMIT + 200) {
				MCShockwave.send(ChatColor.RED, p, "Warning! You are %s karma away from being karma-banned!", k
						- KARMA_LIMIT);
			}
		}
		SQLTable.Karma.set("Karma", getKarma(name, true) + "", "Username", name);
		karmaChange.remove(name);
	}

	public static int getKarma(String name) {
		return getKarma(name, false);
	}

	public static int getKarma(String name, boolean includeChange) {
		int day = SQLTable.Karma.getInt("Username", name, "Day");
		long curDay = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis());
		int karma = 1000;
		if (!SQLTable.Karma.has("Username", name)) {
			SQLTable.Karma.add("Username", name, "Karma", karma + "", "Day", curDay + "");
		}
		if (day < curDay) {
			SQLTable.Karma.set("Day", curDay + "", "Username", name);
			SQLTable.Karma.set("Karma", karma + "", "Username", name);
		} else {
			karma = SQLTable.Karma.getInt("Username", name, "Karma");
		}
		if (includeChange && karmaChange.containsKey(name)) {
			karma += karmaChange.get(name).getNet();
		}
		if (karma > MAX_KARMA) {
			karma = MAX_KARMA;
		}
		return karma;
	}

	public static void addKarma(String name, int am, Role r) {
		if (!karmaChange.containsKey(name)) {
			karmaChange.put(name, new KarmaChange());
		}
		karmaChange.get(name).add(am, r);
	}

	public static class KarmaChange {
		public HashMap<Role, Integer>	reasons;

		public KarmaChange() {
			reasons = new HashMap<>();
		}

		public int getNet() {
			int ret = 0;
			for (int i : reasons.values()) {
				ret += i;
			}
			return ret;
		}

		public void add(int am, Role r) {
			if (reasons.containsKey(r)) {
				am = reasons.get(r) + am;
				reasons.remove(r);
			}
			reasons.put(r, am);
		}
	}
}
