package br.com.battlebits.commons.bukkit.menu.account;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.commons.api.menu.ClickType;
import br.com.battlebits.commons.api.menu.MenuClickHandler;
import br.com.battlebits.commons.api.menu.MenuInventory;
import br.com.battlebits.commons.api.menu.MenuItem;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.account.League;
import br.com.battlebits.commons.core.permission.Group;
import net.md_5.bungee.api.ChatColor;

public class AccountMenu {

	public AccountMenu(Player player, BattlePlayer opener, BattlePlayer battlePlayer) {
		MenuInventory menu = new MenuInventory("§%account%§ - " + battlePlayer.getName(), 54);

		ItemBuilder builder = new ItemBuilder().type(Material.SKULL_ITEM).durability(3).name(battlePlayer.getName());

		if (opener.hasGroupPermission(Group.YOUTUBER)) {
			builder.lore("\n" + ChatColor.GRAY + "FakeName: " + battlePlayer.getFakeName());
		}
		MenuItem item = new MenuItem(builder.build());
		menu.setItem(13, item);
		String clanName = "§%dont-have-clan%§";
		if (battlePlayer.getClan() != null)
			clanName = battlePlayer.getClan().getName();
		ItemStack stack = new ItemBuilder().type(Material.BLAZE_POWDER)
				.name(ChatColor.GOLD + "" + ChatColor.BOLD + "Clan").lore("§%clanName%§: " + clanName).build();
		menu.setItem(19, stack);
		stack = new ItemBuilder().type(Material.GOLD_INGOT).name(ChatColor.GOLD + "" + ChatColor.BOLD + "§%money%§")
				.lore("\n" + battlePlayer.getMoney()).build();
		menu.setItem(21, stack);
		stack = new ItemBuilder().type(Material.PAPER).name(ChatColor.BOLD + "§%tickets%§")
				.lore("\n" + battlePlayer.getFichas()).build();
		menu.setItem(22, stack);
		stack = new ItemBuilder().type(Material.EXP_BOTTLE).name(ChatColor.RED + "" + ChatColor.BOLD + "Xp")
				.lore("\n" + battlePlayer.getXp()).build();
		menu.setItem(23, stack);
		builder = new ItemBuilder().type(Material.WATCH)
				.name(ChatColor.BLUE + "" + ChatColor.BOLD + "§%time-information%§");
		Date date = new Date(battlePlayer.getFirstTimePlaying());
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		StringBuilder loreBuilder = new StringBuilder();
		loreBuilder.append("\n" + ChatColor.GRAY + "§%first-join%§: " + df.format(date));
		date = new Date(battlePlayer.getOnlineTime());
		loreBuilder.append("\n" + ChatColor.GRAY + "§%last-login%§: " + df.format(date));
		date = new Date(battlePlayer.getLastLoggedIn());
		loreBuilder.append("\n" + ChatColor.GRAY + "§%total-online-time%§: " + df.format(date));
		if (battlePlayer.isOnline()) {
			date = new Date(System.currentTimeMillis() - battlePlayer.getJoinTime());
			loreBuilder.append("\n" + ChatColor.GRAY + "§%current-online-time%§: " + df.format(date));
		}
		builder.lore(loreBuilder.toString());
		menu.setItem(25, builder.build());
		builder = new ItemBuilder().type(Material.NAME_TAG).name(ChatColor.GOLD + "" + ChatColor.BOLD + "§%league%§");
		loreBuilder = new StringBuilder();

		for (int i = League.values().length; i > 0; i--) {
			League league = League.values()[i - 1];
			String str = league.getSymbol() + " " + league.toString();
			if (battlePlayer.getLeague() != league) {
				str = ChatColor.stripColor(str);
			}
			loreBuilder.append("\n" + ChatColor.GRAY + "" + ChatColor.BOLD + "➟" + ChatColor.stripColor(str));
		}
		builder.lore(loreBuilder.toString());
		menu.setItem(31, builder.build());
		menu.setItem(37, new MenuItem(new ItemBuilder().type(Material.REDSTONE_COMPARATOR).name("§%preferences%§").lore("§%preferences-lore%§").build(),
				new MenuClickHandler() {
					@Override
					public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
						// TODO Open Preferences menu
					}
				}));
		builder = new ItemBuilder().type(Material.COMPASS).name("§%location%§");
		loreBuilder = new StringBuilder();
		loreBuilder.append("\n§%country%§: " + battlePlayer.getCountry());
		if(battlePlayer.getUniqueId() == opener.getUniqueId() || opener.hasGroupPermission(Group.TRIAL)) {
			loreBuilder.append("\n§%region%§: " + battlePlayer.getRegion());
			loreBuilder.append("\n§%city%§: " + battlePlayer.getCity());
		}
		menu.setItem(38, builder.lore(loreBuilder.toString()).build());
		
		

		ItemStack nullItem = new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(15).name(" ").build();
		for (int i = 0; i < menu.getInventory().getSize(); i++) {
			if (menu.getItem(i) == null)
				menu.setItem(i, nullItem);
		}
		menu.open(player);
	}

}
