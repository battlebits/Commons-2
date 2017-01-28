package br.com.battlebits.commons.bukkit.scoreboard.tagmanager;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.BukkitCommon;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.scoreboard.ScoreboardAPI;

public class TagManager extends BukkitCommon {
	public TagManager(BukkitMain main) {
		super(main);
	}

	@Override
	public void onEnable() {
		registerListener(new TagListener(this));
	}

	public void removePlayerTag(Player p) {
		ScoreboardAPI.leaveCurrentTeamForOnlinePlayers(p);
	}

	@Override
	public void onDisable() {
		for (Player player : getServer().getOnlinePlayers()) {
			removePlayerTag(player);
		}
	}
}
