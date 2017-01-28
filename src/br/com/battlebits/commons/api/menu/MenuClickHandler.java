package br.com.battlebits.commons.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public interface MenuClickHandler {

	public void onClick(Player p, Inventory inv, ClickType type, ItemStack stack, int slot);

}
