package net.mcshockwave.ttt.manage;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;

public class FileElements {

	public static final String	ELEMENT_SUFFIX	= ": ";

	public static void set(String el, String set, String w) {
		try {
			ArrayList<String> get = WorldFileUtils.fromArray(WorldFileUtils.get(w));

			boolean found = false;
			for (int i = 0; i < get.size(); i++) {
				String s = get.get(i);
				if (s.startsWith(el + ELEMENT_SUFFIX)) {
					get.set(i, el + ELEMENT_SUFFIX + set);
					found = true;
					break;
				}
			}
			if (!found) {
				get.add(el + ELEMENT_SUFFIX + set);
			}

			WorldFileUtils.set(w, get.toArray(new String[0]));
		} catch (Exception e) {
		}
	}

	public static String get(String el, String w) {
		try {
			String[] get = WorldFileUtils.get(w);

			for (int i = 0; i < get.length; i++) {
				String s = get[i];
				if (s.startsWith(el + ELEMENT_SUFFIX)) {
					String ret = s.replaceFirst(el + ELEMENT_SUFFIX, "");
					return ret;
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static void set(String el, Location set, World w) {
		set(el, set.getX() + "," + set.getY() + "," + set.getZ() + "," + set.getYaw() + "," + set.getPitch(),
				w.getName());
	}

	public static Location getLoc(String el, World w) {
		String g = get(el, w.getName());
		String[] spl = g.split(",");

		double x = Double.parseDouble(spl[0]);
		double y = Double.parseDouble(spl[1]);
		double z = Double.parseDouble(spl[2]);
		float yaw = Float.parseFloat(spl[3]);
		float pit = Float.parseFloat(spl[4]);

		return new Location(w, x, y, z, yaw, pit);
	}

	public static ArrayList<Location> getAll(String el, World w) {
		ArrayList<Location> ret = new ArrayList<>();

		int max = 0;
		while (has(el + "-" + ++max, w)) {
		}
		max--;

		for (int i = 1; i <= max; i++) {
			ret.add(getLoc(el + "-" + i, w));
		}

		return ret;
	}

	public static boolean has(String el, World w) {
		return has(el, w.getName());
	}

	public static boolean has(String el, String w) {
		return get(el, w) != null;
	}

}
