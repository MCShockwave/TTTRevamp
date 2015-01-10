package net.mcshockwave.ttt.world;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlankGenerator extends ChunkGenerator {

	@Override
	public List<BlockPopulator> getDefaultPopulators(World world) {
		return Arrays.asList(new BlockPopulator[] { new BlockPopulator() {
			public void populate(World world, Random random, Chunk source) {
				for (int x = 0; x < 16; x++) {
					for (int y = 0; y < world.getMaxHeight(); y++) {
						for (int z = 0; z < 16; z++) {
							source.getBlock(x, y, z).setType(Material.AIR);
						}
					}
				}
			}
		} });
	}
}
