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
import br.com.battlebits.commons.core.punish.Mute;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.commons.util.DateUtils;

public class MuteMenu {
	private static int itemsPerPage = 21;

	public MuteMenu(Player player, BattlePlayer opener, BattlePlayer battlePlayer, MenuInventory topInventory,
			int page) {
		List<Mute> muteList = battlePlayer.getPunishHistoric().getMuteHistory();
		Collections.sort(muteList, new Comparator<Mute>() {

			@Override
			public int compare(Mute o1, Mute o2) {
				return (int) (o1.getMuteTime() - o2.getMuteTime());
			}
		});
		MenuInventory menu = new MenuInventory(
				"§%mute-list%§ [" + page + "/" + ((int) Math.ceil(muteList.size() / itemsPerPage) + 1) + "]", 6, true);
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

		menu.setItem(4, new ItemBuilder().type(Material.BOOK_AND_QUILL).name("§%mute-list%§").build());
		
		int pageStart = 0;
		int pageEnd = itemsPerPage;
		if (page > 1) {
			pageStart = ((page - 1) * itemsPerPage);
			pageEnd = (page * itemsPerPage);
		}
		if (pageEnd > muteList.size()) {
			pageEnd = muteList.size();
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
									new MuteMenu(arg0, opener, battlePlayer, topInventory, page - 1);
								}
							}));
		}

		if (Math.ceil(muteList.size() / itemsPerPage) + 1 > page) {
			menu.setItem(35,
					new MenuItem(new ItemBuilder().type(Material.INK_SACK).durability(10).name("§%page-next-page%§")
							.lore(Arrays.asList("§%page-next-click-here%§")).build(), new MenuClickHandler() {
								@Override
								public void onClick(Player arg0, Inventory arg1, ClickType arg2, ItemStack arg3,
										int arg4) {
									new MuteMenu(arg0, opener, battlePlayer, topInventory, page + 1);
								}
							}));
		} else {
			menu.setItem(35,
					new ItemBuilder().type(Material.INK_SACK).durability(8).name("§%page-next-dont-have%§").build());
		}
		int w = 19;
		for (int i = pageStart; i < pageEnd; i++) {
			Mute mute = muteList.get(i);
			Material type = Material.STONE;
			String name = "";
			String lore = "";
			if (mute.isPermanent()) {
				if (!mute.isUnmuted()) {
					name = "§%perm-mute%§";
					lore = "permmute-lore";
					type = Material.REDSTONE_BLOCK;
				} else {
					name = "§%unmuted-mute%§";
					lore = "unmuted-lore";
					type = Material.DIAMOND_BLOCK;
				}
			} else {
				if (mute.isUnmuted()) {
					if (!mute.getUnmutedBy().equals("CONSOLE")) {
						name = "§%unmuted-mute%§";
						lore = "unmuted-lore";
						type = Material.DIAMOND_BLOCK;
					} else {
						lore = "expiredmute-lore";
						name = "§%expired-mute%§";
					}
				} else {
					lore = "tempmute-lore";
					name = "§%tempmute-mute%§";
					type = Material.LAPIS_BLOCK;
				}

			}
			Date date = new Date(mute.getMuteTime());
			DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:SS");
			Date unmute = new Date(mute.getUnmuteTime());
			lore = T.t(opener.getLanguage(), lore,
					new String[] { "%mutedBy%", "%mutedIp%", "%muteTime%", "%duration%", "%expire%", "%reason%",
							"%server%", "%unmutedBy%", "%unmuteTime%" },
					new String[] { mute.getMutedBy(), mute.getMutedBy(), df.format(date), mute.getDuration() + "",
							DateUtils.formatDifference(opener.getLanguage(),
									(mute.getExpire() - System.currentTimeMillis()) / 1000),
							mute.getReason(), mute.getServer(), mute.getUnmutedBy(), df.format(unmute) });
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
		if (muteList.size() == 0) {
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
