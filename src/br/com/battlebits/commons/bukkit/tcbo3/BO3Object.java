package br.com.battlebits.commons.bukkit.tcbo3;

import java.util.List;

import org.bukkit.Location;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Arquivo criado em 29/03/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
@RequiredArgsConstructor
public class BO3Object 
{	
	@NonNull
	private List<BO3Block> blocks;	
	private Location lastPaste;
	
	@SuppressWarnings("deprecation")
	public void paste(Location location) 
	{
		blocks.forEach(b -> location.clone().add(b.getX(), b.getY(), b.getZ()).getBlock().setTypeIdAndData(b.getMaterial().getId(), b.getData(), true));
		lastPaste = location.clone();		
	}
	
	@SuppressWarnings("deprecation")
	public void undo(Location location) 
	{
		blocks.forEach(b -> location.clone().add(b.getX(), b.getY(), b.getZ()).getBlock().setTypeIdAndData(0, (byte)0, true));
	}
	
	public void undo()
	{
		if (lastPaste != null)
		{
			undo(lastPaste);
			lastPaste = null;
		}		
	}
}
