package br.com.battlebits.commons.bukkit.listener;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import br.com.battlebits.commons.bukkit.BukkitMain;
import us.myles.ViaVersion.ViaVersionPlugin;
import us.myles.ViaVersion.api.Via;


public class NBTDeleteListener implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerKick(PlayerKickEvent event) {
		if (BukkitMain.getInstance().isRemovePlayerDat())
			removePlayerFile(event.getPlayer().getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (BukkitMain.getInstance().isRemovePlayerDat())
			removePlayerFile(event.getPlayer().getUniqueId());
	}
	
	private void removePlayerFile(UUID uuid) {
		
		World world = Bukkit.getWorlds().get(0);
		File playerdata = new File(world.getWorldFolder(), "playerdata");
		if (playerdata.exists() && playerdata.isDirectory()) {
			File file = new File(playerdata, uuid.toString() + ".dat");
			if (file.exists()) {
				Bukkit.getScheduler().runTaskLater(BukkitMain.getInstance(), () -> {
					if (!file.delete()) {
						removePlayerFile(uuid);
					}
				}, 2L);
			}
		}	
	}
}