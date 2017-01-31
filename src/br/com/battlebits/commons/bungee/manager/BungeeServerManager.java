package br.com.battlebits.commons.bungee.manager;

import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.core.server.ServerManager;

public class BungeeServerManager extends ServerManager{

	@Override
	public void addActiveServer(String serverAddress, String serverIp, int maxPlayers) {
		super.addActiveServer(serverAddress, serverIp, maxPlayers);
		BungeeMain.getPlugin().addBungee(serverIp, serverAddress.split(":")[0],
				Integer.valueOf(serverAddress.split(":")[1]));
	}
}
