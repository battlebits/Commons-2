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

import com.google.common.base.Preconditions;

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
		EntityBoss boss = entityMap.get(player.getUniqueId());
		
		if (boss == null)
			return;
		
		boss.move(event);
	}
	
	private static Map<UUID, EntityBoss> entityMap = new HashMap<>();
	
	public static void setBar(Player player, String message, float percent)
	{
		Preconditions.checkNotNull(message, "Message cannot be null.");
	    Preconditions.checkArgument(percent >= 0F && percent <= 100F, "Health must be between 0 and 100.");
		
		EntityBoss boss = entityMap.computeIfAbsent(player.getUniqueId(), v -> createBoss(player));
		
		if (boss != null && !boss.hasTask())
		{
			boolean update = false;
			
			update |= boss.setTitle(message);
			update |= boss.setHealth(percent);
			
			if (update)
				boss.update();
		}
	}
	
	public static void setBar(Player player, String message, int period)
	{
		Preconditions.checkNotNull(message, "Message cannot be null.");
		Preconditions.checkArgument(period > 0, "Period must be greater than 0.");
		
		EntityBoss boss = entityMap.computeIfAbsent(player.getUniqueId(), v -> createBoss(player));
		
		if (boss != null && !boss.hasTask())
		{
			boss.setTitle(message);
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
						boss.setTitle(message);
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
	}
	
	public static boolean hasBar(Player player)
	{
		return entityMap.containsKey(player.getUniqueId());
	}
	
	public static void removeBar(Player player)
	{
		if (entityMap.containsKey(player.getUniqueId()))
		{
			EntityBoss boss = entityMap.remove(player.getUniqueId());
			
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
		    	BattlebitsAPI.debug("ProtocolVersion is UNKNOWN");
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
