package br.com.battlebits.commons.bukkit.menu.account;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
import br.com.battlebits.commons.core.punish.Ban;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.util.DateUtils;

public class BanMenu {
	private static int itemsPerPage = 21;

	public BanMenu(Player player, BattlePlayer opener, BattlePlayer battlePlayer, MenuInventory topInventory,
			int page) {
		List<Ban> banList = battlePlayer.getPunishHistoric().getBanHistory();
		Collections.sort(banList, new Comparator<Ban>() {

			@Override
			public int compare(Ban o1, Ban o2) {
				return (int) (o1.getBanTime() - o2.getBanTime());
			}
		});
		MenuInventory menu = new MenuInventory(
				"§%ban-list%§ [" + page + "/" + ((int) Math.ceil(banList.size() / itemsPerPage) + 1) + "]", 6, true);
		ItemStack nullItem = new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(15).name(" ").build();
		menu.setItem(0,
				new MenuItem(new ItemBuilder().type(Material.BED).name("§%back%§").build(), new MenuClickHandler() {
					@Override
					public void onClick(Player arg0, Inventory arg1, ClickType arg2, ItemStack arg3, int arg4) {
						if (topInventory != null)
							topInventory.open(arg0);
						else
							arg0.closeInventory();
					}
				}));

		menu.setItem(4, new ItemBuilder().type(Material.BARRIER).name("§%ban-list%§").build());

		int pageStart = 0;
		int pageEnd = itemsPerPage;
		if (page > 1) {
			pageStart = ((page - 1) * itemsPerPage);
			pageEnd = (page * itemsPerPage);
		}
		if (pageEnd > banList.size()) {
			pageEnd = banList.size();
		}
		if (page == 1) {
			menu.setItem(27,
					new ItemBuilder().type(Material.INK_SACK).durability(8).name("§%page-last-dont-have%§").build());
		} else {
			menu.setItem(27,
					new MenuItem(new ItemBuilder().type(Material.INK_SACK).durability(10).name("§%page-last-page%§")
							.lore(Arrays.asList("§%page-last-click-here%§")).build(), new MenuClickHandler() {
								@Override
								public void onClick(Player arg0, Inventory arg1, ClickType arg2, ItemStack arg3,
										int arg4) {
									new BanMenu(arg0, opener, battlePlayer, topInventory, page - 1);
								}
							}));
		}

		if (Math.ceil(banList.size() / itemsPerPage) + 1 > page) {
			menu.setItem(35,
					new MenuItem(new ItemBuilder().type(Material.INK_SACK).durability(10).name("§%page-next-page%§")
							.lore(Arrays.asList("§%page-next-click-here%§")).build(), new MenuClickHandler() {
								@Override
								public void onClick(Player arg0, Inventory arg1, ClickType arg2, ItemStack arg3,
										int arg4) {
									new BanMenu(arg0, opener, battlePlayer, topInventory, page + 1);
								}
							}));
		} else {
			menu.setItem(35,
					new ItemBuilder().type(Material.INK_SACK).durability(8).name("§%page-next-dont-have%§").build());
		}
		int w = 19;
		for (int i = pageStart; i < pageEnd; i++) {
			Ban ban = banList.get(i);
			Material type = Material.STONE;
			String name = "";
			String lore = "";
			if (ban.isPermanent()) {
				if (!ban.isUnbanned()) {
					name = "§%perm-ban%§";
					lore = "permban-lore";
					type = Material.REDSTONE_BLOCK;
				} else {
					name = "§%unbanned-ban%§";
					lore = "unbanned-lore";
					type = Material.DIAMOND_BLOCK;
				}
			} else {
				if (ban.isUnbanned()) {
					if (!ban.getUnbannedBy().equals("CONSOLE")) {
						name = "§%unbanned-ban%§";
						lore = "unbanned-lore";
						type = Material.DIAMOND_BLOCK;
					} else {
						lore = "expiredban-lore";
						name = "§%expired-ban%§";
					}
				} else {
					lore = "tempban-lore";
					name = "§%tempban-ban%§";
					type = Material.LAPIS_BLOCK;
				}

			}
			Date date = new Date(ban.getBanTime());
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:SS");
			Date unban = new Date(ban.getUnbanTime());
			lore = T.t(opener.getLanguage(), lore,
					new String[] { "%bannedBy%", "%bannedIp%", "%banTime%", "%duration%", "%expire%", "%reason%",
							"%server%", "%unbannedBy%", "%unbanTime%" },
					new String[] { ban.getBannedBy(), ban.getBannedBy(), df.format(date), ban.getDuration() + "",
							DateUtils.formatDifference(opener.getLanguage(),
									(ban.getExpire() - System.currentTimeMillis()) / 1000),
							ban.getReason(), ban.getServer(), ban.getUnbannedBy(), df.format(unban) });
			ItemStack item = new ItemBuilder().type(type)//
					.name(name)//
					.lore(lore).build();
			menu.setItem(w, item);
			if (w % 9 == 7) {
				w += 3;
				continue;
			}
			w += 1;
		}
		for (int i = pageEnd - pageStart; i < itemsPerPage; i++) {
			menu.setItem(w, nullItem);
			if (w % 9 == 7) {
				w += 3;
				continue;
			}
			w += 1;
		}
		if (banList.size() == 0) {
			menu.setItem(31, new ItemBuilder().type(Material.PAINTING).name("§c§lOps!")
					.lore(Arrays.asList("§%nothing-found%§")).build());
		}

		for (int i = 0; i < 9; i++) {
			if (menu.getItem(i) == null)
				menu.setItem(i, nullItem);
		}
		menu.open(player);
	}

}
