package br.com.battlebits.commons.bukkit.protocol;

import java.lang.reflect.Method;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import br.com.battlebits.commons.bukkit.BukkitMain;
import lombok.Getter;

public class ProtocolHook {
	
	@Getter
	private static boolean viaVersion = false;
	@Getter
	private static boolean protocolSupport = false;
	@Getter
	private static boolean protocolHack = false;

	public static void hook() {
		try {
			Class.forName("protocolsupport.api.ProtocolSupportAPI");
			BukkitMain.getInstance().getLogger().info("ProtocolSupport encontrado!");
			protocolSupport = true;
		} catch (ClassNotFoundException e) {
			String version = Bukkit.getServer().getClass().getPackage().getName().substring(23);
			if (version.equals("v1_7_R4")) {
				try {
					Class.forName("org.spigotmc.ProtocolInjector");
					BukkitMain.getInstance().getLogger().info("ProtocolHack encontrado!");
					protocolHack = true;
				} catch (ClassNotFoundException e2) { }
			}
		}

		try {
			Class.forName("us.myles.ViaVersion.api.Via");
			BukkitMain.getInstance().getLogger().info("ViaVersion encontrado!");
			viaVersion = true;
		} catch (ClassNotFoundException e) { }
	}

	public static ProtocolVersion getVersion(Player player) {
		try {
			if (viaVersion) {
				Class<?> clazz = Class.forName("us.myles.ViaVersion.api.Via");
				Object object = clazz.getDeclaredMethod("getAPI").invoke(null);
				Method method = object.getClass().getMethod("getPlayerVersion", UUID.class);
				return ProtocolVersion.getById((int) method.invoke(object, player.getUniqueId()));
			} else if (protocolSupport) {
				Class<?> clazz = Class.forName("protocolsupport.api.ProtocolSupportAPI");
				Method method = clazz.getDeclaredMethod("getProtocolVersion", Player.class);
				return ProtocolVersion.valueOf((String) method.invoke(null, player));
			} else if (protocolHack) {
				Object handle = player.getClass().getMethod("getHandle").invoke(player);
				Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
				Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);
				return ProtocolVersion.getById((int) networkManager.getClass().getMethod("getVersion").invoke(networkManager));
			}
		} catch (Exception e) { }
		
		return ProtocolVersion.UNKNOWN;
	}
}
