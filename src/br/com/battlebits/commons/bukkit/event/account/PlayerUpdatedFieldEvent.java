package br.com.battlebits.commons.bukkit.event.account;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import br.com.battlebits.commons.bukkit.account.BukkitPlayer;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerUpdatedFieldEvent extends PlayerEvent {

	private static final HandlerList handlers = new HandlerList();

	private BukkitPlayer bukkitPlayer;
	private String field;
	@Setter
	private Object object;

	public PlayerUpdatedFieldEvent(Player p, BukkitPlayer player, String field, Object object) {
		super(p);
		this.bukkitPlayer = player;
		this.field = field;
		this.object = object;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
