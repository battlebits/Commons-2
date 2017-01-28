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
import br.com.battlebits.commons.core.command.CommandArgs;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.command.CommandFramework.Completer;
import br.com.battlebits.commons.core.command.CommandSender;
import br.com.battlebits.commons.core.data.DataPlayer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.Translate;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class GroupCommand implements CommandClass {

	@Command(name = "groupset", usage = "/<command> <player> <group>", groupToUse = Group.MANAGER, aliases = {
			"setargrupo" }, noPermMessageId = "command-groupset-no-access", runAsync = true)
	public void groupset(BungeeCommandArgs cmdArgs) {
		final CommandSender sender = cmdArgs.getSender();
		final String[] args = cmdArgs.getArgs();
		Language lang = BattlebitsAPI.getDefaultLanguage();
		if (cmdArgs.isPlayer()) {
			lang = BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId()).getLanguage();
		}
		final Language language = lang;
		final String groupSetPrefix = Translate.getTranslation(lang, "command-groupset-prefix") + " ";
		if (args.length != 2) {
			sender.sendMessage(TextComponent
					.fromLegacyText(groupSetPrefix + Translate.getTranslation(lang, "command-groupset-usage")));
			return;
		}
		Group grupo = null;
		try {
			grupo = Group.valueOf(args[1].toUpperCase());
		} catch (Exception e) {
			sender.sendMessage(TextComponent.fromLegacyText(
					groupSetPrefix + Translate.getTranslation(lang, "command-groupset-group-not-exist")));
			return;
		}
		final Group group = grupo;
		boolean owner = false;
		ServerType type = ServerType.NONE;
		if (cmdArgs.isPlayer()) {
			BattlePlayer battleSender = BattlebitsAPI.getAccountCommon()
					.getBattlePlayer(cmdArgs.getPlayer().getUniqueId());
			type = battleSender.getServerConnectedType();
			if (battleSender.getServerGroup() == Group.DONO)
				owner = true;
		} else {
			owner = true;
			type = ServerType.NETWORK;
		}
		if (group.ordinal() <= Group.YOUTUBER.ordinal() && group != Group.NORMAL) {
			sender.sendMessage(TextComponent.fromLegacyText(
					groupSetPrefix + Translate.getTranslation(lang, "command-groupset-group-temporary")));
			return;
		}

		if (!owner)
			if (group.ordinal() > Group.MODPLUS.ordinal()) {
				sender.sendMessage(TextComponent.fromLegacyText(
						groupSetPrefix + Translate.getTranslation(lang, "command-groupset-group-not-owner")));
				return;
			}
		final ServerType typep = type;
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
		ServerType serverType = typep;
		if (group.ordinal() >= Group.MODPLUS.ordinal())
			serverType = ServerType.NETWORK;
		if (group == Group.NORMAL && player.getServerGroup().ordinal() >= Group.MODPLUS.ordinal())
			serverType = ServerType.NETWORK;
		Group actualGroup = player.getGroups().containsKey(serverType.getStaffType())
				? player.getGroups().get(serverType.getStaffType()) : Group.NORMAL;
		if (actualGroup == group) {
			sender.sendMessage(TextComponent.fromLegacyText(
					groupSetPrefix + Translate.getTranslation(language, "command-groupset-player-already-group")));
			return;
		}

		if (group == Group.NORMAL) {
			if (serverType != ServerType.NETWORK)
				if (player.getGroups().containsKey(ServerType.NETWORK.getStaffType()) && cmdArgs.isPlayer()
						&& BattlebitsAPI.getAccountCommon().getBattlePlayer(cmdArgs.getPlayer().getUniqueId())
								.isOnline())
					serverType = ServerType.NETWORK;
			player.getGroups().remove(serverType.getStaffType());
		} else {
			player.getGroups().put(serverType.getStaffType(), group);
		}
		player.saveGroups();
		ProxiedPlayer pPlayer = BungeeMain.getPlugin().getProxy().getPlayer(player.getUniqueId());
		if (pPlayer != null) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Groupset");
			out.writeUTF(group.toString());
			out.writeUTF(serverType.toString());
			if (pPlayer.getServer() != null)
				pPlayer.getServer().sendData(BattlebitsAPI.getBungeeChannel(), out.toByteArray());
		}
		String message = groupSetPrefix + Translate.getTranslation(language, "command-groupset-change-group");
		message = message.replace("%player%",
				player.getName() + "(" + player.getUniqueId().toString().replace("-", "") + ")");
		message = message.replace("%group%", group.name());
		message = message.replace("%serverType%", serverType != null ? serverType.toString() : "null");
		sender.sendMessage(TextComponent.fromLegacyText(message));
	}

	@Completer(name = "groupset", aliases = { "setargrupo", "removevip", "removervip" })
	public List<String> groupsetCompleter(CommandArgs args) {
		if (args.isPlayer()) {
			if (args.getArgs().length == 1) {
				ArrayList<String> players = new ArrayList<>();
				for (ProxiedPlayer p : BungeeMain.getPlugin().getProxy().getPlayers()) {
					if (p.getName().toLowerCase().startsWith(args.getArgs()[0].toLowerCase())) {
						players.add(p.getName());
					}
				}
				return players;
			} else if (args.getArgs().length == 2) {
				ArrayList<String> grupos = new ArrayList<>();
				for (Group group : Group.values()) {
					if (group.toString().toLowerCase().startsWith(args.getArgs()[1].toLowerCase())) {
						grupos.add(group.toString());
					}
				}
				return grupos;
			}
		}
		return new ArrayList<>();
	}

}
