package br.com.battlebits.commons.bukkit.util;

import java.util.UUID;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.util.mojang.UUIDGetter;

public class BukkitUUID implements UUIDGetter {
	@Override
	public UUID getUuid(String str) {
		Player player = BukkitMain.getInstance().getServer().getPlayer(str);
		if (player == null)
			return null;
		return player.getUniqueId();
	}
}
