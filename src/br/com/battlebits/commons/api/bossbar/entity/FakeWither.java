package br.com.battlebits.commons.api.bossbar.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

public class FakeWither extends EntityBoss
{
	public FakeWither(Player player)
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
			
			PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
			packet.getDataWatcherModifier().write(0, new WrappedDataWatcher());
			packet.getIntegers().write(0, getId());
			packet.getIntegers().write(1, 64);
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
			WrappedDataWatcher watcher = new WrappedDataWatcher();
			watcher.setObject(0, (byte) 0x20);
			watcher.setObject(2, this.title);
			watcher.setObject(3, (byte) 1);
			watcher.setObject(6, this.health);
			watcher.setObject(10, this.title);
			watcher.setObject(11, (byte) 1);
			watcher.setObject(14, 881);
			watcher.setObject(20, 1000);
			
			PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
		    packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
			packet.getIntegers().write(0, getId());
			sendPacket(getPlayer(), packet);
		}	
	}
	
	private int lastX = 0, lastY = 0, lastZ = 0;
	
	public void move(PlayerMoveEvent event)
	{
		if (isAlive())
		{
			Location to = getWitherLocation(event.getTo().clone(), 24D);
			
			if ((lastX != to.getBlockX()) || (lastY != to.getBlockY()) || (lastZ != to.getBlockZ()))
			{
				PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);				
				packet.getIntegers().write(0, getId());
				packet.getIntegers().write(1, to.getBlockX() * 32);
				packet.getIntegers().write(2, to.getBlockY() * 32);
				packet.getIntegers().write(3, to.getBlockZ() * 32);
				sendPacket(getPlayer(), packet);
				
				this.lastX = to.getBlockX();
				this.lastY = to.getBlockY();
				this.lastZ = to.getBlockZ();
			}
		}
	}
	
	protected Location getWitherLocation(Location pLoc, double distance)
	{
		return pLoc.getDirection().multiply(distance).add(pLoc.toVector()).toLocation(pLoc.getWorld());
	}
}
