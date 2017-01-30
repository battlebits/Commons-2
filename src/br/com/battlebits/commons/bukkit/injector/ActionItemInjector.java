package br.com.battlebits.commons.bukkit.injector;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import br.com.battlebits.commons.bukkit.BukkitMain;

public class ActionItemInjector implements Injector {

	@Override
	public void inject(BukkitMain plugin) {
		plugin.getProcotolManager()
				.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, //
						PacketType.Play.Server.WINDOW_ITEMS, //
						PacketType.Play.Server.SET_SLOT) {
					@Override
					public void onPacketSending(PacketEvent event) {
						PacketContainer packet = event.getPacket();
						if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {
							for (ItemStack stack : packet.getItemArrayModifier().read(0)) {
								if (stack == null || stack.getType() == Material.AIR) {
									continue;
								}
								try {
									Constructor<?> caller = MinecraftReflection.getCraftItemStackClass()
											.getDeclaredConstructor(ItemStack.class);
									caller.setAccessible(true);
									ItemStack item = (ItemStack) caller.newInstance(stack);
									NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
									if (!compound.containsKey("interactHandler")) {
										return;
									}
									compound.remove("interactHandler");
								} catch (NoSuchMethodException | SecurityException | InstantiationException
										| IllegalAccessException | IllegalArgumentException
										| InvocationTargetException e) {
									e.printStackTrace();
								}
							}
						} else if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {
							ItemStack stack = packet.getItemModifier().read(0);
							if (stack == null || stack.getType() == Material.AIR) {
								return;
							}
							try {
								Constructor<?> caller = MinecraftReflection.getCraftItemStackClass()
										.getDeclaredConstructor(ItemStack.class);
								caller.setAccessible(true);
								ItemStack item = (ItemStack) caller.newInstance(stack);
								NbtCompound compound = (NbtCompound) NbtFactory.fromItemTag(item);
								if (!compound.containsKey("interactHandler")) {
									return;
								}
								compound.remove("interactHandler");
							} catch (NoSuchMethodException | SecurityException | InstantiationException
									| IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
								e.printStackTrace();
							}
						}
					}

				});
	}

}
