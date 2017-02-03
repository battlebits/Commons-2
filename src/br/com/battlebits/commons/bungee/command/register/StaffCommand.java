package br.com.battlebits.commons.bungee.command.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.bungee.command.BungeeCommandArgs;
import br.com.battlebits.commons.bungee.command.BungeeCommandSender;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.account.Tag;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.Translate;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class StaffCommand implements CommandClass {

	@Command(name = "finder", usage = "/<command> [player]", groupToUse = Group.MOD, noPermMessageId = "command-finder-no-access", runAsync = true)
	public void finder(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = ((BungeeCommandSender) cmdArgs.getSender()).getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final Language language = lang;
		final String finderPrefix = Translate.getTranslation(lang, "command-finder-prefix") + " ";
		if (args.length != 1) {
			sender.sendMessage(TextComponent.fromLegacyText(finderPrefix
					+ Translate.getTranslation(lang, "command-finder-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(TextComponent
					.fromLegacyText(finderPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		ProxiedPlayer proxied = BungeeMain.getPlugin().getProxy().getPlayer(uuid);
		if (proxied == null) {
			sender.sendMessage(TextComponent
					.fromLegacyText(finderPrefix + Translate.getTranslation(language, "player-not-found")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player.getServerGroup().ordinal() >= Group.MANAGER.ordinal()) {
			sender.sendMessage(TextComponent.fromLegacyText(
					finderPrefix + Translate.getTranslation(language, "command-finder-player-not-allowed")));
			return;
		}
		String tag = Tag.valueOf(player.getServerGroup().toString()).getPrefix();
		String format = tag + (ChatColor.stripColor(tag).trim().length() > 0 ? " " : "") + player.getName();

		TextComponent playerMessage = new TextComponent(format);
		TextComponent space = new TextComponent(ChatColor.WHITE + " - ");
		TextComponent ip = new TextComponent(ChatColor.BLUE + player.getServerConnected());
		ip.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/connect " + player.getServerConnected()));
		ip.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
				new TextComponent[] { new TextComponent(Translate.getTranslation(language, "command-finder-hover")) }));
		sender.sendMessage(playerMessage, space, ip);
	}

	@Command(name = "staffsee", aliases = "stafflist", usage = "/<command>", groupToUse = Group.TRIAL, noPermMessageId = "command-staffsee-no-access")
	public void staffsee(BungeeCommandArgs cmdArgs) {
		CommandSender sender = ((BungeeCommandSender) cmdArgs.getSender()).getSender();
		Group originalGroup = Group.DONO;
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			originalGroup = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId())
					.getServerGroup();
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		HashMap<Group, ArrayList<UUID>> groups = new HashMap<>();
		for (ProxiedPlayer player : BungeeMain.getPlugin().getProxy().getPlayers()) {
			BattlePlayer bP = BattlebitsAPI.getAccountCommon().getBattlePlayer(player.getUniqueId());
			if (bP.getServerGroup().ordinal() < Group.BUILDER.ordinal()) {
				continue;
			}
			ArrayList<UUID> players = null;
			if (groups.containsKey(bP.getServerGroup()))
				players = groups.get(bP.getServerGroup());
			else
				players = new ArrayList<>();
			players.add(player.getUniqueId());
			groups.put(bP.getServerGroup(), players);
		}
		for (Group group : Group.values()) {
			if (!groups.containsKey(group))
				continue;
			if (group.ordinal() >= Group.MANAGER.ordinal())
				if (originalGroup.ordinal() < Group.MANAGER.ordinal())
					continue;
			for (UUID uuid : groups.get(group)) {
				BattlePlayer bP = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
				String tag = Tag.valueOf(bP.getServerGroup().toString()).getPrefix();
				String format = tag + (ChatColor.stripColor(tag).trim().length() > 0 ? " " : "") + bP.getName();
				TextComponent playerMessage = new TextComponent(format);
				TextComponent space = new TextComponent(ChatColor.WHITE + " - ");
				TextComponent ip = new TextComponent(ChatColor.BLUE + bP.getServerConnected());
				ip.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/connect " + bP.getServerConnected()));
				ip.setHoverEvent(
						new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new TextComponent[] {
								new TextComponent(Translate.getTranslation(lang, "command-finder-hover")) }));
				sender.sendMessage(playerMessage, space, ip);
			}
		}
	}

	@Command(name = "staffchat", aliases = {
			"sc" }, groupToUse = Group.STAFF, noPermMessageId = "command-staffchat-no-access")
	public void staffchat(BungeeCommandArgs args) {
		if (!args.isPlayer()) {
			args.getSender().sendMessage(TextComponent.fromLegacyText("COMANDO APENAS PARA PLAYERS"));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(args.getPlayer().getUniqueId());
		boolean active = player.getConfiguration().isStaffChatEnabled();
		player.getConfiguration().setStaffChatEnabled(!active);
		args.getSender().sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(player.getLanguage(),
				"command-staffchat-" + (active ? "disabled" : "enabled"))));
	}

	@Command(name = "screenshare", aliases = {
			"ss" }, groupToUse = Group.MODPLUS, noPermMessageId = "command-screeshare-no-access")
	public void screeshare(BungeeCommandArgs cmdArgs) {
		if (!cmdArgs.isPlayer()) {
			cmdArgs.getSender().sendMessage(TextComponent.fromLegacyText("COMANDO APENAS PARA PLAYERS"));
			return;
		}
		CommandSender sender = ((BungeeCommandSender) cmdArgs.getSender()).getSender();
		String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		String ssPrefix = Translate.getTranslation(lang, "command-screenshare-prefix") + " ";
		if (args.length < 1) {
			sender.sendMessage(TextComponent.fromLegacyText(ssPrefix + Translate
					.getTranslation(lang, "command-screenshare-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		ProxiedPlayer proxied = BungeeMain.getPlugin().getProxy().getPlayer(args[0]);
		if (proxied == null) {
			sender.sendMessage(
					TextComponent.fromLegacyText(ssPrefix + Translate.getTranslation(lang, "player-not-online")));
			return;
		}
		if (proxied.getUniqueId().equals(cmdArgs.getPlayer().getUniqueId())) {
			sender.sendMessage(
					TextComponent.fromLegacyText(ssPrefix + Translate.getTranslation(lang, "cant-yourself")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxied.getUniqueId());
		if (player.isScreensharing()) {
			player.setScreensharing(false);
			cmdArgs.getPlayer().sendMessage(TextComponent.fromLegacyText(
					ssPrefix + Translate.getTranslation(player.getLanguage(), "command-screenshare-finished")));
			proxied.sendMessage(TextComponent.fromLegacyText(
					ssPrefix + Translate.getTranslation(player.getLanguage(), "command-screenshare-finished")));
			if (player.getLastServer().isEmpty()) {
				proxied.connect(
						BungeeMain.getPlugin().getServerManager().getBalancer(ServerType.LOBBY).next().getServerInfo());
			} else {
				proxied.connect(BungeeMain.getPlugin().getProxy().getServerInfo(player.getLastServer()));
			}
			return;
		}
		ServerInfo server = BungeeMain.getPlugin().getProxy().getServerInfo("ss.battlebits.com.br");

		if (server == null) {
			cmdArgs.getPlayer()
					.sendMessage(TextComponent.fromLegacyText(ssPrefix
							+ Translate.getTranslation(BattlePlayer.getLanguage(cmdArgs.getPlayer().getUniqueId()),
									"command-screenshare-server-not-online")));
			return;
		}

		player.setScreensharing(true);

		proxied.sendMessage(TextComponent.fromLegacyText(
				ssPrefix + Translate.getTranslation(player.getLanguage(), "command-screenshare-started")));
		cmdArgs.getPlayer().sendMessage(TextComponent.fromLegacyText(ssPrefix + Translate.getTranslation(
				BattlePlayer.getLanguage(cmdArgs.getPlayer().getUniqueId()), "command-screenshare-moderator")));

		cmdArgs.getPlayer().connect(server);
		proxied.connect(server);
	}

}
