package br.com.battlebits.commons.bukkit.listener;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.admin.AdminMode;
import br.com.battlebits.commons.api.vanish.VanishAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.event.account.PlayerChangeLeagueEvent;
import br.com.battlebits.commons.bukkit.event.account.PlayerLanguageEvent;
import br.com.battlebits.commons.bukkit.event.vanish.PlayerShowToPlayerEvent;
import br.com.battlebits.commons.core.data.DataServer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.T;

public class PlayerListener implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
	public void onPreProcessCommand(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().toLowerCase().startsWith("/me ")) {
			event.setCancelled(true);
		}
		if (event.getMessage().split(" ")[0].contains(":")) {
			event.getPlayer().sendMessage("§%cant-type-two-dot-command%§");
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onWhitelist(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().toLowerCase().startsWith("/whitelist ")) {
			if (event.getPlayer().hasPermission("minecraft.command.whitelist")
					|| event.getPlayer().hasPermission("bukkit.command.whitelist")) {
				new BukkitRunnable() {
					@Override
					public void run() {
						DataServer.setJoinEnabled(!Bukkit.hasWhitelist());
					}
				}.runTaskLaterAsynchronously(BukkitMain.getInstance(), 2);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent event) {
		if (BattlebitsAPI.isChristmas())
			new BukkitRunnable() {
				@Override
				public void run() {
					event.getPlayer().sendMessage("§%merry-christmas%§");
				}
			}.runTaskLater(BukkitMain.getInstance(), 2);			
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoinMonitor(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		VanishAPI.getInstance().updateVanishToPlayer(player);
		for (Player online : Bukkit.getOnlinePlayers()) {
			if (online.getUniqueId().equals(player.getUniqueId()))
				continue;
			PlayerShowToPlayerEvent eventCall = new PlayerShowToPlayerEvent(player, online);
			Bukkit.getPluginManager().callEvent(eventCall);
			if (eventCall.isCancelled()) {
				if (online.canSee(player))
					online.hidePlayer(player);
			} else if (!online.canSee(player))
				online.showPlayer(player);
		}
	}

	@EventHandler
	public void onLanguage(PlayerLanguageEvent event) {
		Player p = event.getPlayer();
		Scoreboard board = p.getScoreboard();
		p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
		p.setScoreboard(board);
		p.updateInventory();
	}

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		if (event.getResult() == Result.KICK_WHITELIST) {
			if (BattlebitsAPI.getAccountCommon().getBattlePlayer(event.getPlayer().getUniqueId()) == null)
				event.disallow(Result.KICK_OTHER, ChatColor.RED + "ERROR");
			if (BattlebitsAPI.getAccountCommon().getBattlePlayer(event.getPlayer().getUniqueId())
					.hasGroupPermission(Group.MODPLUS)) {
				event.allow();
			}
		}
	}

	@EventHandler(priority=EventPriority.MONITOR)
	public void onQuit(PlayerQuitEvent event) {
		AdminMode.getInstance().removeAdmin(event.getPlayer());
		VanishAPI.getInstance().removeVanish(event.getPlayer());
	}

	@EventHandler
	public void onPlayerChangeLeague(PlayerChangeLeagueEvent event) {
		if (event.getPlayer() != null && event.getNewLeague().ordinal() > event.getOldLeague().ordinal()) {
			HashMap<String, String> replaces = new HashMap<>();
			replaces.put("%league%", event.getNewLeague().toString());
			replaces.put("%symbol%", event.getNewLeague().getSymbol());
			event.getPlayer().sendMessage("§%league-prefix%§ " + T.t(event.getBukkitPlayer().getLanguage(), "league-rank-level-up", replaces));
		}
	}
}