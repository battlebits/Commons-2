package br.com.battlebits.commons.bungee.command.register;

import java.util.UUID;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.bungee.command.BungeeCommandArgs;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.command.CommandSender;
import br.com.battlebits.commons.core.data.DataPlayer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.punish.Ban;
import br.com.battlebits.commons.core.punish.Mute;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.core.translate.Translate;
import br.com.battlebits.commons.util.DateUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PunishCommand implements CommandClass {

	@Command(name = "ban", usage = "/<command> <player> <reason>", aliases = {
			"banir" }, groupToUse = Group.TRIAL, noPermMessageId = "command-ban-no-access", runAsync = true)
	public void ban(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final Language language = lang;
		final String banPrefix = Translate.getTranslation(lang, "command-ban-prefix") + " ";
		if (args.length < 2) {
			sender.sendMessage(TextComponent.fromLegacyText(banPrefix
					+ Translate.getTranslation(lang, "command-ban-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(
					TextComponent.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			if (sender instanceof ProxiedPlayer) {
				if (BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getServerGroup()
						.equals(Group.TRIAL)) {
					sender.sendMessage(TextComponent.fromLegacyText(
							banPrefix + Translate.getTranslation(language, "command-ban-trial-offline")));
					return;
				}
			}
			try {
				player = DataPlayer.getPlayer(uuid);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-cant-request-offline")));
				return;
			}
			if (player == null) {
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-never-joined")));
				return;
			}
		}
		if (player.getUniqueId() == sender.getUniqueId()) {
			sender.sendMessage(banPrefix + T.t(language, "command-ban-cant-ban-yourself"));
			return;
		}
		Ban actualBan = player.getPunishHistoric().getActualBan();
		if (actualBan != null && !actualBan.isUnbanned() && actualBan.isPermanent()) {
			sender.sendMessage(TextComponent
					.fromLegacyText(banPrefix + Translate.getTranslation(language, "command-ban-already-banned")));
			return;
		}
		if (player.isStaff()) {
			Group group = Group.DONO;
			if (cmdArgs.isPlayer())
				group = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId())
						.getServerGroup();
			if (group != Group.DONO && group != Group.ADMIN) {
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "command-ban-cant-staff")));
				return;
			}
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			String espaco = " ";
			if (i >= args.length - 1)
				espaco = "";
			builder.append(args[i] + espaco);
		}
		Ban ban = null;
		String playerIp = "";
		try {
			playerIp = player.getIpAddress();
		} catch (Exception ex) {
			playerIp = "OFFLINE";
		}
		if (cmdArgs.isPlayer()) {
			ProxiedPlayer bannedBy = cmdArgs.getPlayer();
			ban = new Ban(bannedBy.getName(), bannedBy.getUniqueId(), playerIp, player.getServerConnected(),
					builder.toString());
			bannedBy = null;
		} else {
			ban = new Ban("CONSOLE", playerIp, player.getServerConnected(), builder.toString());
		}
		BungeeMain.getPlugin().getBanManager().ban(player, ban);
	}

	@Command(name = "tempban", usage = "/<command> <player> <time> <reason>", aliases = {
			"tempbanir" }, groupToUse = Group.TRIAL, noPermMessageId = "command-tempban-no-access", runAsync = true)
	public void tempban(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final Language language = lang;
		final String banPrefix = Translate.getTranslation(lang, "command-tempban-prefix") + " ";
		if (args.length < 3) {
			sender.sendMessage(TextComponent.fromLegacyText(banPrefix + Translate
					.getTranslation(lang, "command-tempban-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(
					TextComponent.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			if (sender instanceof ProxiedPlayer) {
				if (BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getServerGroup()
						.equals(Group.TRIAL)) {
					sender.sendMessage(TextComponent.fromLegacyText(
							banPrefix + Translate.getTranslation(language, "command-ban-trial-offline")));
					return;
				}
			}
			try {
				player = DataPlayer.getPlayer(uuid);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-cant-request-offline")));
				return;
			}
			if (player == null) {
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-never-joined")));
				return;
			}
		}
		Ban actualBan = player.getPunishHistoric().getActualBan();
		if (actualBan != null && !actualBan.isUnbanned() && actualBan.isPermanent()) {
			sender.sendMessage(TextComponent
					.fromLegacyText(banPrefix + Translate.getTranslation(language, "command-ban-already-banned")));
			return;
		}
		if (player.isStaff()) {
			Group group = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId())
					.getServerGroup();
			if (group != Group.DONO && group != Group.ADMIN) {
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "command-ban-cant-staff")));
				return;
			}
		}
		long expiresCheck;
		try {
			expiresCheck = DateUtils.parseDateDiff(args[1], true);
		} catch (Exception e1) {
			sender.sendMessage(
					TextComponent.fromLegacyText(banPrefix + Translate.getTranslation(language, "invalid-format")));
			return;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			String espaco = " ";
			if (i >= args.length - 1)
				espaco = "";
			builder.append(args[i] + espaco);
		}
		Ban ban = null;
		String playerIp = "";
		try {
			playerIp = player.getIpAddress();
		} catch (Exception ex) {
			playerIp = "OFFLINE";
		}
		if (cmdArgs.isPlayer()) {
			ProxiedPlayer bannedBy = cmdArgs.getPlayer();
			ban = new Ban(bannedBy.getName(), bannedBy.getUniqueId(), playerIp, player.getServerConnected(),
					builder.toString(), expiresCheck);
			bannedBy = null;
		} else {
			ban = new Ban("CONSOLE", playerIp, player.getServerConnected(), builder.toString(), expiresCheck);
		}
		BungeeMain.getPlugin().getBanManager().ban(player, ban);
	}

	@Command(name = "unban", usage = "/<command> <player>", aliases = {
			"desbanir" }, groupToUse = Group.MANAGER, noPermMessageId = "command-unban-no-access", runAsync = true)
	public void unban(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final Language language = lang;
		final String banPrefix = Translate.getTranslation(lang, "command-unban-prefix") + " ";
		if (args.length != 1) {
			sender.sendMessage(TextComponent.fromLegacyText(banPrefix
					+ Translate.getTranslation(lang, "command-unban-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(
					TextComponent.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			try {
				player = DataPlayer.getPlayer(uuid);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-cant-request-offline")));
				return;
			}
			if (player == null) {
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-never-joined")));
				return;
			}
		}
		Ban actualBan = player.getPunishHistoric().getActualBan();
		if (actualBan == null) {
			sender.sendMessage(
					TextComponent.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-not-banned")));
			return;
		}
		BattlePlayer unbannedBy = null;
		if (cmdArgs.isPlayer()) {
			unbannedBy = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId());
		}
		BungeeMain.getPlugin().getBanManager().unban(unbannedBy, player, actualBan);
	}

	@Command(name = "mute", usage = "/<command> <player> <reason>", aliases = {
			"mutar" }, groupToUse = Group.HELPER, noPermMessageId = "command-mute-no-access", runAsync = true)
	public void mute(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		final Language language = lang;
		final String banPrefix = Translate.getTranslation(lang, "command-mute-prefix") + " ";
		if (args.length < 2) {
			sender.sendMessage(TextComponent.fromLegacyText(banPrefix
					+ Translate.getTranslation(lang, "command-mute-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(
					TextComponent.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			if (sender instanceof ProxiedPlayer) {
				if (BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getServerGroup()
						.equals(Group.TRIAL)) {
					sender.sendMessage(TextComponent.fromLegacyText(
							banPrefix + Translate.getTranslation(language, "command-mute-trial-offline")));
					return;
				}
			}
			try {
				player = DataPlayer.getPlayer(uuid);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-cant-request-offline")));
				return;
			}
			if (player == null) {
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-never-joined")));
				return;
			}
		}
		Mute actualMute = player.getPunishHistoric().getActualMute();
		if (actualMute != null && !actualMute.isUnmuted() && actualMute.isPermanent()) {
			sender.sendMessage(TextComponent
					.fromLegacyText(banPrefix + Translate.getTranslation(language, "command-mute-already-muted")));
			return;
		}
		if (player.isStaff()) {
			Group group = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId())
					.getServerGroup();
			if (group != Group.DONO && group != Group.ADMIN) {
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "command-mute-cant-staff")));
				return;
			}
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			String espaco = " ";
			if (i >= args.length - 1)
				espaco = "";
			builder.append(args[i] + espaco);
		}
		Mute mute = null;
		String playerIp = "";
		try {
			playerIp = player.getIpAddress();
		} catch (Exception ex) {
			playerIp = "OFFLINE";
		}
		if (cmdArgs.isPlayer()) {
			ProxiedPlayer bannedBy = cmdArgs.getPlayer();
			mute = new Mute(bannedBy.getName(), bannedBy.getUniqueId(), playerIp, player.getServerConnected(),
					builder.toString());
			bannedBy = null;
		} else {
			mute = new Mute("CONSOLE", playerIp, player.getServerConnected(), builder.toString());
		}
		BungeeMain.getPlugin().getBanManager().mute(player, mute);
	}

	@Command(name = "tempmute", usage = "/<command> <player> <time> <reason>", aliases = {
			"tempmutar" }, groupToUse = Group.HELPER, noPermMessageId = "command-tempmute-no-access", runAsync = true)
	public void tempmute(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		final Language language = lang;
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final String banPrefix = Translate.getTranslation(lang, "command-tempmute-prefix") + " ";
		if (args.length < 3) {
			sender.sendMessage(TextComponent.fromLegacyText(banPrefix + Translate
					.getTranslation(lang, "command-tempmute-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(
					TextComponent.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			if (sender instanceof ProxiedPlayer) {
				if (BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getServerGroup()
						.equals(Group.TRIAL)) {
					sender.sendMessage(TextComponent.fromLegacyText(
							banPrefix + Translate.getTranslation(language, "command-mute-trial-offline")));
					return;
				}
			}
			try {
				player = DataPlayer.getPlayer(uuid);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-cant-request-offline")));
				return;
			}
			if (player == null) {
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-never-joined")));
				return;
			}
		}
		Mute actualMute = player.getPunishHistoric().getActualMute();
		if (actualMute != null && !actualMute.isUnmuted() && actualMute.isPermanent()) {
			sender.sendMessage(TextComponent
					.fromLegacyText(banPrefix + Translate.getTranslation(language, "command-mute-already-muted")));
			return;
		}
		if (player.isStaff()) {
			Group group = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId())
					.getServerGroup();
			if (group != Group.DONO && group != Group.ADMIN) {
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "command-mute-cant-staff")));
				return;
			}
		}
		long expiresCheck;
		try {
			expiresCheck = DateUtils.parseDateDiff(args[1], true);
		} catch (Exception e1) {
			sender.sendMessage(
					TextComponent.fromLegacyText(banPrefix + Translate.getTranslation(language, "invalid-format")));
			return;
		}
		StringBuilder builder = new StringBuilder();
		for (int i = 2; i < args.length; i++) {
			String espaco = " ";
			if (i >= args.length - 1)
				espaco = "";
			builder.append(args[i] + espaco);
		}
		Mute mute = null;
		String playerIp = "";
		try {
			playerIp = player.getIpAddress();
		} catch (Exception ex) {
			playerIp = "OFFLINE";
		}
		if (cmdArgs.isPlayer()) {
			ProxiedPlayer bannedBy = cmdArgs.getPlayer();
			mute = new Mute(bannedBy.getName(), bannedBy.getUniqueId(), playerIp, player.getServerConnected(),
					builder.toString(), expiresCheck);
			bannedBy = null;
		} else {
			mute = new Mute("CONSOLE", playerIp, player.getServerConnected(), builder.toString(), expiresCheck);
		}
		BungeeMain.getPlugin().getBanManager().mute(player, mute);
	}

	@Command(name = "unmute", usage = "/<command> <player>", aliases = {
			"desmutar" }, groupToUse = Group.MANAGER, noPermMessageId = "command-unmute-no-access", runAsync = true)
	public void unmute(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final Language language = lang;
		final String banPrefix = Translate.getTranslation(lang, "command-unmute-prefix") + " ";
		if (args.length != 1) {
			sender.sendMessage(TextComponent.fromLegacyText(banPrefix
					+ Translate.getTranslation(lang, "command-unmute-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(
					TextComponent.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			try {
				player = DataPlayer.getPlayer(uuid);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-cant-request-offline")));
				return;
			}
			if (player == null) {
				sender.sendMessage(TextComponent
						.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-never-joined")));
				return;
			}
		}
		Mute actualMute = player.getPunishHistoric().getActualMute();
		if (actualMute == null) {
			sender.sendMessage(
					TextComponent.fromLegacyText(banPrefix + Translate.getTranslation(language, "player-not-muted")));
			return;
		}
		BattlePlayer unbannedBy = null;
		if (cmdArgs.isPlayer()) {
			unbannedBy = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId());
		}
		BungeeMain.getPlugin().getBanManager().unmute(unbannedBy, player, actualMute);
	}
}