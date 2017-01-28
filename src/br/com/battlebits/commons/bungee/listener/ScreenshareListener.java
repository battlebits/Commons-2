package br.com.battlebits.commons.bungee.listener;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.punish.Ban;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.translate.Translate;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ScreenshareListener implements Listener {

	@EventHandler(priority = (byte) -128)
	public void onConnectScreenShare(ServerConnectEvent event) {
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(event.getPlayer().getUniqueId());
		if (event.getTarget() == null)
			return;
		if (!event.getTarget().getName().equals("ss.battlebits.com.br"))
			return;
		if (player.hasGroupPermission(Group.MODPLUS))
			return;
		if (player.isScreensharing())
			return;
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onChangeServer(PlayerDisconnectEvent event) {
		if (event.getPlayer().getServer() == null)
			return;
		ServerInfo info = event.getPlayer().getServer().getInfo();
		if (!info.getName().equals("ss.battlebits.com.br"))
			return;
		BattlePlayer bp = BattlePlayer.getPlayer(event.getPlayer().getUniqueId());
		if (bp.isScreensharing()) {
			bp.setScreensharing(false);
			Ban ban = new Ban("CONSOLE", bp.getIpAddress(), bp.getServerConnected(), "ScreenShare leave");
			BungeeMain.getPlugin().getBanManager().ban(bp, ban);
			return;
		}
		if (BattlePlayer.getPlayer(event.getPlayer().getUniqueId()).hasGroupPermission(Group.MODPLUS)) {
			for (ProxiedPlayer player : info.getPlayers()) {
				if (player.getUniqueId() == event.getPlayer().getUniqueId())
					continue;
				if (BattlePlayer.getPlayer(player.getUniqueId()).hasGroupPermission(Group.MODPLUS)) {
					return;
				}
			}
			for (ProxiedPlayer proxied : info.getPlayers()) {
				BattlePlayer player = BattlePlayer.getPlayer(proxied.getUniqueId());
				player.setScreensharing(false);
				if (proxied.getUniqueId() == event.getPlayer().getUniqueId())
					continue;
				if (player.getLastServer().isEmpty()) {
					proxied.connect(BungeeMain.getPlugin().getServerManager().getBalancer(ServerType.LOBBY).next()
							.getServerInfo());
				} else {
					proxied.connect(BungeeMain.getPlugin().getProxy().getServerInfo(player.getLastServer()));
				}
				proxied.sendMessage(TextComponent.fromLegacyText(
						Translate.getTranslation(player.getLanguage(), "command-screenshare-prefix") + " " + Translate
								.getTranslation(player.getLanguage(), "command-screenshare-moderator-leave")));
			}
		}
	}

	@EventHandler
	public void onChangeServer(ServerKickEvent event) {
		ServerInfo info = event.getKickedFrom();
		if (!info.getName().equals("ss.battlebits.com.br"))
			return;
		BattlePlayer bp = BattlePlayer.getPlayer(event.getPlayer().getUniqueId());
		if (bp.isScreensharing()) {
			bp.setScreensharing(false);
			return;
		}
		if (BattlePlayer.getPlayer(event.getPlayer().getUniqueId()).hasGroupPermission(Group.MODPLUS)) {
			for (ProxiedPlayer player : info.getPlayers()) {
				if (player.getUniqueId() == event.getPlayer().getUniqueId())
					continue;
				if (BattlePlayer.getPlayer(player.getUniqueId()).hasGroupPermission(Group.MODPLUS)) {
					return;
				}
			}
			for (ProxiedPlayer proxied : info.getPlayers()) {
				BattlePlayer player = BattlePlayer.getPlayer(proxied.getUniqueId());
				player.setScreensharing(false);
				if (proxied.getUniqueId() == event.getPlayer().getUniqueId())
					continue;
				if (player.getLastServer().isEmpty()) {
					proxied.connect(BungeeMain.getPlugin().getServerManager().getBalancer(ServerType.LOBBY).next()
							.getServerInfo());
				} else {
					proxied.connect(BungeeMain.getPlugin().getProxy().getServerInfo(player.getLastServer()));
				}
				proxied.sendMessage(TextComponent.fromLegacyText(
						Translate.getTranslation(player.getLanguage(), "command-screenshare-prefix") + " " + Translate
								.getTranslation(player.getLanguage(), "command-screenshare-moderator-leave")));
			}
		}
	}

	@EventHandler
	public void onChangeServer(ServerConnectEvent event) {
		if (event.getPlayer().getServer() == null)
			return;
		ServerInfo info = event.getPlayer().getServer().getInfo();
		if (!info.getName().equals("ss.battlebits.com.br"))
			return;
		BattlePlayer bp = BattlePlayer.getPlayer(event.getPlayer().getUniqueId());
		if (bp.isScreensharing()) {
			bp.setScreensharing(false);
			Ban ban = new Ban("CONSOLE", bp.getIpAddress(), bp.getServerConnected(), "ScreenShare leave");
			BungeeMain.getPlugin().getBanManager().ban(bp, ban);
			return;
		}
		if (BattlePlayer.getPlayer(event.getPlayer().getUniqueId()).hasGroupPermission(Group.MODPLUS)) {
			for (ProxiedPlayer player : info.getPlayers()) {
				if (player.getUniqueId() == event.getPlayer().getUniqueId())
					continue;
				if (BattlePlayer.getPlayer(player.getUniqueId()).hasGroupPermission(Group.MODPLUS)) {
					return;
				}
			}
			for (ProxiedPlayer proxied : info.getPlayers()) {
				BattlePlayer player = BattlePlayer.getPlayer(proxied.getUniqueId());
				player.setScreensharing(false);
				if (proxied.getUniqueId() == event.getPlayer().getUniqueId())
					continue;
				if (player.getLastServer().isEmpty()) {
					proxied.connect(BungeeMain.getPlugin().getServerManager().getBalancer(ServerType.LOBBY).next()
							.getServerInfo());
				} else {
					proxied.connect(BungeeMain.getPlugin().getProxy().getServerInfo(player.getLastServer()));
				}
				proxied.sendMessage(TextComponent.fromLegacyText(
						Translate.getTranslation(player.getLanguage(), "command-screenshare-prefix") + " " + Translate
								.getTranslation(player.getLanguage(), "command-screenshare-moderator-leave")));
			}
		}
	}

}
