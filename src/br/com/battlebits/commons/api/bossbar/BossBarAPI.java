package br.com.battlebits.commons.api.bossbar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.bossbar.entity.FakeBoss;
import br.com.battlebits.commons.api.bossbar.entity.FakeWither;

public class BossBarAPI
{
	private static Map<UUID, FakeBoss> fakeBoss = new HashMap<>();
	
	public static void setBar(Player player, String message, float percent)
	{
		FakeBoss boss = fakeBoss.computeIfAbsent(player.getUniqueId(), v -> new FakeWither(player));
		
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
		FakeBoss boss = fakeBoss.computeIfAbsent(player.getUniqueId(), v -> new FakeWither(player));
	
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
			FakeBoss boss = fakeBoss.remove(player.getUniqueId());
			
			if (boss.hasTask())
				boss.cancelTask();
			
			boss.remove();
		}
	}
}
