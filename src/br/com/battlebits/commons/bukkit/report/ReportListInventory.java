package br.com.battlebits.commons.bukkit.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.commons.api.menu.ClickType;
import br.com.battlebits.commons.api.menu.MenuClickHandler;
import br.com.battlebits.commons.api.menu.MenuInventory;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.report.Report;
import net.md_5.bungee.api.ChatColor;

public class ReportListInventory {

	private static int itemsPerPage = 36;

	public ReportListInventory(Player player, int page) {
		List<Report> reports =  new ArrayList<>(); // TODO GetReports
		Iterator<Report> iterator = reports.iterator();
		while (iterator.hasNext()) {
			Report report = iterator.next();
			if (BungeeMain.getPlugin().getProxy().getPlayer(report.getPlayerUniqueId()) == null) {
				iterator.remove();
				continue;
			}
			BattlePlayer reportPlayer = BattlePlayer.getPlayer(report.getPlayerUniqueId());
			if (reportPlayer == null || !reportPlayer.isOnline()) {
				iterator.remove();
				return;
			}
			if (report.isExpired()) {
				iterator.remove();
				continue;
			}
			if (report.getReportLevel() < 1000)
				iterator.remove();
		}
		Collections.sort(reports, new Comparator<Report>() {
			@Override
			public int compare(Report o1, Report o2) {
				if (o1.getLastReportTime() > o2.getLastReportTime())
					return 1;
				else if (o1.getLastReportTime() == o2.getLastReportTime())
					return 0;
				return -1;
			}
		});

		MenuInventory menu = new MenuInventory("§%reports%§", 54);
		// PAGINAÇÃO
		int pageStart = 0;
		int pageEnd = itemsPerPage;
		if (page > 1) {
			pageStart = ((page - 1) * itemsPerPage);
			pageEnd = (page * itemsPerPage);
		}
		if (pageEnd > reports.size()) {
			pageEnd = reports.size();
		}
		if (page == 1) {
			menu.setItem(0, new ItemBuilder().type(Material.INK_SACK).durability(8).name("§%page-last-dont-have%§").build());
		} else {
			menu.setItem(0, new ItemBuilder().type(Material.INK_SACK).durability(10).name("§%page-last-page%§").lore(Arrays.asList("§%page-last-click-here%§")).build(), new MenuClickHandler() {
				@Override
				public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
					new ReportListInventory(player, page - 1);
				}
			});
		}
		if (Math.ceil(reports.size() / itemsPerPage) + 1 > page) {
			menu.setItem(8, new ItemBuilder().type(Material.INK_SACK).durability(10).name("§%page-next-page%§").lore(Arrays.asList("§%page-next-click-here%§")).build(), new MenuClickHandler() {
				@Override
				public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
					new ReportListInventory(player, page + 1);
				}
			});
		} else {
			menu.setItem(8, new ItemBuilder().type(Material.INK_SACK).durability(8).name("§%page-next-dont-have%§").build());
		}

		// REPORT LIST

		int w = 9;

		for (int i = pageStart; i < pageEnd; i++) {
			Report report = reports.get(i);
			ItemBuilder builder = new ItemBuilder();
			builder.type(Material.SKULL_ITEM);
			builder.durability(3);
			builder.name(ChatColor.RED + report.getPlayerName());
			builder.lore("§%right-click-teleport%§");
			ItemStack skull = builder.build();
			SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
			skullMeta.setOwner(report.getPlayerName());
			skull.setItemMeta(skullMeta);
			menu.setItem(w, skull, new ReportClickHandler(report, menu));
			w += 1;
		}
		if (reports.size() == 0) {
			menu.setItem(31, new ItemBuilder().type(Material.PAINTING).name("§c§lOps!").lore(Arrays.asList("§%error%§")).build());
		}

		ItemStack nullItem = new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(15).name(" ").build();
		for (int i = 0; i < 9; i++) {
			if (menu.getItem(i) == null)
				menu.setItem(i, nullItem);
		}
		menu.open(player);
	}

	private static class ReportClickHandler implements MenuClickHandler {

		private Report report;
		private MenuInventory topInventory;

		public ReportClickHandler(Report report, MenuInventory topInventory) {
			this.report = report;
			this.topInventory = topInventory;
		}

		@Override
		public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
			BattlePlayer reportPlayer = BattlePlayer.getPlayer(report.getPlayerUniqueId());
			if (reportPlayer == null || !reportPlayer.isOnline()) {
				new ReportListInventory(p, 1);
				return;
			}
			if (type == ClickType.RIGHT) {
				// TODO TELEPORT
				topInventory.open(p);
				return;
			}
			new ReportInventory(p, report);
		}

	}
}
