package br.com.battlebits.commons.bukkit.scoreboard.tagmanager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bukkit.account.BukkitPlayer;
import br.com.battlebits.commons.bukkit.event.account.PlayerChangeTagEvent;
import br.com.battlebits.commons.bukkit.scoreboard.ScoreboardAPI;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.account.League;
import br.com.battlebits.commons.core.account.Tag;

public class TagListener implements Listener {
	private TagManager manager;

	public TagListener(TagManager manager) {
		this.manager = manager;
		for (Player p : manager.getServer().getOnlinePlayers()) {
			BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId());
			if (player == null)
				continue;
			player.setTag(player.getTag());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		manager.removePlayerTag(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoinListener(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		BukkitPlayer player = (BukkitPlayer) BattlebitsAPI.getAccountCommon()
				.getBattlePlayer(e.getPlayer().getUniqueId());
		if (player == null)
			return;
		if (!manager.getPlugin().isTagControl())
			return;
		player.setTag(player.getTag());
		for (Player o : Bukkit.getOnlinePlayers()) {
			if (o.getUniqueId() != p.getUniqueId()) {
				BukkitPlayer bp = (BukkitPlayer) BattlebitsAPI.getAccountCommon().getBattlePlayer(o.getUniqueId());
				if (bp == null)
					continue;
				String id = getTeamName(bp.getTag(), bp.getLeague());
				if (manager.getPlugin().isOldTag()) {
					id = chars[bp.getTag().ordinal()] + "";
				}
				String tag = bp.getTag().getPrefix();
				String league = " §7(" + bp.getLeague().getSymbol() + "§7)";
				if (manager.getPlugin().isOldTag()) {
					tag = tag.substring(tag.length() - 2, tag.length());
					league = "";
				}
				ScoreboardAPI.joinTeam(ScoreboardAPI.createTeamIfNotExistsToPlayer(p, id,
						tag + (ChatColor.stripColor(tag).trim().length() > 0 ? " " : ""), league), o);
				bp = null;
			}
			o = null;
		}
		player = null;
		p = null;
	}

	@EventHandler
	public void onPlayerChangeTagListener(PlayerChangeTagEvent e) {
		Player p = e.getPlayer();
		if (!manager.getPlugin().isTagControl())
			return;
		if (p == null) {
			System.out.println("NULL TagListener.java linha 75");
			return;
		}
		BukkitPlayer player = (BukkitPlayer) BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId());
		if (player == null)
			return;
		String id = getTeamName(e.getNewTag(), player.getLeague());
		String oldId = getTeamName(e.getOldTag(), player.getLeague());
		if (manager.getPlugin().isOldTag()) {
			id = chars[e.getNewTag().ordinal()] + "";
		}
		for (final Player o : Bukkit.getOnlinePlayers()) {
			try {
				BukkitPlayer bp = (BukkitPlayer) BattlebitsAPI.getAccountCommon().getBattlePlayer(o.getUniqueId());
				if (bp == null)
					continue;
				String tag = e.getNewTag().getPrefix();
				String league = " §7(" + player.getLeague().getSymbol() + "§7)";
				if (manager.getPlugin().isOldTag()) {
					tag = tag.substring(tag.length() - 2, tag.length());
					league = "";
				}
				ScoreboardAPI.leaveTeamToPlayer(o, oldId, p);
				ScoreboardAPI.joinTeam(ScoreboardAPI.createTeamIfNotExistsToPlayer(o, id,
						tag + (ChatColor.stripColor(tag).trim().length() > 0 ? " " : ""), league), p);
				bp = null;
			} catch (Exception e2) {
			}
		}
		id = null;
		player = null;
	}

	private static char[] chars = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public static String getTeamName(Tag tag, League liga) {
		return chars[tag.ordinal()] + "-" + chars[League.values().length - liga.ordinal()];
	}

}
