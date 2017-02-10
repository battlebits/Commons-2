package br.com.battlebits.commons.bukkit.report;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import br.com.battlebits.commons.api.item.ItemBuilder;
import br.com.battlebits.commons.api.menu.ClickType;
import br.com.battlebits.commons.api.menu.MenuClickHandler;
import br.com.battlebits.commons.api.menu.MenuInventory;

public class ConfirmInventory {

	public ConfirmInventory(Player player, String confirmTitle, ConfirmHandler handler, MenuInventory topInventory) {
		MenuInventory menu = new MenuInventory(confirmTitle, 54);
		ItemStack nullItem = new ItemBuilder().type(Material.STAINED_GLASS_PANE).durability(15).name(" ").build();
		menu.setItem(0, new ItemBuilder().type(Material.BED).name("§%back%§").build(), new MenuClickHandler() {

			@Override
			public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
				if (topInventory != null)
					topInventory.open(p);
				else
					menu.close(p);
			}
		});

		MenuClickHandler yesHandler = new MenuClickHandler() {

			@Override
			public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
				handler.onCofirm(true);
			}
		};

		MenuClickHandler noHandler = new MenuClickHandler() {

			@Override
			public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot) {
				handler.onCofirm(false);
			}
		};

		menu.setItem(20, new ItemBuilder().type(Material.REDSTONE_BLOCK).name("§%deny%§").build(), noHandler);
		menu.setItem(21, new ItemBuilder().type(Material.REDSTONE_BLOCK).name("§%deny%§").build(), noHandler);
		menu.setItem(29, new ItemBuilder().type(Material.REDSTONE_BLOCK).name("§%deny%§").build(), noHandler);
		menu.setItem(30, new ItemBuilder().type(Material.REDSTONE_BLOCK).name("§%deny%§").build(), noHandler);

		menu.setItem(23, new ItemBuilder().type(Material.EMERALD_BLOCK).name("§%confirm%§").build(), yesHandler);
		menu.setItem(24, new ItemBuilder().type(Material.EMERALD_BLOCK).name("§%confirm%§").build(), yesHandler);
		menu.setItem(32, new ItemBuilder().type(Material.EMERALD_BLOCK).name("§%confirm%§").build(), yesHandler);
		menu.setItem(33, new ItemBuilder().type(Material.EMERALD_BLOCK).name("§%confirm%§").build(), yesHandler);

		for (int i = 0; i < 9; i++) {
			if (menu.getItem(i) == null)
				menu.setItem(i, nullItem);
		}

		for (int i = 45; i < 54; i++) {
			if (menu.getItem(i) == null)
				menu.setItem(i, nullItem);
		}
		menu.open(player);
	}

	public static interface ConfirmHandler {
		public void onCofirm(boolean confirmed);
	}
}
