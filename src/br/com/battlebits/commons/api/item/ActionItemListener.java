package br.com.battlebits.commons.api.item;

import java.lang.reflect.Constructor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import br.com.battlebits.commons.api.item.ActionItemStack.InteractHandler;

public class ActionItemListener implements Listener {

	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		if (event.getItem() == null)
			return;
		ItemStack stack = event.getItem();
		try {
			if (stack == null || stack.getType() == Material.AIR)
				throw new Exception();
			Constructor<?> caller = MinecraftReflection.getCraftItemStackClass()
					.getDeclaredConstructor(ItemStack.class);
			caller.setAccessible(true);
			ItemStack item = (ItemStack) caller.newInstance(stack);
			NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
			if (!compound.containsKey("interactHandler")) {
				return;
			}
			InteractHandler handler = ActionItemStack.getHandler(compound.getInteger("interactHandler"));
			if (handler == null) {
				throw new NullPointerException("Handler com ID" + compound.getInteger("interactHandler") + " nulo");
			}
			Player player = event.getPlayer();
			Action action = event.getAction();
			handler.onInteract(player, item, action);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
