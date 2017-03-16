package br.com.battlebits.commons.api.bossbar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.bossbar.entity.EntityBoss;
import br.com.battlebits.commons.api.bossbar.entity.FakeBoss;
import br.com.battlebits.commons.api.bossbar.entity.FakeDragon;
import br.com.battlebits.commons.api.bossbar.entity.FakeWither;
import br.com.battlebits.commons.bukkit.protocol.ProtocolHook;

public class BossBarAPI implements Listener
{
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		removeBar(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		EntityBoss boss = fakeBoss.get(player.getUniqueId());
		
		if (boss == null)
			return;
		
		boss.move(event);
	}
	
	private static Map<UUID, EntityBoss> fakeBoss = new HashMap<>();
	
	public static void setBar(Player player, String message, float percent)
	{
		EntityBoss boss = fakeBoss.computeIfAbsent(player.getUniqueId(), v -> createBoss(player));
		
		if (!boss.hasTask())
		{
			boolean update = false;
			
			update |= boss.setDisplayName(message);
			update |= boss.setHealth(percent);
			
			if (update)
				boss.update();
		}
	}
	
	public static void setBar(Player player, String message, int period)
	{
		EntityBoss boss = fakeBoss.computeIfAbsent(player.getUniqueId(), v -> createBoss(player));
		
		boss.setDisplayName(message);
		boss.setHealth(100F);
		boss.update();
		
		boss.startTask(new BukkitRunnable() 
		{
			float health = 100F;
			
			@Override
			public void run() 
			{
				health -= (100F / period);
				
				if (health > 1F)
				{
					int i = (int) (health / (100F / period))+1;
					BattlebitsAPI.debug("BOSSBAR TEMPO RESTANTE > " + i);
					boss.setDisplayName(message);
					boss.setHealth(health);
					boss.update();
				}
				else
				{
					removeBar(player);
				}				
			}
		});
	}
	
	public static boolean hasBar(Player player)
	{
		return fakeBoss.containsKey(player.getUniqueId());
	}
	
	public static void removeBar(Player player)
	{
		if (fakeBoss.containsKey(player.getUniqueId()))
		{
			EntityBoss boss = fakeBoss.remove(player.getUniqueId());
			
			if (boss.hasTask())
				boss.cancelTask();
			
			boss.remove();
		}
	}
	
	private static EntityBoss createBoss(Player player)
	{
		switch (ProtocolHook.getVersion(player))
		{
		    case UNKNOWN: 
			    return null;
		    case MINECRAFT_1_8:
		    	return new FakeWither(player);
		    case MINECRAFT_1_7_10:
		    	return new FakeDragon(player);
		    case MINECRAFT_1_7_5:
		    	return new FakeDragon(player);
		    default:
		    	return new FakeBoss(player);
		}		
	}
}
