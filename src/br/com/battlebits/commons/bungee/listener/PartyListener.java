package br.com.battlebits.commons.bungee.listener;

import java.util.UUID;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.party.Party;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PartyListener implements Listener
{
	@EventHandler
	public void onServerConnect(ServerConnectEvent event)
	{
		ProxiedPlayer player = event.getPlayer();
		
		if (player.getServer() != null && !event.getTarget().getName().startsWith("Lobby"))
		{
			Party party = BattlebitsAPI.getPartyCommon().getByOwner(player.getUniqueId());
			
			if (party != null)
			{
				for (UUID uuid : party.getMembers())
				{
					ProxiedPlayer member = ProxyServer.getInstance().getPlayer(uuid);
					
					if (member != null && member.getServer() != null && !member.getServer().getInfo().equals(event.getTarget()))
					{
						member.connect(event.getTarget());
					}
				}
			}
		}
	}
}
