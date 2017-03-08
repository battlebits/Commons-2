package br.com.battlebits.commons.bukkit.protocolsupport;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.Getter;

public class ProtocolSupportHook {
	
	@Getter
	private static boolean protocolSupport = false;
	@Getter
	private static boolean protocolHack = false;

	public static void hook() {
		try {
			Class.forName("protocolsupport.api.ProtocolSupportAPI");
			protocolSupport = true;
		} catch (ClassNotFoundException e) {
			String version = Bukkit.getServer().getClass().getPackage().getName().substring(23);
			if (version.equals("v1_7_R4")) {
				try {
					Class.forName("org.spigotmc.ProtocolInjector");
					protocolHack = true;
				} catch (ClassNotFoundException e2) { }
			}
		}
	}

	public static ProtocolVersion getVersion(Player player) {
		if (protocolHack) {
			try {
				Object handle = player.getClass().getMethod("getHandle").invoke(player);
				Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
				Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);
				return ProtocolVersion.getByProtocol((int) networkManager.getClass().getMethod("getVersion").invoke(networkManager));
			} catch (Exception e) {
				return ProtocolVersion.UNKNOWN;
			}
		} else if (protocolSupport) {
			try {
				Class<?> clazz = Class.forName("protocolsupport.api.ProtocolSupportAPI");
				Method method = clazz.getDeclaredMethod("getProtocolVersion", Player.class);
				return ProtocolVersion.valueOf((String) method.invoke(null, player));
			} catch (Exception e) {
				return ProtocolVersion.UNKNOWN;
			}
		}

		return ProtocolVersion.UNKNOWN;
	}
}
