package br.com.battlebits.commons.bukkit;

import org.bukkit.Server;
import org.bukkit.event.Listener;

import br.com.battlebits.commons.core.BattleCommon;

public abstract class BukkitCommon extends BattleCommon {

	private BukkitMain main;

	public BukkitCommon(BukkitMain main) {
		this.main = main;
	}

	public BukkitMain getPlugin() {
		return main;
	}

	public Server getServer() {
		return main.getServer();
	}
	
	public void registerListener(Listener listener) {
		getServer().getPluginManager().registerEvents(listener, main);
	}

}
