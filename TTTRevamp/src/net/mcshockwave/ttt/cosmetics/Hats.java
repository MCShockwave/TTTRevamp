package net.mcshockwave.ttt.cosmetics;

public enum Hats {

	GLASS(
		"Glass",
		7500),
	ICE(
		"Ice",
		6000);

	public String	name;
	public int		cost;

	Hats(String name, int cost) {
		this.name = name;
		this.cost = cost;
	}

}
