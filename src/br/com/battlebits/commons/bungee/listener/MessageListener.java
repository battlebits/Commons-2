package br.com.battlebits.commons.bungee.listener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.punish.Ban;
import br.com.battlebits.commons.core.server.ServerManager;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.server.loadbalancer.server.BattleServer;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.core.translate.Translate;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MessageListener implements Listener {
	private ServerManager manager;

	public MessageListener(ServerManager manager) {
		this.manager = manager;
	}

	@EventHandler
	public void onPluginMessageBungeeCordChannel(PluginMessageEvent event) {
		if (!event.getTag().equals("BungeeCord"))
			return;
		if (!(event.getSender() instanceof Server))
			return;
		if (!(event.getReceiver() instanceof ProxiedPlayer))
			return;
		ProxiedPlayer proxiedPlayer = (ProxiedPlayer) event.getReceiver();
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedPlayer.getUniqueId());
		ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
		String subChannel = in.readUTF();
		switch (subChannel) {
		case "Hungergames": {
			event.setCancelled(true);
			BattleServer server = manager.getBalancer(ServerType.HUNGERGAMES).next();
			if (server != null && server.getServerInfo() != null) {
				if (!server.isFull() || (server.isFull() && player.hasGroupPermission(Group.ULTIMATE))) {
					proxiedPlayer.connect(server.getServerInfo());
					break;
				}
			}
			proxiedPlayer.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
					BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedPlayer.getUniqueId()).getLanguage(),
					"server-not-available")));
			break;
		}
		case "CustomHungergames": {
			event.setCancelled(true);
			BattleServer server = manager.getBalancer(ServerType.CUSTOMHG).next();
			if (server != null && server.getServerInfo() != null) {
				if (!server.isFull() || (server.isFull() && player.hasGroupPermission(Group.ULTIMATE))) {
					proxiedPlayer.connect(server.getServerInfo());
					break;
				}
			}
			proxiedPlayer.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
					BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedPlayer.getUniqueId()).getLanguage(),
					"server-not-available")));
			break;
		}
		case "DoubleKitHungergames": {
			event.setCancelled(true);
			BattleServer server = manager.getBalancer(ServerType.DOUBLEKITHG).next();
			if (server != null && server.getServerInfo() != null) {
				if (!server.isFull() || (server.isFull() && player.hasGroupPermission(Group.ULTIMATE))) {
					proxiedPlayer.connect(server.getServerInfo());
					break;
				}
			}
			proxiedPlayer.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
					BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedPlayer.getUniqueId()).getLanguage(),
					"server-not-available")));
			break;
		}
		case "Fairplayhg": {
			event.setCancelled(true);
			proxiedPlayer.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
					BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedPlayer.getUniqueId()).getLanguage(),
					"server-not-available")));
			break;
		}

		case "PVPFulliron": {
			event.setCancelled(true);
			BattleServer server = manager.getBalancer(ServerType.PVP_FULLIRON).next();
			if (server != null && server.getServerInfo() != null) {
				if (!server.isFull() || (server.isFull() && player.hasGroupPermission(Group.ULTIMATE))) {
					proxiedPlayer.connect(server.getServerInfo());
					break;
				}
			}
			proxiedPlayer.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
					BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedPlayer.getUniqueId()).getLanguage(),
					"server-not-available")));
			break;
		}

		case "PVPSimulator": {
			event.setCancelled(true);
			BattleServer server = manager.getBalancer(ServerType.PVP_SIMULATOR).next();
			if (server != null && server.getServerInfo() != null) {
				if (!server.isFull() || (server.isFull() && player.hasGroupPermission(Group.ULTIMATE))) {
					proxiedPlayer.connect(server.getServerInfo());
					break;
				}
			}
			proxiedPlayer.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
					BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedPlayer.getUniqueId()).getLanguage(),
					"server-not-available")));
			break;
		}

		case "Lobby": {
			event.setCancelled(true);
			BattleServer server = manager.getBalancer(ServerType.LOBBY).next();
			if (server != null && server.getServerInfo() != null) {
				if (!server.isFull() || (server.isFull() && player.hasGroupPermission(Group.ULTIMATE))) {
					proxiedPlayer.connect(server.getServerInfo());
					break;
				}
			}
			proxiedPlayer.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
					BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedPlayer.getUniqueId()).getLanguage(),
					"server-not-available")));
			break;
		}
		default:
			break;
		}
	}

	@EventHandler
	public void onAnticheatMessage(PluginMessageEvent event) {
		if (!event.getTag().equals("BungeeCord"))
			return;
		if (!(event.getSender() instanceof Server))
			return;
		if (!(event.getReceiver() instanceof ProxiedPlayer))
			return;
		ProxiedPlayer proxiedPlayer = (ProxiedPlayer) event.getReceiver();
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxiedPlayer.getUniqueId());
		ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
		String subChannel = in.readUTF();
		switch (subChannel) {
		case "AnticheatAlert": {
			event.setCancelled(true);
			int count = in.readInt();
			int total = in.readInt();
			int ping = in.readInt();
			String hackType = in.readUTF();
			for (ProxiedPlayer online : BungeeMain.getPlugin().getProxy().getPlayers()) {
				BattlePlayer pl = BattlebitsAPI.getAccountCommon().getBattlePlayer(online.getUniqueId());
				if (pl.hasGroupPermission(Group.TRIAL) && pl.getConfiguration().isAlertsEnabled()) {
					TextComponent message = new TextComponent(T.t(pl.getLanguage(), "anticheat-alert-prefix") + " "
							+ T.t(pl.getLanguage(), "anticheat-alert-player",
									new String[] { "%player%", "%count%", "%total%", "%ping%", "%hacktype%" },
									new String[] { player.getName(), "" + count, "" + total, "" + ping, hackType }));
					message.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/teleport " + player.getName()));
					message.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
							new TextComponent[] { new TextComponent(T.t(pl.getLanguage(), "click-to-teleport")) }));
					online.sendMessage(message);
				}
			}
			break;
		}
		case "AnticheatBanAlert": {
			event.setCancelled(true);
			int ping = in.readInt();
			String hackType = in.readUTF();
			for (ProxiedPlayer online : BungeeMain.getPlugin().getProxy().getPlayers()) {
				BattlePlayer pl = BattlebitsAPI.getAccountCommon().getBattlePlayer(online.getUniqueId());
				if (pl.hasGroupPermission(Group.TRIAL) && pl.getConfiguration().isAlertsEnabled()) {
					TextComponent message = new TextComponent(T.t(pl.getLanguage(), "anticheat-alert-prefix") + " "
							+ T.t(pl.getLanguage(), "anticheat-alert-ban",
									new String[] { "%player%", "%ping%", "%hacktype%" },
									new String[] { player.getName(), "" + ping, hackType }));
					message.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/teleport " + player.getName()));
					message.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
							new TextComponent[] { new TextComponent(T.t(pl.getLanguage(), "click-to-teleport")) }));
					online.sendMessage(message);
				}
			}
			break;
		}
		case "AnticheatBan": {
			event.setCancelled(true);
			String banReason = in.readUTF();
			int time = 30;
			for (Ban ban : player.getPunishHistoric().getBanHistory()) {
				if (ban.isPermanent())
					continue;
				if (!ban.getBannedBy().equals("CONSOLE"))
					continue;
				if (System.currentTimeMillis() - ban.getBanTime() > 604800000)
					continue;
				time *= 2;
			}
			BungeeMain.getPlugin().getProxy().getPluginManager().dispatchCommand(BungeeMain.getPlugin().getProxy().getConsole(), "tempban " + player.getUniqueId().toString() + " " + time + "m " + banReason);
			break;
		}
		default:
			break;
		}
	}
}
