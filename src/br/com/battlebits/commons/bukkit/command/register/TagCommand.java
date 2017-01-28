package br.com.battlebits.commons.bukkit.command.register;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.account.BukkitPlayer;
import br.com.battlebits.commons.bukkit.command.BukkitCommandArgs;
import br.com.battlebits.commons.core.account.Tag;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.translate.Translate;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

public class TagCommand implements CommandClass {
	@Command(name = "tag", runAsync = true)
	public void tag(BukkitCommandArgs cmdArgs) {
		if (cmdArgs.isPlayer()) {
			Player p = cmdArgs.getPlayer();
			String[] args = cmdArgs.getArgs();
			BukkitPlayer player = (BukkitPlayer) BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId());
			String prefix = Translate.getTranslation(player.getLanguage(), "command-tag-prefix") + " ";
			if (!BukkitMain.getPlugin().isTagControl()) {
				p.sendMessage(prefix + "§%command-tag-not-enabled%§");
				return;
			}
			if (args.length == 0) {
				int max = player.getTags().size() * 2;
				TextComponent[] message = new TextComponent[max];
				message[0] = new TextComponent(
						prefix + Translate.getTranslation(player.getLanguage(), "command-tag-available") + " ");
				int i = max - 1;
				for (Tag t : player.getTags()) {
					if (i < max - 1) {
						message[i] = new TextComponent("§f, ");
						i -= 1;
					}
					TextComponent component = new TextComponent(
							(t == Tag.NORMAL) ? "§7§lNORMAL" : t.getPrefix(player.getLanguage()));
					component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TextComponent[] { new TextComponent(
							Translate.getTranslation(player.getLanguage(), "command-tag-click-select")) }));
					component.setClickEvent(
							new ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND, "/tag " + t.name()));
					message[i] = component;
					i -= 1;
					component = null;
				}
				p.spigot().sendMessage(message);
				message = null;
			} else {
				Tag tag = null;
				try {
					tag = Tag.getTag(args[0].toUpperCase(), player.getLanguage());
				} catch (Exception ex) {
					p.sendMessage(prefix + Translate.getTranslation(player.getLanguage(), "command-tag-not-found"));
					return;
				}
				if (tag != null) {
					if (player.getTags().contains(tag)) {
						if (player.getTag() != tag) {
							if (player.setTag(tag)) {
								p.sendMessage(
										prefix + Translate.getTranslation(player.getLanguage(), "command-tag-selected")
												.replace("%tag%", ((tag == Tag.NORMAL) ? "§7§lNORMAL"
														: tag.getPrefix(player.getLanguage()))));
							}
						} else {
							p.sendMessage(
									prefix + Translate.getTranslation(player.getLanguage(), "command-tag-current"));
						}
					} else {
						p.sendMessage(prefix + Translate.getTranslation(player.getLanguage(), "command-tag-no-access"));
					}
					tag = null;
				} else {
					p.sendMessage(prefix + Translate.getTranslation(player.getLanguage(), "command-tag-not-found"));
				}
			}
			prefix = null;
			player = null;
			args = null;
			p = null;
		} else {
			cmdArgs.getSender().sendMessage("§4§lERRO §fComando disponivel apenas §c§lin-game");
		}
	}
}
