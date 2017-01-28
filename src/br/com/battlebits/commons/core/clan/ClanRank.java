package br.com.battlebits.commons.core.clan;

import net.md_5.bungee.api.ChatColor;

public enum ClanRank {
	INITIAL(ChatColor.WHITE, Integer.MAX_VALUE);

	private ChatColor color;
	private int maxXp;

	private ClanRank(ChatColor color, int xp) {
		this.color = color;
		this.maxXp = xp;
	}

	public int getMaxXp() {
		return maxXp;
	}

	public ChatColor getColor() {
		return color;
	}

}
