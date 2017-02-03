package br.com.battlebits.commons.bukkit.event.account;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.account.BukkitPlayer;
import br.com.battlebits.commons.bukkit.event.PlayerCancellableEvent;
import br.com.battlebits.commons.core.translate.Language;
import lombok.Getter;

@Getter
public class PlayerLanguageEvent extends PlayerCancellableEvent {
	private BukkitPlayer bukkitPlayer;
	private Language language;

	public PlayerLanguageEvent(Player p, BukkitPlayer player, Language language) {
		super(p);
		this.bukkitPlayer = player;
		this.language = language;
	}
}
