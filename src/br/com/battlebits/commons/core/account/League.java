package br.com.battlebits.commons.core.account;

import net.md_5.bungee.api.ChatColor;

public enum League {
	UNRANKED(ChatColor.WHITE + "-", 1000), //
	PRIMARY(ChatColor.GREEN + "☰", 2000), //
	ADVANCED(ChatColor.YELLOW + "☲", 2000), //
	EXPERT(ChatColor.DARK_BLUE + "☷", 3000), //
	SILVER(ChatColor.GRAY + "✶", 3000), //
	GOLD(ChatColor.GOLD + "✷", 4000), //
	DIAMOND(ChatColor.AQUA + "✦", 4000), //
	ELITE(ChatColor.DARK_PURPLE + "✹", 5000), //
	MASTER(ChatColor.RED + "✫", 5000), //
	LEGENDARY(ChatColor.DARK_RED + "✪", Integer.MAX_VALUE);

	private String symbol;
	private int maxXp;

	private League(String symbol, int xp) {
		this.symbol = symbol;
		this.maxXp = xp;
	}

	public int getMaxXp() {
		return maxXp;
	}

	public String getSymbol() {
		return symbol;
	}

	public League getNextLeague() {
		return League.values()[ordinal() + 1];
	}
}
