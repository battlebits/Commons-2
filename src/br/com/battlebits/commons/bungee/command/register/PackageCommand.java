package br.com.battlebits.commons.bungee.command.register;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.bungee.command.BungeeCommandArgs;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.account.RankType;
import br.com.battlebits.commons.core.account.Tag;
import br.com.battlebits.commons.core.command.CommandArgs;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.command.CommandFramework.Completer;
import br.com.battlebits.commons.core.command.CommandSender;
import br.com.battlebits.commons.core.data.DataPlayer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.Translate;
import br.com.battlebits.commons.util.DateUtils;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PackageCommand implements CommandClass {

	@Command(name = "givevip", usage = "/<command> <player> <tempo> <group>", groupToUse = Group.DONO, aliases = {
			"darvip" }, noPermMessageId = "command-givevip-no-access", runAsync = true)
	public void givevip(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final Language language = lang;
		final String giveVipPrefix = Translate.getTranslation(lang, "command-givevip-prefix") + " ";
		if (args.length != 3) {
			sender.sendMessage(TextComponent.fromLegacyText(giveVipPrefix + Translate
					.getTranslation(lang, "command-givevip-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(TextComponent
					.fromLegacyText(giveVipPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			try {
				player = DataPlayer.getPlayer(uuid);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(TextComponent
						.fromLegacyText(giveVipPrefix + Translate.getTranslation(language, "cant-request-offline")));
				return;
			}
			if (player == null) {
				sender.sendMessage(TextComponent
						.fromLegacyText(giveVipPrefix + Translate.getTranslation(language, "player-never-joined")));
				return;
			}
		}

		long expiresCheck;
		try {
			expiresCheck = DateUtils.parseDateDiff(args[1], true);
		} catch (Exception e1) {
			sender.sendMessage(
					TextComponent.fromLegacyText(giveVipPrefix + Translate.getTranslation(language, "invalid-format")));
			return;
		}
		expiresCheck = expiresCheck - System.currentTimeMillis();
		RankType rank = null;
		try {
			rank = RankType.valueOf(args[2].toUpperCase());
		} catch (Exception e) {
			sender.sendMessage(TextComponent.fromLegacyText(
					giveVipPrefix + Translate.getTranslation(language, "command-givevip-rank-not-exist")));
			return;
		}
		long newAdd = System.currentTimeMillis();
		if (player.getRanks().containsKey(rank)) {
			newAdd = player.getRanks().get(rank);
		}
		newAdd = newAdd + expiresCheck;
		player.getRanks().put(rank, newAdd);
		player.setTag(Tag.valueOf(rank.toString()));
		player.saveRanks();
		ProxiedPlayer pPlayer = BungeeMain.getPlugin().getProxy().getPlayer(player.getUniqueId());
		if (pPlayer != null) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Givevip");
			out.writeUTF(rank.name());
			out.writeLong(expiresCheck);
			if (pPlayer.getServer() != null)
				pPlayer.getServer().sendData(BattlebitsAPI.getBungeeChannel(), out.toByteArray());
		}
		String message = giveVipPrefix + Translate.getTranslation(language, "command-givevip-added");
		message = message.replace("%player%",
				player.getName() + "(" + player.getUniqueId().toString().replace("-", "") + ")");
		message = message.replace("%rank%", rank.name());
		message = message.replace("%duration%", DateUtils.formatDifference(language, expiresCheck / 1000));
		sender.sendMessage(TextComponent.fromLegacyText(message));
	}

	@Command(name = "removevip", usage = "/<command> <player> <group>", groupToUse = Group.DONO, aliases = {
			"removervip" }, noPermMessageId = "command-removevip-no-access", runAsync = true)
	public void removevip(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final Language language = lang;
		final String giveVipPrefix = Translate.getTranslation(lang, "command-removevip-prefix") + " ";
		if (args.length != 2) {
			sender.sendMessage(TextComponent.fromLegacyText(giveVipPrefix + Translate
					.getTranslation(lang, "command-removevip-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(TextComponent
					.fromLegacyText(giveVipPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			try {
				player = DataPlayer.getPlayer(uuid);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(TextComponent
						.fromLegacyText(giveVipPrefix + Translate.getTranslation(language, "cant-request-offline")));
				return;
			}
			if (player == null) {
				sender.sendMessage(TextComponent
						.fromLegacyText(giveVipPrefix + Translate.getTranslation(language, "player-never-joined")));
				return;
			}
		}

		RankType rank = null;
		try {
			rank = RankType.valueOf(args[1].toUpperCase());
		} catch (Exception e) {
			sender.sendMessage(TextComponent.fromLegacyText(
					giveVipPrefix + Translate.getTranslation(language, "command-removevip-rank-not-exist")));
			return;
		}
		player.getRanks().remove(rank);
		player.saveRanks();
		ProxiedPlayer pPlayer = BungeeMain.getPlugin().getProxy().getPlayer(player.getUniqueId());
		if (pPlayer != null) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Removevip");
			out.writeUTF(rank.name());
			if (pPlayer.getServer() != null)
				pPlayer.getServer().sendData(BattlebitsAPI.getBungeeChannel(), out.toByteArray());
		}
		String message = giveVipPrefix + Translate.getTranslation(language, "command-removevip-removed");
		message = message.replace("%player%",
				player.getName() + "(" + player.getUniqueId().toString().replace("-", "") + ")");
		message = message.replace("%rank%", rank.name());
		sender.sendMessage(TextComponent.fromLegacyText(message));
	}

	@Command(name = "doublexpadd", groupToUse = Group.DONO, noPermMessageId = "command-doublexpadd-no-access", runAsync = true)
	public void doublexpadd(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final Language language = lang;
		final String groupSetPrefix = Translate.getTranslation(lang, "command-doublexpadd-prefix") + " ";
		if (args.length != 2) {
			sender.sendMessage(TextComponent.fromLegacyText(groupSetPrefix + Translate
					.getTranslation(lang, "command-doublexpadd-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		int doublexp = 0;
		try {
			doublexp = Integer.valueOf(args[1]);
		} catch (Exception e) {
			sender.sendMessage(TextComponent.fromLegacyText(groupSetPrefix + Translate
					.getTranslation(lang, "command-doublexpadd-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		final int d = doublexp;
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(TextComponent
					.fromLegacyText(groupSetPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			try {
				player = DataPlayer.getPlayer(uuid);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(TextComponent
						.fromLegacyText(groupSetPrefix + Translate.getTranslation(language, "cant-request-offline")));
				return;
			}
			if (player == null) {
				sender.sendMessage(TextComponent
						.fromLegacyText(groupSetPrefix + Translate.getTranslation(language, "player-never-joined")));
				return;
			}
		}
		player.addDoubleXpMultiplier(d);
		String message = groupSetPrefix + Translate.getTranslation(language, "command-doublexpadd-add-success");
		message = message.replace("%player%",
				player.getName() + "(" + player.getUniqueId().toString().replace("-", "") + ")");
		sender.sendMessage(TextComponent.fromLegacyText(message));
	}

	@Command(name = "torneioadd", aliases = {
			"tadd" }, groupToUse = Group.DONO, noPermMessageId = "command-torneio-no-access", runAsync = true)
	public void torneioadd(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final Language language = lang;
		final String groupSetPrefix = Translate.getTranslation(lang, "command-torneio-prefix") + " ";
		if (args.length != 1) {
			sender.sendMessage(TextComponent.fromLegacyText(groupSetPrefix + Translate
					.getTranslation(lang, "command-torneio-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(TextComponent
					.fromLegacyText(groupSetPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			try {
				player = DataPlayer.getPlayer(uuid);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(TextComponent
						.fromLegacyText(groupSetPrefix + Translate.getTranslation(language, "cant-request-offline")));
				return;
			}
			if (player == null) {
				sender.sendMessage(TextComponent
						.fromLegacyText(groupSetPrefix + Translate.getTranslation(language, "player-never-joined")));
				return;
			}
		}
		player.setTournament(BattlebitsAPI.getTournament());
		ProxiedPlayer pPlayer = BungeeMain.getPlugin().getProxy().getPlayer(player.getUniqueId());
		if (pPlayer != null) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("TorneioAdd");
			if (pPlayer.getServer() != null)
				pPlayer.getServer().sendData(BattlebitsAPI.getBungeeChannel(), out.toByteArray());
		}
		String message = groupSetPrefix + Translate.getTranslation(language, "command-torneio-add-success");
		message = message.replace("%player%",
				player.getName() + "(" + player.getUniqueId().toString().replace("-", "") + ")");
		sender.sendMessage(TextComponent.fromLegacyText(message));
	}

	@Command(name = "torneioremove", aliases = {
			"tremove" }, groupToUse = Group.DONO, noPermMessageId = "command-torneio-no-access", runAsync = true)
	public void torneioremove(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final Language language = lang;
		final String groupSetPrefix = Translate.getTranslation(lang, "command-torneio-prefix") + " ";
		if (args.length != 1) {
			sender.sendMessage(TextComponent.fromLegacyText(groupSetPrefix + Translate
					.getTranslation(lang, "command-torneio-usage").replace("%command%", cmdArgs.getLabel())));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(args[0]);
		if (uuid == null) {
			sender.sendMessage(TextComponent
					.fromLegacyText(groupSetPrefix + Translate.getTranslation(language, "player-not-exist")));
			return;
		}
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
		if (player == null) {
			try {
				player = DataPlayer.getPlayer(uuid);
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(TextComponent
						.fromLegacyText(groupSetPrefix + Translate.getTranslation(language, "cant-request-offline")));
				return;
			}
			if (player == null) {
				sender.sendMessage(TextComponent
						.fromLegacyText(groupSetPrefix + Translate.getTranslation(language, "player-never-joined")));
				return;
			}
		}
		player.setTournament(null);
		ProxiedPlayer pPlayer = BungeeMain.getPlugin().getProxy().getPlayer(player.getUniqueId());
		if (pPlayer != null) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("TorneioRemove");
			if (pPlayer.getServer() != null)
				pPlayer.getServer().sendData(BattlebitsAPI.getBungeeChannel(), out.toByteArray());
		}
		String message = groupSetPrefix + Translate.getTranslation(language, "command-torneio-remove-success");
		message = message.replace("%player%",
				player.getName() + "(" + player.getUniqueId().toString().replace("-", "") + ")");
		sender.sendMessage(TextComponent.fromLegacyText(message));
	}

	@Completer(name = "givevip", aliases = { "darvip" })
	public List<String> givevipCompleter(CommandArgs args) {
		if (args.isPlayer()) {
			if (args.getArgs().length == 1) {
				ArrayList<String> players = new ArrayList<>();
				for (ProxiedPlayer p : BungeeMain.getPlugin().getProxy().getPlayers()) {
					if (p.getName().toLowerCase().startsWith(args.getArgs()[0].toLowerCase())) {
						players.add(p.getName());
					}
				}
				return players;
			} else if (args.getArgs().length == 3) {
				ArrayList<String> grupos = new ArrayList<>();
				for (Group group : Group.values()) {
					if (group.toString().toLowerCase().startsWith(args.getArgs()[2].toLowerCase())) {
						grupos.add(group.toString());
					}
				}
				return grupos;
			}
		}
		return new ArrayList<>();
	}

}
