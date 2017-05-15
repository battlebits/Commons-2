package br.com.battlebits.commons.bungee.manager;

import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.core.server.ServerManager;
import br.com.battlebits.commons.core.server.ServerType;

public class BungeeServerManager extends ServerManager {

	@Override
	public void addActiveServer(String serverAddress, String serverIp, ServerType type, int maxPlayers) {
		super.addActiveServer(serverAddress, serverIp, type, maxPlayers);
		BungeeMain.getPlugin().addBungee(serverIp, serverAddress.split(":")[0], Integer.valueOf(serverAddress.split(":")[1]));
	}
}
