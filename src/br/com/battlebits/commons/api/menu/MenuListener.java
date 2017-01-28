package br.com.battlebits.commons.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public class MenuListener implements Listener{
	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryClickListener(InventoryClickEvent event) {
		if (event.getInventory() == null)
			return;
		Inventory inv = event.getInventory();
		if (inv.getType() != InventoryType.CHEST)
			return;
		if (inv.getHolder() == null)
			return;
		if (!(inv.getHolder() instanceof MenuHolder))
			return;
		if (event.getClickedInventory() != inv)
			return;
		event.setCancelled(true);
		if (!(event.getWhoClicked() instanceof Player))
			return;
		if (event.getSlot() < 0)
			return;
		MenuHolder holder = (MenuHolder) inv.getHolder();
		MenuInventory menu = holder.getMenu();
		if (menu.hasItem(event.getSlot())) {
			Player p = (Player) event.getWhoClicked();
			MenuItem item = menu.getItem(event.getSlot());
			item.getHandler().onClick(p, inv,
					((event.getAction() == InventoryAction.PICKUP_HALF) ? ClickType.RIGHT : ClickType.LEFT),
					event.getCurrentItem(), event.getSlot());
		}
	}

}
