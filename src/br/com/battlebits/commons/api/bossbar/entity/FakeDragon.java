package br.com.battlebits.commons.api.bossbar.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class FakeDragon extends FakeBoss 
{
	public FakeDragon(Player player) 
	{
		super(player);
		spawn();
	}
	
	@Override
	public void spawn()
	{
		if (!isAlive())
		{
			setAlive(true);
			
			Location dragon = getPlayer().getLocation().clone();
			PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
			
			/* Integers */
			packet.getIntegers().write(0, getId());
			packet.getIntegers().write(1, 63);
			packet.getIntegers().write(2, dragon.getBlockX() * 32);
			packet.getIntegers().write(3, -9600);
			packet.getIntegers().write(4, dragon.getBlockZ() * 32);
			packet.getIntegers().write(5, 0);
			packet.getIntegers().write(6, 0);
			packet.getIntegers().write(7, 0);
			
			/* Bytes */
			packet.getBytes().write(0, (byte)0);
			packet.getBytes().write(1, (byte)0);
			packet.getBytes().write(2, (byte)0);
	
			sendPacket(getPlayer(), packet);
		}		
	}

	@Override
	public void remove() 
	{
		if (isAlive())
		{
			PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
			packet.getIntegerArrays().write(0, new int[] { getId() });
			sendPacket(getPlayer(), packet);
			setAlive(false);
		}
	}

	@Override
	public void update() 
	{
		if (isAlive())
		{
			/* DataWatcher for Ender Dragon. */
			WrappedDataWatcher watcher = new WrappedDataWatcher();
			watcher.setObject(0, (byte) 0x20);
			watcher.setObject(2, getDisplayName());
			watcher.setObject(3, (byte) 1);
			watcher.setObject(5, 0);
			watcher.setObject(6, getHealth());
			watcher.setObject(7, 0);
			watcher.setObject(8, (byte) 0);
			watcher.setObject(9, (byte) 0);
			watcher.setObject(10, getDisplayName());
			watcher.setObject(11, (byte) 1);
			watcher.setObject(17, 0);
			watcher.setObject(18, 0);
			watcher.setObject(19, 0);
			watcher.setObject(20, 1000);

			/* Update DataWatcher */
			PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
			packet.getIntegers().write(0, getId());
			packet.getDataWatcherModifier().write(0, watcher);
			sendPacket(getPlayer(), packet);
		}
	}
	
	@Override
	public void move(PlayerMoveEvent event)
	{
		if (isAlive())
		{
			Location to = event.getTo();
			Location from = event.getFrom();
			
			if ((to.getBlockX() != from.getBlockX()) && (to.getBlockY() != from.getBlockY()) && (to.getBlockZ() != from.getBlockZ()))
			{
				PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
				
				/* Integers */
				packet.getIntegers().write(0, getId());
				packet.getIntegers().write(1, to.getBlockX() * 32);
				packet.getIntegers().write(2, -9600);
				packet.getIntegers().write(3, to.getBlockZ() * 32);
				
				/* Bytes */
				packet.getBytes().write(0, (byte)0);
				packet.getBytes().write(1, (byte)0);

				/* Boolean */
				packet.getBooleans().write(0, false);
				
				sendPacket(getPlayer(), packet);
			}
		}
	}
}
