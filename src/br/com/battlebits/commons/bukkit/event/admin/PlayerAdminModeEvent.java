package br.com.battlebits.commons.bukkit.event.admin;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.event.PlayerCancellableEvent;
import lombok.Getter;

@Getter
public class PlayerAdminModeEvent extends PlayerCancellableEvent {

	private AdminMode adminMode;

	public PlayerAdminModeEvent(Player player, AdminMode adminMode) {
		super(player);
		this.adminMode = adminMode;
	}

	public static enum AdminMode {
		ADMIN, //
		PLAYER
	}

}
