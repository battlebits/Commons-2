package br.com.battlebits.commons.bungee.util;

import java.util.UUID;

import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.util.mojang.UUIDGetter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeUUID implements UUIDGetter {

	@Override
	public UUID getUuid(String str) {
		ProxiedPlayer player = BungeeMain.getPlugin().getProxy().getPlayer(str);
		if (player == null)
			return null;
		return player.getUniqueId();
	}

}
