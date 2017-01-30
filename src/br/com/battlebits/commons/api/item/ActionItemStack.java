package br.com.battlebits.commons.api.item;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import lombok.Getter;

public class ActionItemStack {

	private static final HashMap<Integer, InteractHandler> handlers = new HashMap<>();

	private static int HANDLER_ID = 0;

	@Getter
	private InteractHandler interactHandler;
	@Getter
	private ItemStack itemStack;

	public ActionItemStack(ItemStack stack, InteractHandler handler) {
		try {
			if (stack == null || stack.getType() == Material.AIR)
				throw new Exception();
			Constructor<?> caller = MinecraftReflection.getCraftItemStackClass()
					.getDeclaredConstructor(ItemStack.class);
			caller.setAccessible(true);
			ItemStack item = (ItemStack) caller.newInstance(stack);
			NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
			compound.put("interactHandler", register(handler));
			itemStack = item;
		} catch (Exception e) {
			e.printStackTrace();
			this.itemStack = stack;
		}
		interactHandler = handler;
	}

	public static int register(InteractHandler handler) {
		if (handlers.containsKey(HANDLER_ID))
			return -1;
		handlers.put(HANDLER_ID, handler);
		++HANDLER_ID;
		return HANDLER_ID - 1;
	}

	public static void unregister(Integer id) {
		handlers.remove(id);
	}

	public static InteractHandler getHandler(Integer id) {
		return handlers.get(id);
	}

	public static interface InteractHandler {

		public void onInteract(Player player, ItemStack item, Action action);
	}
}
