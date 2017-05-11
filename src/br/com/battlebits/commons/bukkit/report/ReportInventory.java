package br.com.battlebits.commons.bukkit.report;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.commons.api.menu.ClickType;
import br.com.battlebits.commons.api.menu.MenuClickHandler;
import br.com.battlebits.commons.api.menu.MenuInventory;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.report.ConfirmInventory.ConfirmHandler;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.report.Report;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;

public class ReportInventory {

	public ReportInventory(Player player, Report report) {
		Language lang = BattlePlayer.getLanguage(player.getUniqueId());
		MenuInventory menu = new MenuInventory(T.t(BukkitMain.getInstance(), lang, "report-inv").replace("%reported%", report.getPlayerName()), 54);

		menu.setItem(0, new ItemBuilder().type(Material.BED).name("§%back%§").build(), new MenuClickHandler() {

			@Override
			public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
				new ReportListInventory(p, 1);
			}
		});
		BattlePlayer reportPlayer = BattlePlayer.getPlayer(report.getPlayerUniqueId());
		ItemBuilder builder = new ItemBuilder().type(Material.SKULL_ITEM);
		builder.name(T.t(BukkitMain.getInstance(), lang, "report-info").replace("%reported%", report.getPlayerName()));
		String lore = T.t(BukkitMain.getInstance(), lang, "report-info-lore");
		lore = lore.replace("%reported%", report.getPlayerName());
		lore = lore.replace("%server%", reportPlayer.getServerConnected());
		lore = lore.replace("%rank%", reportPlayer.getTag().getPrefix(lang));
		builder.lore(lore);
		builder.durability(3);
		ItemStack skull = builder.build();
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
		skullMeta.setOwner(report.getPlayerName());
		skull.setItemMeta(skullMeta);
		menu.setItem(22, skull, new MenuClickHandler() {

			@Override
			public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
				// TODO ACCOUNT INVENTORY
			}
		});

		menu.setItem(38, new ItemBuilder().type(Material.COMPASS).name("§%teleport%§").build(), new MenuClickHandler() {

			@Override
			public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
				if (reportPlayer == null || !reportPlayer.isOnline()) {
					new ReportListInventory(p, 1);
					return;
				}
				// TODO TELEPORT
			}
		});
		String listLore = T.t(BukkitMain.getInstance(), lang, "report-list-lore");
		listLore = listLore.replace("%number%", report.getPlayersReason().size() + "");
		listLore = listLore.replace("%lastReason%", report.getLastReport().getReason());
		listLore = listLore.replace("%lastPlayer%", report.getLastReport().getPlayerName());

		menu.setItem(40, new ItemBuilder().type(Material.BOOK_AND_QUILL).name("§%report-list%§").lore(listLore).build(), new MenuClickHandler() {

			@Override
			public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
				new ReportInformationListInventory(player, report, menu, 1);
			}
		});

		menu.setItem(42, new ItemBuilder().type(Material.REDSTONE_BLOCK).name("§%reject-report%§").build(), new MenuClickHandler() {

			@Override
			public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
				new ConfirmInventory(player, "§%remove-report%§", new ConfirmHandler() {

					@Override
					public void onCofirm(boolean confirmed) {
						if (confirmed) {
							if (reportPlayer == null || !reportPlayer.isOnline()) {
								new ReportListInventory(p, 1);
								return;
							}
							report.expire();
							new ReportListInventory(p, 1);
						} else {
							menu.open(p);
						}
					}
				}, menu);
			}
		});

		ItemStack nullItem = new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(15).name(" ").build();
		for (int i = 0; i < 9; i++) {
			if (menu.getItem(i) == null)
				menu.setItem(i, nullItem);
		}
		menu.open(player);
	}

}
