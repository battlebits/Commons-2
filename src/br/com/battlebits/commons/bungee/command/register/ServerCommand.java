package br.com.battlebits.commons.bungee.command.register;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.bungee.command.BungeeCommandArgs;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.loadbalancer.server.BattleServer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.core.translate.Translate;
import br.com.battlebits.commons.core.twitter.Twitter;
import br.com.battlebits.commons.core.twitter.TwitterAccount;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import twitter4j.TwitterException;

public class ServerCommand implements CommandClass {

	@Command(name = "connect", usage = "/<command> <server>", aliases = { "server", "con" })
	public void connect(BungeeCommandArgs cmdArgs) {
		if (cmdArgs.isPlayer()) {
			ProxiedPlayer p = cmdArgs.getPlayer();

			if (cmdArgs.getArgs().length != 1) {
				p.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
						BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
						"command-connect-usage")));
				return;
			}
			String serverIp = cmdArgs.getArgs()[0];

			BattleServer server = BungeeMain.getPlugin().getServerManager().getServer(serverIp);
			if (server != null && server.getServerInfo() != null) {
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.sendMessage(
						TextComponent
								.fromLegacyText(
										Translate
												.getTranslation(
														BattlebitsAPI.getAccountCommon()
																.getBattlePlayer(p.getUniqueId()).getLanguage(),
														"server-connect-ip")
												.replace("%address%", serverIp.toLowerCase())));
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.connect(server.getServerInfo());
			} else {
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
						BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
						"server-not-exists")));
				p.sendMessage(TextComponent.fromLegacyText(""));
			}
		}
	}

	@Command(name = "hungergames", usage = "/<command>", aliases = { "hg" })
	public void hungergames(BungeeCommandArgs cmdArgs) {
		if (cmdArgs.isPlayer()) {
			ProxiedPlayer p = cmdArgs.getPlayer();
			BattleServer hg = BungeeMain.getPlugin().getServerManager().getBalancer(ServerType.HUNGERGAMES).next();
			if (hg != null && hg.getServerInfo() != null) {
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
						BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
						"server-connect-hungergames")));
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.connect(hg.getServerInfo());
			} else {
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
						BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
						"server-not-available")));
				p.sendMessage(TextComponent.fromLegacyText(""));
			}
		}
	}

	@Command(name = "doublekit", usage = "/<command>", aliases = { "dk" })
	public void doublekit(BungeeCommandArgs cmdArgs) {
		if (cmdArgs.isPlayer()) {
			ProxiedPlayer p = cmdArgs.getPlayer();
			BattleServer hg = BungeeMain.getPlugin().getServerManager().getBalancer(ServerType.DOUBLEKITHG).next();
			if (hg != null && hg.getServerInfo() != null) {
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
						BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
						"server-connect-doublekit")));
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.connect(hg.getServerInfo());
			} else {
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
						BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
						"server-not-available")));
				p.sendMessage(TextComponent.fromLegacyText(""));
			}
		}
	}

	@Command(name = "customhg", usage = "/<command>", aliases = { "custom" })
	public void customhg(BungeeCommandArgs cmdArgs) {
		if (cmdArgs.isPlayer()) {
			ProxiedPlayer p = cmdArgs.getPlayer();
			BattleServer hg = BungeeMain.getPlugin().getServerManager().getBalancer(ServerType.CUSTOMHG).next();
			if (hg != null && hg.getServerInfo() != null) {
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
						BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
						"server-connect-customhg")));
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.connect(hg.getServerInfo());
			} else {
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
						BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
						"server-not-available")));
				p.sendMessage(TextComponent.fromLegacyText(""));
			}
		}
	}

	@Command(name = "fairplay", usage = "/<command>", aliases = { "fp" })
	public void fairplay(BungeeCommandArgs cmdArgs) {
		if (cmdArgs.isPlayer()) {
			ProxiedPlayer p = cmdArgs.getPlayer();
			p.sendMessage(TextComponent.fromLegacyText(""));
			p.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
					BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
					"server-not-available")));
			p.sendMessage(TextComponent.fromLegacyText(""));
		}
	}

	@Command(name = "lobby", usage = "/<command>", aliases = { "hub" })
	public void lobby(BungeeCommandArgs cmdArgs) {
		if (cmdArgs.isPlayer()) {
			ProxiedPlayer p = cmdArgs.getPlayer();
			BattleServer lobby = BungeeMain.getPlugin().getServerManager().getBalancer(ServerType.LOBBY).next();
			if (lobby != null && lobby.getServerInfo() != null) {
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
						BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
						"server-connect-lobby")));
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.connect(lobby.getServerInfo());
			} else {
				p.sendMessage(TextComponent.fromLegacyText(""));
				p.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(
						BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
						"server-not-available")));
				p.sendMessage(TextComponent.fromLegacyText(""));
			}
		}
	}

	@Command(name = "address", usage = "/<command>", aliases = { "ip" }, permission = "bungeecord.command.ip")
	public void address(BungeeCommandArgs cmdArgs) {
		if (cmdArgs.isPlayer()) {
			ProxiedPlayer p = cmdArgs.getPlayer();
			p.sendMessage(TextComponent.fromLegacyText(Translate
					.getTranslation(BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId()).getLanguage(),
							"server-connected-ip")
					.replace("%address%", p.getServer().getInfo().getName().toUpperCase())));
		}
	}

	@Command(name = "broadcast", aliases = { "bc", "alert",
			"tweet" }, groupToUse = Group.MODPLUS, noPermMessageId = "command-broadcast-no-access")
	public void tweet(BungeeCommandArgs cmdArgs) {
		Language language = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			language = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId())
					.getLanguage();
		}
		String[] args = cmdArgs.getArgs();
		if (args.length <= 0) {
			cmdArgs.getSender().sendMessage(TextComponent.fromLegacyText(Translate
					.getTranslation(language, "command-broadcast-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			String espaco = " ";
			if (i >= args.length - 1)
				espaco = "";
			builder.append(args[i] + espaco);
		}
		BungeeMain.getPlugin().getProxy().broadcast(TextComponent.fromLegacyText(""));
		BungeeMain.getPlugin().getProxy().broadcast(TextComponent.fromLegacyText(
				Translate.getTranslation(language, "broadcast") + " " + ChatColor.WHITE + builder.toString()));
		BungeeMain.getPlugin().getProxy().broadcast(TextComponent.fromLegacyText(""));
		try {
			if (Twitter.tweet(TwitterAccount.BATTLEBITSMC, builder.toString())) {
				cmdArgs.getSender().sendMessage(TextComponent.fromLegacyText(
						T.t(language, "tweet-success", new String[] { "%message%", builder.toString() })));
			}
		} catch (TwitterException e) {
			e.printStackTrace();
		}

	}

}