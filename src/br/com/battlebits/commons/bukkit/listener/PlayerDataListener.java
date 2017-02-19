package br.com.battlebits.commons.bukkit.listener;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import br.com.battlebits.commons.bukkit.BukkitMain;

public class PlayerDataListener implements Listener {

	private final HashSet<File> playerDataFolders;

	public PlayerDataListener() {
		playerDataFolders = new HashSet<>();
		addDataFolders();
	}

	private void addDataFolders() {
		List<String> enabledWorlds = BukkitMain.getInstance().getConfig().getStringList("enabled-worlds");
		for (String world : enabledWorlds) {
			File dataFolder = getDataFolder(world);
			if (dataFolder.exists())
				playerDataFolders.add(dataFolder);
		}
	}

	private File getDataFolder(String worldName) {
		File worldFolder = new File(
				BukkitMain.getInstance().getServer().getWorldContainer().getPath() + File.separatorChar + worldName);
		return new File(worldFolder.getPath() + File.separatorChar + "playerdata");
	}

	private File getPlayerFile(File dataFolder, UUID playerUUID) {
		return new File(dataFolder.getPath() + File.separatorChar + playerUUID.toString() + ".dat");
	}

	void removePlayerFile(UUID playerUUID) {
		for (File file : playerDataFolders) {
			File playerFile = getPlayerFile(file, playerUUID);
			playerFile.delete();
		}
	}

	private void removePlayerAsyncDelayed(final UUID playerUUID) {
		BukkitMain.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(BukkitMain.getInstance(),
				new Runnable() {
					@Override
					public void run() {
						removePlayerFile(playerUUID);
					}
				}, 60L);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if (BukkitMain.getInstance().isRemovePlayerDat())
			removePlayerAsyncDelayed(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerKick(PlayerKickEvent e) {
		if (BukkitMain.getInstance().isRemovePlayerDat())
			removePlayerAsyncDelayed(e.getPlayer().getUniqueId());
	}
}