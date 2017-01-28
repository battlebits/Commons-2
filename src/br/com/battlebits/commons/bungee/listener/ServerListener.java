package br.com.battlebits.commons.bungee.listener;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.account.BattlePlayer;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ServerListener implements Listener {

	@EventHandler(priority = (byte) -128)
	public void onConnect(ServerConnectedEvent event) {
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(event.getPlayer().getUniqueId());
		player.connect(event.getServer().getInfo().getName());
	}

}
