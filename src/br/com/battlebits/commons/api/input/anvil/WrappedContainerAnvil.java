package br.com.battlebits.commons.api.input.anvil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.AbstractWrapper;
import com.comphenix.protocol.wrappers.BlockPosition;

import br.com.battlebits.commons.util.reflection.Reflection;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class WrappedContainerAnvil extends AbstractWrapper {

	private static final Class<?> CONTAINER_ANVIL = MinecraftReflection.getMinecraftClass("ContainerAnvil");
	private static final Class<?> PLAYER_INVENTORY = MinecraftReflection.getMinecraftClass("PlayerInventory");
	private static final Class<?> BLOCK_POSITION = MinecraftReflection.getBlockPositionClass();
	private static final Class<?> ENTITY_PLAYER = MinecraftReflection.getEntityPlayerClass();
	private static final Class<?> ENTITY_HUMAN = MinecraftReflection.getEntityHumanClass();
	private static final Class<?> WORLD = MinecraftReflection.getNmsWorldClass();

	public WrappedContainerAnvil(Object anvilContainer) {
		super(CONTAINER_ANVIL);
		setHandle(anvilContainer);
	}

	public WrappedContainerAnvil(Player player) {
		super(CONTAINER_ANVIL);

		try {
			Method getHandle = MinecraftReflection.getCraftPlayerClass().getMethod("getHandle");
			Object entityPlayer = getHandle.invoke(player);
			Object inventory = Reflection.getField(ENTITY_PLAYER, "inventory").get(entityPlayer);
			Object world = Reflection.getField(ENTITY_PLAYER, "world").get(entityPlayer);
			Object blockPosition = BlockPosition.getConverter().getGeneric(null, BlockPosition.ORIGIN);
			CtClass origClazz = ClassPool.getDefault().get(CONTAINER_ANVIL.getName());
			CtClass subClass = ClassPool.getDefault().makeClass("AnvilNoXPContainer", origClazz);
			CtMethod m = CtNewMethod.make("public boolean a(net.minecraft.server."
					+ MinecraftReflection.getPackageVersion() + ".EntityHuman entityhuman) {return true; }", subClass);
			subClass.addMethod(m);
			Class<?> clazz = subClass.toClass();

			ConstructorAccessor CREATE = Accessors.getConstructorAccessorOrNull(clazz, PLAYER_INVENTORY, WORLD,
					BLOCK_POSITION, ENTITY_HUMAN);
			Object anvilContainer = CREATE.invoke(inventory, world, blockPosition, entityPlayer);
			setHandle(anvilContainer);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NotFoundException | CannotCompileException e) {
			e.printStackTrace();
		}
	}

	public InventoryView getBukkitView() {
		try {
			return (InventoryView) getHandle().getClass().getMethod("getBukkitView").invoke(getHandle());
		} catch (Exception e) {
			return null;
		}
	}

}
