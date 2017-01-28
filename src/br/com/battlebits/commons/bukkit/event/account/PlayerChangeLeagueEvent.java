package br.com.battlebits.commons.bukkit.event.account;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.account.BukkitPlayer;
import br.com.battlebits.commons.bukkit.event.PlayerCancellableEvent;
import br.com.battlebits.commons.core.account.League;
import lombok.Getter;

@Getter
public class PlayerChangeLeagueEvent extends PlayerCancellableEvent {
	private BukkitPlayer bukkitPlayer;
	private League oldLeague;
	private League newLeague;

	public PlayerChangeLeagueEvent(Player p, BukkitPlayer player, League oldLeague, League newLeague) {
		super(p);
		this.bukkitPlayer = player;
		this.oldLeague = oldLeague;
		this.newLeague = newLeague;
	}
}
