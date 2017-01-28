package br.com.battlebits.commons.bukkit.event.account;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.account.BukkitPlayer;
import br.com.battlebits.commons.bukkit.event.PlayerCancellableEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerUpdateFieldEvent extends PlayerCancellableEvent {

	private BukkitPlayer bukkitPlayer;
	private String field;
	@Setter
	private Object object;

	public PlayerUpdateFieldEvent(Player p, BukkitPlayer player, String field, Object object) {
		super(p);
		this.bukkitPlayer = player;
		this.field = field;
		this.object = object;
	}

}
