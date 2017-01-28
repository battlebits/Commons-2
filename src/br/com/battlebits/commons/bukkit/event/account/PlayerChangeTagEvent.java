package br.com.battlebits.commons.bukkit.event.account;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.event.PlayerCancellableEvent;
import br.com.battlebits.commons.core.account.Tag;
import lombok.Getter;

@Getter
public class PlayerChangeTagEvent extends PlayerCancellableEvent {

	private Tag oldTag;
	private Tag newTag;
	private boolean isForced;

	public PlayerChangeTagEvent(Player p, Tag oldTag, Tag newTag, boolean isForced) {
		super(p);
		this.oldTag = oldTag;
		this.newTag = newTag;
		this.isForced = isForced;
	}

}
