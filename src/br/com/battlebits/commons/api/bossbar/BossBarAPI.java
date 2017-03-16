package br.com.battlebits.commons.api.bossbar;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Preconditions;

import br.com.battlebits.commons.api.bossbar.entity.GenericBoss;
import br.com.battlebits.commons.api.bossbar.entity.NopeBoss;
import br.com.battlebits.commons.api.bossbar.entity.DragonBoss;
import br.com.battlebits.commons.api.bossbar.entity.WitherBoss;
import br.com.battlebits.commons.bukkit.protocol.ProtocolHook;

public class BossBarAPI implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		removeBar(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		BossBarEntity entity = entityMap.get(player.getUniqueId());
		if (entity != null && !(entity instanceof NopeBoss)) {
			entity.move(event);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
		Player player = event.getPlayer();
		BossBarEntity entity = entityMap.get(player.getUniqueId());
		if (entity != null && !(entity instanceof NopeBoss)) {
			entity.remove();
			entity.spawn();
			entity.update();
		}
	}

	private static Map<UUID, BossBarEntity> entityMap = new HashMap<>();

	public static void setBar(Player player, String message, float percent) {
		Preconditions.checkNotNull(message, "Message cannot be null.");
		Preconditions.checkArgument(percent >= 0F && percent <= 100F, "Health must be between 0 and 100.");
		BossBarEntity entity = entityMap.computeIfAbsent(player.getUniqueId(), v -> createBoss(player));
		if (entity != null && !entity.hasTask() && !(entity instanceof NopeBoss)) {
			boolean update = false;
			update |= entity.setTitle(message);
			update |= entity.setHealth(percent);
			if (update)
				entity.update();
		}
	}

	public static void setBar(Player player, String message, int period) {
		Preconditions.checkNotNull(message, "Message cannot be null.");
		Preconditions.checkArgument(period > 0, "Period must be greater than 0.");
		BossBarEntity entity = entityMap.computeIfAbsent(player.getUniqueId(), v -> createBoss(player));
		if (entity != null && !entity.hasTask() && !(entity instanceof NopeBoss)) {
			entity.setTitle(message);
			entity.setHealth(100F);
			entity.update();
			entity.startTask(new BukkitRunnable() {
				float health = 100F;
				public void run() {
					health -= (100F / period);
					if (health > 1F) {
						entity.setTitle(message);
						entity.setHealth(health);
						entity.update();
					} else {
						removeBar(player);
					}
				}
			});
		}
	}

	public static boolean hasBar(Player player) {
		return entityMap.containsKey(player.getUniqueId());
	}

	public static void removeBar(Player player) {
		if (entityMap.containsKey(player.getUniqueId())) {
			BossBarEntity entity = entityMap.remove(player.getUniqueId());
			if (!(entity instanceof NopeBoss)) {
				if (entity.hasTask())
					entity.cancelTask();	
				entity.remove();
			}
		}
	}

	private static BossBarEntity createBoss(Player player) {
		switch (ProtocolHook.getVersion(player)) {
		case MINECRAFT_1_8:
			return new WitherBoss(player);
		case MINECRAFT_1_7_10:
			return new DragonBoss(player);
		case MINECRAFT_1_7_5:
			return new DragonBoss(player);
		case UNKNOWN:
			return new NopeBoss(player);
		default:
			if (ProtocolHook.isViaVersion())
				return new GenericBoss(player);
			else 
				return new NopeBoss(player);	
		}
	}
}
