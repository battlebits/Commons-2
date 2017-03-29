package br.com.battlebits.commons.bukkit.tcbo3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Arquivo criado em 29/03/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
public class BO3Common 
{
	public static List<BO3Block> loadByFile(File file) throws Exception
	{
		List<BO3Block> blocks = new ArrayList<>();
		
		try (Scanner scanner = new Scanner(file))
		{
			String line = scanner.nextLine();
			
			if (line.startsWith("Block(") && line.endsWith(")"))
			{
				String[] data = line.replace("Block(", "").replace(")", "").split(",");
				
				int x = Integer.parseInt(data[0]);
				int y = Integer.parseInt(data[1]);
				int z = Integer.parseInt(data[2]);
				
				String[] block = data[3].split(":");
				Material bType = Material.valueOf(block[0]);
				byte bData = block.length > 1 ? Byte.parseByte(block[1]) : 0;
				
				blocks.add(new BO3Block(x, y, z, bType, bData));
			}
		}

		return blocks;
	}
	
	@SuppressWarnings("deprecation")
	public static void paste(Location location, List<BO3Block> blocks)
	{
		blocks.forEach(b -> location.clone().add(b.getX(), b.getY(), b.getZ()).getBlock().setTypeIdAndData(b.getMaterial().getId(), b.getData(), true));
	}
	
	@SuppressWarnings("deprecation")
	public static void undo(Location location, List<BO3Block> blocks)
	{
		blocks.forEach(b -> location.clone().add(b.getX(), b.getY(), b.getZ()).getBlock().setTypeIdAndData(0, (byte)0, true));
	}
}
