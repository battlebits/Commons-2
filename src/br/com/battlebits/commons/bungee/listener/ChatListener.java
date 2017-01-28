package br.com.battlebits.commons.bungee.listener;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.account.Tag;
import br.com.battlebits.commons.core.clan.Clan;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.Translate;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ChatListener implements Listener {

	@EventHandler
	public void onChat(ChatEvent event) {
		if (event.isCommand())
			return;
		if (event.isCancelled())
			return;
		if (!(event.getSender() instanceof ProxiedPlayer))
			return;
		BattlePlayer player = BattlebitsAPI.getAccountCommon()
				.getBattlePlayer(((ProxiedPlayer) event.getSender()).getUniqueId());
		if (player.getConfiguration().isStaffChatEnabled()) {
			sendStaffMessage(player, event.getMessage());
			event.setCancelled(true);
		} else if (player.getConfiguration().isClanChatEnabled() && player.getClan() != null) {
			sendClanMessage(player, event.getMessage());
			event.setCancelled(true);
		}
	}

	public static void sendStaffMessage(BattlePlayer bP, String message) {
		for (ProxiedPlayer player : BungeeMain.getPlugin().getProxy().getPlayers()) {
			BattlePlayer onlineBp = BattlebitsAPI.getAccountCommon().getBattlePlayer(player.getUniqueId());
			if (!onlineBp.hasGroupPermission(Group.YOUTUBERPLUS))
				continue;
			String tag = Tag.valueOf(bP.getServerGroup().toString()).getPrefix(onlineBp.getLanguage());
			String format = tag + (ChatColor.stripColor(tag).trim().length() > 0 ? " " : "") + bP.getName()
					+ ChatColor.WHITE + ": ";

			player.sendMessage(TextComponent
					.fromLegacyText(ChatColor.YELLOW + "" + ChatColor.BOLD + "[STAFF] " + format + message));
		}
	}

	public static void sendClanMessage(BattlePlayer bP, String message) {
		for (ProxiedPlayer player : BungeeMain.getPlugin().getProxy().getPlayers()) {
			BattlePlayer onlineBp = BattlebitsAPI.getAccountCommon().getBattlePlayer(player.getUniqueId());
			if (onlineBp == null)
				continue;
			Clan clan = bP.getClan();
			if (!clan.isParticipant(onlineBp))
				continue;

			String tag = "";
			if (clan.isOwner(bP)) {
				tag = ChatColor.WHITE + " - " + ChatColor.DARK_RED + ""
						+ Translate.getTranslation(onlineBp.getLanguage(), "owner");
			} else if (clan.isAdministrator(bP)) {
				tag = ChatColor.WHITE + " - " + ChatColor.RED + ""
						+ Translate.getTranslation(onlineBp.getLanguage(), "admin");
			}
			String format = ChatColor.DARK_RED + "[CLAN" + tag.toUpperCase() + ChatColor.DARK_RED + "] "
					+ ChatColor.GRAY + bP.getName() + ChatColor.WHITE + ": ";

			player.sendMessage(TextComponent.fromLegacyText(format + message));
		}

	}
}
