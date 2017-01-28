package br.com.battlebits.commons.bungee.command.register;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.bungee.command.BungeeCommandArgs;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.clan.Clan;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.command.CommandSender;
import br.com.battlebits.commons.core.data.DataClan;
import br.com.battlebits.commons.core.data.DataPlayer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.Translate;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ClanCommand implements CommandClass {

	private static int CLAN_PRICE = 5000;
	private static int CHANGE_ABB_PRICE = 3000;

	@Command(name = "clan", aliases = { "clans" }, runAsync = true)
	public void clan(BungeeCommandArgs args) {
		String clanPrefix = Translate.getTranslation(args.getLanguage(), "clan-prefix") + " ";
		if (args.getArgs().length < 1) {
			if (args.isPlayer()) {
				BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(args.getPlayer().getUniqueId());
				if (player.getClan() != null) {
					clanInfo(args.getLanguage(), args.getSender(), player.getClan(), true);
					return;
				}
			}
			args.getSender().sendMessage(
					TextComponent.fromLegacyText(Translate.getTranslation(args.getLanguage(), "clan-help")));
			return;
		}
		if (!args.isPlayer()) {
			args.getSender().sendMessage(TextComponent.fromLegacyText(clanPrefix + "VOCE NAO É UM JOGADOR"));
			return;
		}
		String subCommand = args.getArgs()[0];
		BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(args.getPlayer().getUniqueId());
		switch (subCommand.toLowerCase()) {
		case "create":
		case "criar": {
			if (player.getClan() != null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-already-have-clan")));
				return;
			}
			if (player.getLeague().ordinal() < 1) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-league-too-low")));
				return;
			}
			if (args.getArgs().length != 3) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-create-usage")));
				return;
			}
			if (player.getMoney() < CLAN_PRICE) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-dont-have-money")));
				return;
			}
			String clanName = args.getArgs()[1];
			if (clanName.length() < 3 || clanName.length() > 16) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-clanName-between-3-16")));
				return;
			}
			if (!isLegal(clanName)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-clanName-invalid-chars")));
				return;
			}
			String clanAbbreviation = args.getArgs()[2];
			if (clanAbbreviation.length() < 3 || clanAbbreviation.length() > 5) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-changeabb-between-3-5")));
				return;
			}
			if (!isLegal(clanAbbreviation)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(clanPrefix
						+ Translate.getTranslation(args.getLanguage(), "clan-clanAbbreviation-invalid-chars")));
				return;
			}
			if (DataClan.clanNameExists(clanName)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-clanName-already-exists")));
				return;
			}
			if (DataClan.clanAbbreviationExists(clanAbbreviation)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(clanPrefix
						+ Translate.getTranslation(args.getLanguage(), "clan-clanAbbreviation-already-exists")));
				return;
			}
			Clan clan = new Clan(clanName, clanAbbreviation, player);
			DataClan.saveMongoClan(clan);
			DataClan.saveRedisClan(clan);
			player.setClanUniqueId(clan.getUniqueId());
			player.removeMoney(CLAN_PRICE);
			BattlebitsAPI.getClanCommon().loadClan(clan);
			args.getSender().sendMessage(TextComponent.fromLegacyText(clanPrefix + Translate
					.getTranslation(args.getLanguage(), "clan-created").replace("%clanName%", clan.getName())));
			return;
		}
		case "join":
		case "entrar":
		case "accept":
		case "aceitar": {
			if (player.getClan() != null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-already-have-clan")));
				return;
			}
			if (args.getArgs().length != 2) {
				args.getSender().sendMessage(TextComponent
						.fromLegacyText(clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-join-usage")));
				return;
			}
			Clan clan = BattlebitsAPI.getClanCommon().getClan(args.getArgs()[1]);
			if (clan == null) {
				try {
					clan = DataClan.getClan(args.getArgs()[1]);
				} catch (Exception e) {
					args.getSender().sendMessage(TextComponent.fromLegacyText(

							clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-cant-request")));
					return;
				}
				if (clan == null) {
					args.getSender().sendMessage(TextComponent.fromLegacyText(
							clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-not-exists")));
					return;
				} else {
					DataClan.cacheRedisClan(clan.getUniqueId(), clan.getName());
				}
			}
			if (!clan.isInvited(player)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-not-invited")));
				return;
			}
			if (!player.hasGroupPermission(Group.LIGHT)) {
				if (clan.getParticipants().size() >= clan.getSlots()) {
					args.getSender()
							.sendMessage(TextComponent.fromLegacyText(clanPrefix
									+ Translate.getTranslation(args.getLanguage(), "clan-dont-have-slots-vip")
											.replace("%store%", BattlebitsAPI.STORE)));
					return;
				}
			}
			for (UUID uuid : clan.getParticipants().keySet()) {
				ProxiedPlayer proxied = BungeeMain.getPlugin().getProxy().getPlayer(uuid);
				if (proxied == null)
					continue;
				Language lang = BattlebitsAPI.getDefaultLanguage();
				BattlePlayer battlePlayer = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxied.getUniqueId());
				if (battlePlayer != null)
					lang = battlePlayer.getLanguage();
				proxied.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(lang, "clan-prefix") + " "
						+ Translate.getTranslation(lang, "clan-player-joined").replace("%player%", player.getName())));
			}
			clan.addParticipant(player);
			args.getSender().sendMessage(TextComponent.fromLegacyText(clanPrefix + Translate
					.getTranslation(args.getLanguage(), "clan-you-joined").replace("%clanName%", clan.getName())));
			return;
		}
		case "leave":
		case "quit":
		case "sair": {
			if (player.getClan() == null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-dont-have-any-clan")));
				return;
			}
			Clan clan;
			try {
				clan = DataClan.getClan(args.getArgs()[1]);
			} catch (Exception e) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(

						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-cant-request")));
				return;
			}
			if (clan == null) {
				args.getSender().sendMessage(TextComponent
						.fromLegacyText(clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-not-exists")));
				return;
			}
			if (clan.isOwner(player)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-owner-disband")));
				return;
			}
			if (clan.isAdministrator(player)) {
				clan.demote(player.getUniqueId());
			}
			if (clan.removeParticipant(player.getUniqueId()))
				player.setClanUniqueId(null);
			else {
				args.getSender().sendMessage(TextComponent.fromLegacyText(clanPrefix + "ERROR"));
				return;
			}
			for (UUID uuid : clan.getParticipants().keySet()) {
				ProxiedPlayer proxied = BungeeMain.getPlugin().getProxy().getPlayer(uuid);
				if (proxied == null)
					continue;
				Language lang = BattlebitsAPI.getDefaultLanguage();
				BattlePlayer battlePlayer = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxied.getUniqueId());
				if (battlePlayer != null)
					lang = battlePlayer.getLanguage();
				proxied.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(lang, "clan-prefix") + " "
						+ Translate.getTranslation(lang, "clan-player-leaved").replace("%player%", player.getName())));
			}
			return;
		}
		case "invite":
		case "convidar": {
			if (player.getClan() == null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-dont-have-any-clan")));
				return;
			}
			Clan clan = player.getClan();
			if (!clan.isAdministrator(player)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-you-not-admin")));
				return;
			}
			if (args.getArgs().length != 2) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-invite-usage")));
				return;
			}
			UUID uuid = BattlebitsAPI.getUUIDOf(args.getArgs()[1]);
			if (uuid == null) {
				args.getSender().sendMessage(TextComponent
						.fromLegacyText(clanPrefix + Translate.getTranslation(args.getLanguage(), "player-not-exist")));
				return;
			}
			BattlePlayer target = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
			if (target == null) {
				try {
					target = DataPlayer.getPlayer(uuid);
				} catch (Exception e) {
					e.printStackTrace();
					args.getSender().sendMessage(TextComponent.fromLegacyText(
							clanPrefix + Translate.getTranslation(args.getLanguage(), "cant-request-offline")));
					return;
				}
				if (target == null) {
					args.getSender().sendMessage(TextComponent.fromLegacyText(
							clanPrefix + Translate.getTranslation(args.getLanguage(), "player-never-joined")));
					return;
				}
			}
			if (target.getClanUniqueId() != null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-already-have-clan")));
				return;
			}
			if (clan.isParticipant(target)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-already-participant")));
				return;
			}
			if (clan.isInvited(target)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-already-invited")));
				return;
			}
			clan.invite(target);
			for (UUID participante : clan.getParticipants().keySet()) {
				ProxiedPlayer proxied = BungeeMain.getPlugin().getProxy().getPlayer(participante);
				if (proxied == null)
					continue;
				Language lang = BattlebitsAPI.getDefaultLanguage();
				BattlePlayer battlePlayer = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxied.getUniqueId());
				if (battlePlayer != null)
					lang = battlePlayer.getLanguage();
				proxied.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(lang, "clan-prefix") + " "
						+ Translate.getTranslation(lang, "clan-player-invited").replace("%player%", target.getName())
								.replace("%byPlayer%", player.getName())));
			}
			ProxiedPlayer targetP = BungeeMain.getPlugin().getProxy().getPlayer(target.getUniqueId());
			if (targetP != null)
				targetP.sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(target.getLanguage(), "clan-you-have-been-invited")
								.replace("%byPlayer%", player.getName()).replace("%clanName%", clan.getName())));
			return;
		}
		case "removeinvite":
		case "removerconvite": {
			if (player.getClan() == null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-dont-have-any-clan")));
				return;
			}
			Clan clan = player.getClan();
			if (!clan.isAdministrator(player)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-you-not-admin")));
				return;
			}
			if (args.getArgs().length != 2) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-removeinvite-usage")));
				return;
			}
			UUID uuid = BattlebitsAPI.getUUIDOf(args.getArgs()[1]);
			if (uuid == null) {
				args.getSender().sendMessage(TextComponent
						.fromLegacyText(clanPrefix + Translate.getTranslation(args.getLanguage(), "player-not-exist")));
				return;
			}
			if (!clan.isInvited(uuid)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-not-invited")));
				return;
			}
			clan.removeInvite(uuid);
			for (UUID participante : clan.getParticipants().keySet()) {
				ProxiedPlayer proxied = BungeeMain.getPlugin().getProxy().getPlayer(participante);
				if (proxied == null)
					continue;
				Language lang = BattlebitsAPI.getDefaultLanguage();
				BattlePlayer battlePlayer = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxied.getUniqueId());
				if (battlePlayer != null)
					lang = battlePlayer.getLanguage();
				proxied.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(lang, "clan-prefix") + " "
						+ Translate.getTranslation(lang, "clan-player-invite-removed")
								.replace("%player%", args.getArgs()[1]).replace("%byPlayer%", player.getName())));
			}
			return;
		}
		case "promote":
		case "promover": {
			if (player.getClan() == null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-dont-have-any-clan")));
				return;
			}
			Clan clan = player.getClan();
			if (!clan.isOwner(player)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-you-not-owner")));
				return;
			}
			if (args.getArgs().length != 2) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-promote-usage")));
				return;
			}
			UUID uuid = BattlebitsAPI.getUUIDOf(args.getArgs()[1]);
			if (uuid == null) {
				args.getSender().sendMessage(TextComponent
						.fromLegacyText(clanPrefix + Translate.getTranslation(args.getLanguage(), "player-not-exist")));
				return;
			}
			if (!clan.isParticipant(uuid)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-not-participant")));
				return;
			}
			if (clan.isOwner(uuid)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-is-owner")));
				return;
			}
			if (clan.isAdministrator(uuid)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-already-admin")));
				return;
			}
			clan.promote(uuid);
			for (UUID participante : clan.getParticipants().keySet()) {
				ProxiedPlayer proxied = BungeeMain.getPlugin().getProxy().getPlayer(participante);
				if (proxied == null)
					continue;
				Language lang = BattlebitsAPI.getDefaultLanguage();
				BattlePlayer battlePlayer = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxied.getUniqueId());
				if (battlePlayer != null)
					lang = battlePlayer.getLanguage();
				proxied.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(lang, "clan-prefix") + " "
						+ Translate.getTranslation(lang, "clan-player-promoted").replace("%player%", args.getArgs()[1])
								.replace("%byPlayer%", player.getName())));
			}
			return;
		}
		case "demote":
		case "rebaixar": {
			if (player.getClan() == null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-dont-have-any-clan")));
				return;
			}
			Clan clan = player.getClan();
			if (!clan.isOwner(player)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-you-not-owner")));
				return;
			}
			if (args.getArgs().length != 2) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-demote-usage")));
				return;
			}
			UUID uuid = BattlebitsAPI.getUUIDOf(args.getArgs()[1]);
			if (uuid == null) {
				args.getSender().sendMessage(TextComponent
						.fromLegacyText(clanPrefix + Translate.getTranslation(args.getLanguage(), "player-not-exist")));
				return;
			}
			if (!clan.isParticipant(uuid)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-not-participant")));
				return;
			}
			if (clan.isOwner(uuid)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-is-owner")));
				return;
			}
			if (!clan.isAdministrator(uuid)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-not-admin")));
				return;
			}
			clan.demote(uuid);
			for (UUID participante : clan.getParticipants().keySet()) {
				ProxiedPlayer proxied = BungeeMain.getPlugin().getProxy().getPlayer(participante);
				if (proxied == null)
					continue;
				Language lang = BattlebitsAPI.getDefaultLanguage();
				BattlePlayer battlePlayer = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxied.getUniqueId());
				if (battlePlayer != null)
					lang = battlePlayer.getLanguage();
				proxied.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(lang, "clan-prefix") + " "
						+ Translate.getTranslation(lang, "clan-player-demoted").replace("%player%", args.getArgs()[1])
								.replace("%byPlayer%", player.getName())));
			}
			return;
		}
		case "kick":
		case "expulsar":
		case "remover":
		case "remove":
		case "ban":
		case "banir": {
			if (player.getClan() == null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-dont-have-any-clan")));
				return;
			}
			Clan clan = player.getClan();
			if (!clan.isAdministrator(player)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-not-admin")));
				return;
			}
			if (args.getArgs().length != 2) {
				args.getSender().sendMessage(TextComponent
						.fromLegacyText(clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-kick-usage")));
				return;
			}
			UUID uuid = BattlebitsAPI.getUUIDOf(args.getArgs()[1]);
			if (uuid == null) {
				args.getSender().sendMessage(TextComponent
						.fromLegacyText(clanPrefix + Translate.getTranslation(args.getLanguage(), "player-not-exist")));
				return;
			}
			BattlePlayer target = BattlebitsAPI.getAccountCommon().getBattlePlayer(uuid);
			if (target == null) {
				try {
					target = DataPlayer.getPlayer(uuid);
				} catch (Exception e) {
					e.printStackTrace();
					args.getSender().sendMessage(TextComponent.fromLegacyText(
							clanPrefix + Translate.getTranslation(args.getLanguage(), "cant-request-offline")));
					return;
				}
				if (target == null) {
					args.getSender().sendMessage(TextComponent.fromLegacyText(
							clanPrefix + Translate.getTranslation(args.getLanguage(), "player-never-joined")));
					return;
				}
			}
			if (!clan.isParticipant(target)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-not-participant")));
				return;
			}
			if (clan.isOwner(target)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-is-owner")));
				return;
			}
			if (clan.isAdministrator(target)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-is-admin")));
				return;
			}
			if (clan.removeParticipant(uuid))
				target.setClanUniqueId(null);
			else {
				args.getSender().sendMessage(TextComponent.fromLegacyText(clanPrefix + "ERROR"));
				return;
			}
			clan.removeParticipant(uuid);
			for (UUID participante : clan.getParticipants().keySet()) {
				ProxiedPlayer proxied = BungeeMain.getPlugin().getProxy().getPlayer(participante);
				if (proxied == null)
					continue;
				Language lang = BattlebitsAPI.getDefaultLanguage();
				BattlePlayer battlePlayer = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxied.getUniqueId());
				if (battlePlayer != null)
					lang = battlePlayer.getLanguage();
				proxied.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(lang, "clan-prefix") + " "
						+ Translate.getTranslation(lang, "clan-player-kicked").replace("%player%", args.getArgs()[1])
								.replace("%byPlayer%", player.getName())));
			}
			ProxiedPlayer targetP = BungeeMain.getPlugin().getProxy().getPlayer(uuid);
			if (targetP != null) {
				Language lang = target.getLanguage();
				targetP.sendMessage(TextComponent.fromLegacyText(clanPrefix + Translate
						.getTranslation(lang, "clan-you-have-been-kicked").replace("%byPlayer%", player.getName())));
			}
			return;
		}
		case "info":
		case "informacao": {
			Clan clan = null;
			if (args.getArgs().length == 2) {
				clan = BattlebitsAPI.getClanCommon().getClan(args.getArgs()[1]);
				if (clan == null) {
					try {
						clan = DataClan.getClan(args.getArgs()[1]);
					} catch (Exception e) {
						args.getSender().sendMessage(TextComponent.fromLegacyText(
								clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-cant-request")));
					}
					if (clan == null) {
						args.getSender().sendMessage(TextComponent.fromLegacyText(
								clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-not-exists")));
						return;
					} else {
						DataClan.cacheRedisClan(clan.getUniqueId(), clan.getName());
					}
				}
			} else if (player.getClan() != null) {
				clan = player.getClan();
			} else {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-info-usage-dont-have-any")));
				return;
			}
			clanInfo(args.getLanguage(), args.getSender(), clan, clan == player.getClan());
			// TODO Send information on InventoryGUI
			return;
		}
		case "changeabb":
		case "mudarsigla": {
			if (player.getClan() == null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-dont-have-any-clan")));
				return;
			}
			Clan clan = player.getClan();
			if (!clan.isOwner(player)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-you-not-owner")));
				return;
			}
			if (args.getArgs().length != 2) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-changeabb-usage")));
				return;
			}
			if (player.getMoney() < CHANGE_ABB_PRICE) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-dont-have-money")));
				return;
			}
			String abbreviation = args.getArgs()[1];
			if (abbreviation.length() < 3 || abbreviation.length() > 5) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-changeabb-between-3-5")));
				return;
			}
			if (!isLegal(abbreviation)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(clanPrefix
						+ Translate.getTranslation(args.getLanguage(), "clan-clanAbbreviation-invalid-chars")));
				return;
			}
			if (DataClan.clanAbbreviationExists(abbreviation)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(clanPrefix
						+ Translate.getTranslation(args.getLanguage(), "clan-clanAbbreviation-already-exists")));
				return;
			}
			clan.changeAbbreviation(abbreviation);
			player.removeMoney(CHANGE_ABB_PRICE);
			for (UUID participante : clan.getParticipants().keySet()) {
				ProxiedPlayer proxied = BungeeMain.getPlugin().getProxy().getPlayer(participante);
				if (proxied == null)
					continue;
				Language lang = BattlebitsAPI.getDefaultLanguage();
				BattlePlayer battlePlayer = BattlebitsAPI.getAccountCommon().getBattlePlayer(proxied.getUniqueId());
				if (battlePlayer != null) {
					lang = battlePlayer.getLanguage();
				}
				proxied.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(lang, "clan-prefix") + " "
						+ Translate.getTranslation(lang, "clan-abbreviation-changed")
								.replace("%abb%", clan.getAbbreviation()).replace("%player%", player.getName())));
			}
			return;
		}
		case "disband":
		case "deletar": {
			if (player.getClan() == null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-dont-have-any-clan")));
				return;
			}
			Clan clan = player.getClan();
			if (!clan.isOwner(player)) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-player-not-owner")));
				return;
			}
			if (clan.confirm()) {
				for (UUID participante : clan.getParticipants().keySet()) {
					Language lang = BattlebitsAPI.getDefaultLanguage();
					BattlePlayer battlePlayer = DataPlayer.getPlayer(participante);
					if (battlePlayer != null) {
						lang = battlePlayer.getLanguage();
						battlePlayer.setClanUniqueId(null);
					}
					ProxiedPlayer proxied = BungeeMain.getPlugin().getProxy().getPlayer(participante);
					if (proxied != null)
						proxied.sendMessage(TextComponent.fromLegacyText(Translate.getTranslation(lang, "clan-prefix")
								+ " " + Translate.getTranslation(lang, "clan-disand-success")
										.replace("%player%", player.getName()).replace("%clanName%", clan.getName())));
				}
				DataClan.disbandMongoClan(clan);
				DataClan.disbandRedisClan(clan);
				BattlebitsAPI.getClanCommon().unloadClan(clan.getUniqueId());
			} else {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-confirm-disand")));
			}
			return;
		}
		case "chat": {
			if (player.getClan() == null) {
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-dont-have-any-clan")));
				return;
			}
			if (player.getConfiguration().isClanChatEnabled()) {
				player.getConfiguration().setClanChatEnabled(false);
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-chat-disabled")));
			} else {
				player.getConfiguration().setClanChatEnabled(true);
				args.getSender().sendMessage(TextComponent.fromLegacyText(
						clanPrefix + Translate.getTranslation(args.getLanguage(), "clan-chat-enabled")));
			}
			return;
		}
		default:
			args.getSender().sendMessage(
					TextComponent.fromLegacyText(Translate.getTranslation(args.getLanguage(), "clan-help")));
			break;
		}
	}

	public boolean isLegal(String clanName) {
		Pattern pattern = Pattern.compile("[a-zA-Z0-9]{3,16}");
		Matcher matcher = pattern.matcher(clanName);
		return matcher.matches();
	}

	private void clanInfo(Language lang, CommandSender sender, Clan clan, boolean self) {
		String clanInfo = Translate.getTranslation(lang, "clan-info" + (self ? "" : "-other"));
		clanInfo = clanInfo.replace("%name%", clan.getName());
		clanInfo = clanInfo.replace("%league%", clan.getRank().name());
		clanInfo = clanInfo.replace("%abb%", clan.getAbbreviation());
		clanInfo = clanInfo
				.replace("%owner%",
						(self ? ((BungeeMain.getPlugin().getProxy().getPlayer(clan.getOwner())) != null
								? ChatColor.GREEN + "" : ChatColor.RED + "") : "")
								+ clan.getParticipants().get(clan.getOwner()));
		clanInfo = clanInfo.replace("%slots%", clan.getSlots() + "");
		clanInfo = clanInfo.replace("%players%", clan.getParticipants().size() + "");
		clanInfo = clanInfo.replace("%xp%", clan.getXp() + "");

		ArrayList<UUID> admins = new ArrayList<>();
		ArrayList<UUID> players = new ArrayList<>();
		for (UUID uuid : clan.getParticipants().keySet()) {
			if (clan.isOwner(uuid))
				continue;
			if (clan.isAdministrator(uuid)) {
				admins.add(uuid);
				continue;
			}
			players.add(uuid);
		}
		// TODO HOVER - ServerIP | Click - Teleport Server
		String adminList = "";
		for (UUID admin : admins) {
			if (self) {
				if (BungeeMain.getPlugin().getProxy().getPlayer(admin) != null)
					adminList = adminList + ChatColor.GREEN + clan.getPlayerName(admin) + ", ";
				else
					adminList = adminList + ChatColor.RED + clan.getPlayerName(admin) + ", ";
			} else
				adminList = adminList + clan.getPlayerName(admin) + ", ";
		}
		clanInfo = clanInfo.replace("%adminList%", adminList);
		String playerList = "";
		for (UUID pl : players) {
			if (self) {
				if (BungeeMain.getPlugin().getProxy().getPlayer(pl) != null)
					playerList = playerList + ChatColor.GREEN + clan.getPlayerName(pl) + ", ";
				else
					playerList = playerList + ChatColor.RED + clan.getPlayerName(pl) + ", ";
			} else
				playerList = playerList + clan.getPlayerName(pl) + ", ";
		}
		clanInfo = clanInfo.replace("%playerList%", playerList);
		if (clanInfo.contains("\n"))
			for (String str : clanInfo.split("\n")) {
				sender.sendMessage(TextComponent.fromLegacyText(str));
			}
	}

}
