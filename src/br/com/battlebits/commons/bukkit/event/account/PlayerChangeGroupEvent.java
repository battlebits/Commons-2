package br.com.battlebits.commons.bukkit.event.account;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.account.BukkitPlayer;
import br.com.battlebits.commons.bukkit.event.PlayerCancellableEvent;
import br.com.battlebits.commons.core.permission.Group;
import lombok.Getter;

@Getter
public class PlayerChangeGroupEvent extends PlayerCancellableEvent {
	private BukkitPlayer bukkitPlayer;
	private Group group;

	public PlayerChangeGroupEvent(Player p, BukkitPlayer player, Group group) {
		super(p);
		this.bukkitPlayer = player;
		this.group = group;
	}
}
