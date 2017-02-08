package br.com.battlebits.commons.bukkit.command.register;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.player.FakePlayerAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.command.BukkitCommandArgs;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.Translate;

public class YoutubeCommand implements CommandClass {

	@Command(name = "fake", groupToUse = Group.YOUTUBER, noPermMessageId = "command-fake-no-access", runAsync = true)
	public void fake(BukkitCommandArgs args) {
		if (!args.isPlayer()) {
			args.getSender().sendMessage("COMANDO PARA PLAYERS");
			return;
		}
		Player p = args.getPlayer();
		BattlePlayer bP = BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId());
		if (!bP.getServerGroup().toString().contains("YOUTUBER") && !bP.hasGroupPermission(Group.MODPLUS)) {
			p.sendMessage(Translate.getTranslation(args.getLanguage(), "command-fake-no-access"));
			return;
		}
		String fakePrefix = Translate.getTranslation(args.getLanguage(), "command-fake-prefix") + " ";
		if (args.getArgs().length != 1) {
			p.sendMessage(fakePrefix + Translate.getTranslation(args.getLanguage(), "command-fake-usage"));
			return;
		}
		String playerName = args.getArgs()[0];

		if (playerName.equalsIgnoreCase(bP.getName())) {
			new BukkitRunnable() {
				@Override
				public void run() {
					fakeremove(args);
				}
			}.runTask(BukkitMain.getInstance());
			return;
		}

		if (!FakePlayerAPI.validateName(playerName)) {
			p.sendMessage(fakePrefix + Translate.getTranslation(args.getLanguage(), "command-fake-invalid"));
			return;
		}
		if (BattlebitsAPI.getUUIDOf(playerName) != null) {
			p.sendMessage(fakePrefix + Translate.getTranslation(args.getLanguage(), "command-fake-player-exists"));
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				BukkitMain.getInstance().getTagManager().removePlayerTag(p);
				FakePlayerAPI.removePlayerSkin(p, false);
				FakePlayerAPI.changePlayerName(p, playerName, true);
				bP.setTag(bP.getTag());
				bP.setFakeName(playerName);
				p.sendMessage(
						fakePrefix + Translate.getTranslation(args.getLanguage(), "command-fake-changed-success"));
			}
		}.runTask(BukkitMain.getInstance());
	}

	@Command(name = "fakeremove", aliases = { "removefake",
			"removerfake" }, groupToUse = Group.YOUTUBER, noPermMessageId = "command-fakeremove-no-access")
	public void fakeremove(BukkitCommandArgs args) {
		if (!args.isPlayer()) {
			args.getSender().sendMessage("COMANDO PARA PLAYERS");
			return;
		}
		Player p = args.getPlayer();
		BattlePlayer bP = BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId());
		if (!bP.getServerGroup().toString().contains("YOUTUBER") && !bP.hasGroupPermission(Group.MODPLUS)) {
			p.sendMessage(Translate.getTranslation(args.getLanguage(), "command-fakeremove-no-access")
					.replace("%command%", args.getLabel()));
			return;
		}
		bP.setFakeName("");

		BukkitMain.getInstance().getTagManager().removePlayerTag(p);
		FakePlayerAPI.changePlayerSkin(p, bP.getName(), bP.getUniqueId(), false);
		FakePlayerAPI.changePlayerName(p, bP.getName(), true);
		bP.setTag(bP.getTag());
		p.sendMessage(Translate.getTranslation(args.getLanguage(), "command-fakeremove-prefix") + " "
				+ Translate.getTranslation(bP.getLanguage(), "command-fakeremove-changed-success"));
	}

	@Command(name = "changeskin", groupToUse = Group.ULTIMATE, noPermMessageId = "command-changeskin-no-access", runAsync = true)
	public void changeskin(BukkitCommandArgs args) {
		if (!args.isPlayer()) {
			args.getSender().sendMessage("COMANDO PARA PLAYERS");
			return;
		}
		Player p = args.getPlayer();
		String fakePrefix = Translate.getTranslation(args.getLanguage(), "command-changeskin-prefix") + " ";
		if (args.getArgs().length != 1) {
			p.sendMessage(fakePrefix + Translate.getTranslation(args.getLanguage(), "command-changeskin-usage"));
			return;
		}
		String playerName = args.getArgs()[0];
		if (!FakePlayerAPI.validateName(playerName)) {
			p.sendMessage(fakePrefix + Translate.getTranslation(args.getLanguage(), "command-changeskin-invalid"));
			return;
		}
		UUID uuid = BattlebitsAPI.getUUIDOf(playerName);

		if (uuid == null) {
			p.sendMessage(
					fakePrefix + Translate.getTranslation(args.getLanguage(), "command-changeskin-player-not-exists"));
			return;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				FakePlayerAPI.changePlayerSkin(p, playerName, uuid, true);
				p.sendMessage(fakePrefix
						+ Translate.getTranslation(args.getLanguage(), "command-changeskin-changed-success"));
			}
		}.runTask(BukkitMain.getInstance());
	}
}
