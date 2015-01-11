package net.mcshockwave.ttt.manage;

import net.mcshockwave.MCS.Utils.MiscUtils;

import org.bukkit.World;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class WorldFileUtils {

	public static final String	FILE_NAME	= "worldfile.txt";

	public static String[] get(File f) {
		ArrayList<String> ret = new ArrayList<>();
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);

			String read = null;
			while ((read = br.readLine()) != null) {
				ret.add(read);
			}

			br.close();
			fr.close();
		} catch (Exception e) {
		}
		return ret.toArray(new String[0]);
	}

	public static String[] get(String w) {
		return get(new File(w + File.separator + FILE_NAME));
	}

	public static String[] get(World w) {
		return get(w.getName());
	}

	public static void append(File f, String append) {
		List<String> set = new ArrayList<>();
		for (String s : get(f)) {
			set.add(s);
		}
		set.add(append);
		set(f, set.toArray(new String[0]));
	}

	public static void append(String w, String append) {
		append(new File(w + File.separator + FILE_NAME), append);
	}

	public static void append(World w, String append) {
		append(w.getName(), append);
	}

	public static void set(File f, String[] setTo) {
		String set = "";
		for (String s : setTo) {
			set += s + "\n";
		}
		try {
			set = set.substring(0, set.length() - 1);
		} catch (Exception e) {
		}

		try {
			FileOutputStream out = new FileOutputStream(f);
			out.write(set.getBytes());
			out.close();
		} catch (Exception e) {
			MiscUtils.printStackTrace(e);
		}
	}

	public static void set(String w, String[] setTo) {
		set(new File(w + File.separator + FILE_NAME), setTo);
	}

	public static void set(World w, String[] setTo) {
		set(w.getName(), setTo);
	}

	public static ArrayList<String> fromArray(String[] ar) {
		ArrayList<String> ret = new ArrayList<>();

		for (String s : ar) {
			ret.add(s);
		}

		return ret;
	}

}
