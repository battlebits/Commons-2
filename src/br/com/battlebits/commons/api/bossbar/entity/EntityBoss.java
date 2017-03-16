package br.com.battlebits.commons.api.bossbar.entity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;

import br.com.battlebits.commons.bukkit.BukkitMain;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public abstract class EntityBoss
{
	@NonNull
	@Getter
	private Player player;
	
	@Getter
	private String displayName;
	
	@Getter
	private float health;
	
	@Getter
	@Setter
	protected int id;
	
	protected int generateId()
	{
		try
		{
			Class<?> clazz = MinecraftReflection.getEntityClass();
			Field field = clazz.getDeclaredField("entityCount");
			field.setAccessible(true);
			int id = field.getInt(null);
			field.set(null, id+1);
			return id;
		}
		catch (Exception e)
		{
			return -1;
		}
	}
	
	public boolean setDisplayName(String displayName) 
	{
		if (!Objects.equals(this.displayName, displayName))
		{
			this.displayName = displayName;
			return true;
		}
		
		return false;
	}
	
	public boolean setHealth(float percent)
	{
		float maxHealth = (this instanceof FakeWither ? 300F : 200F);
		float newHealth = Math.max(1F, (percent * 100F) / maxHealth);
		
		if (!Objects.equals(this.health, newHealth))
		{
			this.health = newHealth;
			return true;
		}
		
		return false;
	}
	
	protected void sendPacket(Player player, PacketContainer packet)
	{
		try 
		{
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
		}
		catch (InvocationTargetException e) 
		{
			e.printStackTrace();
		}
	}
	
	public boolean isAlive() 
	{
		return id > 0;
	}
	
	public void setAlive(boolean alive) 
	{
		this.id = (alive ? generateId() : -1);
	}
	
	public abstract void spawn();
	public abstract void remove();
	public abstract void update();
	public abstract void move(PlayerMoveEvent event);
	
	private BukkitTask task;

	public void startTask(BukkitRunnable runnable)
	{
		if (task == null) task = runnable.runTaskTimer(BukkitMain.getInstance(), 20L, 20L);
	}
	
	public void cancelTask()
	{
		if (task != null) task.cancel();
	}
	
	public boolean hasTask() 
	{
		return task != null;
	}
}
