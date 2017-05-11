package br.com.battlebits.commons.bukkit.menu.account;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.commons.api.menu.ClickType;
import br.com.battlebits.commons.api.menu.MenuClickHandler;
import br.com.battlebits.commons.api.menu.MenuInventory;
import br.com.battlebits.commons.api.menu.MenuItem;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.account.League;
import br.com.battlebits.commons.core.account.RankType;
import br.com.battlebits.commons.core.account.Tag;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.server.ServerStaff;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.util.DateUtils;
import net.md_5.bungee.api.ChatColor;

public class AccountMenu {

	public AccountMenu(Player player, BattlePlayer opener, BattlePlayer battlePlayer) {
		MenuInventory menu = new MenuInventory("§%account%§ - " + battlePlayer.getName(), 6);

		ItemBuilder builder = new ItemBuilder().type(Material.SKULL_ITEM).durability(3).name(battlePlayer.getName());

		if (opener.hasGroupPermission(Group.YOUTUBER)) {
			builder.lore(ChatColor.GRAY + "FakeName: " + battlePlayer.getFakeName());
		}

		ItemStack stack = builder.build();
		SkullMeta meta = (SkullMeta) stack.getItemMeta();
		meta.setOwner(battlePlayer.getName());
		stack.setItemMeta(meta);
		MenuItem item = new MenuItem(stack);
		menu.setItem(13, item);
		String clanName = "§%dont-have-clan%§";
		if (battlePlayer.getClan() != null)
			clanName = battlePlayer.getClan().getName();
		stack = new ItemBuilder().type(Material.BLAZE_POWDER).name(ChatColor.GOLD + "" + ChatColor.BOLD + "Clan").lore("§%clanName%§: " + clanName).build();
		menu.setItem(19, stack);
		stack = new ItemBuilder().type(Material.GOLD_INGOT).name(ChatColor.GOLD + "" + ChatColor.BOLD + "§%money%§").lore(ChatColor.GRAY + "" + battlePlayer.getMoney()).build();
		menu.setItem(21, stack);
		stack = new ItemBuilder().type(Material.PAPER).name(ChatColor.BOLD + "§%tickets%§").lore(ChatColor.GRAY + "" + battlePlayer.getFichas()).build();
		menu.setItem(22, stack);
		stack = new ItemBuilder().type(Material.EXP_BOTTLE).name(ChatColor.RED + "" + ChatColor.BOLD + "Xp").lore(ChatColor.GRAY + "" + battlePlayer.getXp()).build();
		menu.setItem(23, stack);
		builder = new ItemBuilder().type(Material.WATCH).name(ChatColor.BLUE + "" + ChatColor.BOLD + "§%time-information%§");
		Date date = new Date(battlePlayer.getFirstTimePlaying());
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:SS");
		StringBuilder loreBuilder = new StringBuilder();
		loreBuilder.append("\n" + ChatColor.GRAY + "§%first-join%§: " + ChatColor.RESET + df.format(date));
		date = new Date(battlePlayer.getLastLoggedIn());
		loreBuilder.append("\n" + ChatColor.GRAY + "§%last-login%§: " + ChatColor.RESET + df.format(date));
		loreBuilder.append("\n" + ChatColor.GRAY + "§%total-online-time%§: " + ChatColor.RESET + DateUtils.formatDifference(opener.getLanguage(), battlePlayer.getOnlineTime() / 1000) + "\n");
		if (battlePlayer.isOnline()) {
			date = new Date(System.currentTimeMillis() - battlePlayer.getJoinTime());
			loreBuilder.append("\n" + ChatColor.GRAY + "§%current-online-time%§: " + ChatColor.RESET + DateUtils.formatDifference(opener.getLanguage(), (System.currentTimeMillis() - battlePlayer.getJoinTime()) / 1000));
		}
		builder.lore(loreBuilder.toString());
		menu.setItem(25, builder.build());
		builder = new ItemBuilder().type(Material.NAME_TAG).name(ChatColor.GOLD + "" + ChatColor.BOLD + "§%league%§");
		loreBuilder = new StringBuilder();

		for (int i = League.values().length; i > 0; i--) {
			League league = League.values()[i - 1];
			String str = league.getSymbol() + " " + league.toString();
			ChatColor seta = ChatColor.GRAY;
			if (battlePlayer.getLeague() != league) {
				str = ChatColor.stripColor(str);
			} else
				seta = ChatColor.GOLD;
			loreBuilder.append((i != League.values().length ? "\n" : "") + seta + "" + ChatColor.BOLD + "➟ " + str);
		}
		builder.lore(loreBuilder.toString());
		menu.setItem(31, builder.build());
		if (opener.getUniqueId().equals(battlePlayer.getUniqueId()))
			menu.setItem(47, new MenuItem(new ItemBuilder().type(Material.REDSTONE_COMPARATOR).name("§%preferences%§").lore("§%preferences-lore%§").build(), new MenuClickHandler() {
				@Override
				public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
					// TODO Open Preferences menu
				}
			}));
		builder = new ItemBuilder().type(Material.COMPASS).name("§%location%§");
		loreBuilder = new StringBuilder();
		loreBuilder.append("§%country%§: " + ChatColor.RESET + battlePlayer.getCountry());
		if (battlePlayer.getUniqueId() == opener.getUniqueId() || opener.hasGroupPermission(Group.STAFF)) {
			loreBuilder.append("\n§%region%§: " + ChatColor.RESET + battlePlayer.getRegion());
			loreBuilder.append("\n§%city%§: " + ChatColor.RESET + battlePlayer.getCity());
		}
		menu.setItem(46, builder.lore(loreBuilder.toString()).build());

		builder = new ItemBuilder().type(Material.EYE_OF_ENDER).name("§%groups%§");
		loreBuilder = new StringBuilder();
		loreBuilder.append("§%actual-group%§: " + Tag.valueOf(battlePlayer.getServerGroup().toString()).getPrefix(opener.getLanguage()));
		if (battlePlayer.getRanks().size() > 0) {
			loreBuilder.append("\n");
			for (Entry<RankType, Long> entry : battlePlayer.getRanks().entrySet()) {
				loreBuilder.append("\n" + Tag.valueOf(entry.getKey().toString()).getPrefix(opener.getLanguage()) + ChatColor.RESET + " - " + DateUtils.formatDifference(opener.getLanguage(), (entry.getValue() - System.currentTimeMillis()) / 1000));
			}
		}
		if (battlePlayer.getGroups().size() > 0) {
			loreBuilder.append("\n");
			for (Entry<ServerStaff, Group> staff : battlePlayer.getGroups().entrySet()) {
				loreBuilder.append("\n" + ChatColor.WHITE + ChatColor.BOLD + staff.getKey().toString() + " - " + Tag.valueOf(staff.getValue().toString()).getPrefix(opener.getLanguage()));
			}
		}

		menu.setItem(49, builder.lore(loreBuilder.toString()).build());

		menu.setItem(51, new MenuItem(new ItemBuilder().type(Material.BARRIER).name("§%bans%§").lore("§%bans-lore%§").build(), new MenuClickHandler() {

			@Override
			public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
				new BanMenu(player, opener, battlePlayer, menu, 1);
			}
		}));

		menu.setItem(52, new MenuItem(new ItemBuilder().type(Material.BOOK_AND_QUILL).name("§%mutes%§").lore("§%mutes-lore%§").build(), new MenuClickHandler() {

			@Override
			public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
				new MuteMenu(player, opener, battlePlayer, menu, 1);
			}
		}));
		if (opener.getUniqueId().equals(battlePlayer.getUniqueId()) || battlePlayer.hasGroupPermission(Group.STAFF)) {
			menu.setItem(8, new ItemBuilder().type(Material.NETHER_STAR).name("§%ip-info%§").lore(T.t(BukkitMain.getInstance(), opener.getLanguage(), "ip-info-lore", new String[] { "%actual%", battlePlayer.getIpAddress() }, new String[] { "%last-ip-address%", battlePlayer.getLastIpAddress() })).build());

			menu.setItem(17, new ItemBuilder().type(Material.BOOKSHELF).name("§%report-points%§").lore(ChatColor.GRAY + "" + battlePlayer.getReportPoints()).build());
		}
		ItemStack nullItem = new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(15).name(" ").build();
		for (int i = 0; i < menu.getInventory().getSize(); i++) {
			if (menu.getItem(i) == null)
				menu.setItem(i, nullItem);
		}
		menu.open(player);
	}

}
