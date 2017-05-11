package br.com.battlebits.commons.bukkit.command.register;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.admin.AdminMode;
import br.com.battlebits.commons.api.chat.ChatAPI;
import br.com.battlebits.commons.api.chat.ChatAPI.ChatState;
import br.com.battlebits.commons.api.vanish.VanishAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.command.BukkitCommandArgs;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.command.CommandClass;
import br.com.battlebits.commons.core.command.CommandFramework.Command;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.T;

public class StaffCommand implements CommandClass {

	@Command(name = "admin", groupToUse = Group.TRIAL, noPermMessageId = "command-admin-no-access")
	public void admin(BukkitCommandArgs args) {
		if (args.isPlayer()) {
			Player p = args.getPlayer();
			if (AdminMode.getInstance().isAdmin(p)) {
				AdminMode.getInstance().setPlayer(p);
			} else {
				AdminMode.getInstance().setAdmin(p);
			}
		}
	}

	@Command(name = "updatevanish", groupToUse = Group.TRIAL, noPermMessageId = "command-admin-no-access")
	public void updatevanish(BukkitCommandArgs args) {
		if (args.isPlayer()) {
			Player p = args.getPlayer();
			VanishAPI.getInstance().updateVanishToPlayer(p);
		}
	}

	@Command(name = "visible", aliases = { "vis", "visivel" }, groupToUse = Group.TRIAL, noPermMessageId = "command-vanish-no-access")
	public void visible(BukkitCommandArgs args) {
		if (args.isPlayer()) {
			Player p = args.getPlayer();
			VanishAPI.getInstance().showPlayer(p);
			p.sendMessage("§%command-vanish-prefix%§ §%command-vanish-visible-all%§");
		}
	}

	@Command(name = "invisible", aliases = { "invis", "invisivel" }, groupToUse = Group.TRIAL, noPermMessageId = "command-vanish-no-access")
	public void invisible(BukkitCommandArgs args) {
		if (args.isPlayer()) {
			Player p = args.getPlayer();
			BattlePlayer bP = BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId());
			Group group = Group.NORMAL;
			if (args.getArgs().length > 0) {
				try {
					group = Group.valueOf(args.getArgs()[0].toUpperCase());
				} catch (Exception e) {
					p.sendMessage("§%command-vanish-prefix%§ §%command-vanish-rank-not-exist%§");
					return;
				}
				if (group.ordinal() >= bP.getServerGroup().ordinal()) {
					p.sendMessage("§%command-vanish-prefix%§ §%command-vanish-rank-high%§");
					return;
				}
			} else
				group = VanishAPI.getInstance().hidePlayer(p);
			VanishAPI.getInstance().setPlayerVanishToGroup(p, group);
			p.sendMessage("§%command-vanish-prefix%§ " + T.t(BukkitMain.getInstance(), bP.getLanguage(), "command-vanish-invisible", new String[] { "%invisible%", group.toString() }));
		}
	}

	@Command(name = "inventorysee", aliases = { "invsee", "inv" }, groupToUse = Group.TRIAL, noPermMessageId = "command-inventorysee-no-access")
	public void inventorysee(BukkitCommandArgs args) {
		if (args.isPlayer()) {
			Player p = args.getPlayer();
			BattlePlayer bP = BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId());
			if (args.getArgs().length == 0) {
				p.sendMessage("§%command-inventorysee-prefix%§ " + T.t(BukkitMain.getInstance(), bP.getLanguage(), "command-inventorysee-usage", new String[] { "%command%", args.getLabel() }));
			} else {
				Player t = Bukkit.getPlayer(args.getArgs()[0]);
				if (t != null) {
					p.sendMessage("§%command-inventorysee-prefix%§ " + T.t(BukkitMain.getInstance(), bP.getLanguage(), "command-inventorysee-success", new String[] { "%player%", t.getName() }));
					p.openInventory(t.getInventory());
				} else {
					p.sendMessage("§%command-inventorysee-prefix%§ §%command-inventorysee-not-found%§");
				}
			}
		}
	}

	@Command(name = "chat", groupToUse = Group.MOD, noPermMessageId = "command-chat-no-access")
	public void chat(BukkitCommandArgs args) {
		if (args.isPlayer()) {
			if (args.getArgs().length == 1) {
				if (args.getArgs()[0].equalsIgnoreCase("on")) {
					if (ChatAPI.getInstance().getChatState() == ChatState.ENABLED) {
						args.getPlayer().sendMessage("§%command-chat-prefix%§ §%command-chat-already-enabled%§");
						return;
					}
					ChatAPI.getInstance().setChatState(ChatState.ENABLED);
					args.getPlayer().sendMessage("§%command-chat-prefix%§ §%command-chat-enabled%§");
				} else if (args.getArgs()[0].equalsIgnoreCase("off")) {
					if (ChatAPI.getInstance().getChatState() == ChatState.YOUTUBER) {
						args.getPlayer().sendMessage("§%command-chat-prefix%§ §%command-chat-already-disabled%§");
						return;
					}
					ChatAPI.getInstance().setChatState(ChatState.YOUTUBER);
					args.getPlayer().sendMessage("§%command-chat-prefix%§ §%command-chat-disabled%§");
				} else {
					args.getPlayer().sendMessage("§%command-chat-prefix%§ §%command-chat-usage%§");
				}
			} else {
				args.getPlayer().sendMessage("§%command-chat-prefix%§ §%command-chat-usage%§");
			}
		}
	}

	@Command(name = "clearchat", aliases = { "limparchat" }, groupToUse = Group.TRIAL, noPermMessageId = "command-chat-no-access")
	public void clearchat(BukkitCommandArgs args) {
		if (args.isPlayer()) {
			HashMap<String, String> map = new HashMap<>();
			map.put("%player%", args.getPlayer().getName());
			for (Player p : Bukkit.getOnlinePlayers()) {
				for (int i = 0; i < 100; i++)
					p.sendMessage("");
				p.sendMessage("§%command-chat-prefix%§ " + T.t(BukkitMain.getInstance(), BattlePlayer.getLanguage(p.getUniqueId()), "command-chat-success", map));
			}
		}
	}
}
