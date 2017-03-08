package br.com.battlebits.commons.bukkit.util;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.Getter;

public class ProtocolUtils
{
	@Getter
	private static boolean protocolSupport = false;
	
	@Getter
	private static boolean protocolHack = false;
	
	public static void checkServer()
	{
		try
		{
			Class.forName("protocolsupport.api.ProtocolSupportAPI");
			protocolSupport = true;
		}
		catch (ClassNotFoundException e)
		{
			String version = Bukkit.getServer().getClass().getPackage().getName().substring(23);
			
			if (version.equals("v1_7_R4"))
			{
				try
				{
					Class.forName("org.spigotmc.ProtocolInjector");
					protocolHack = true;
				}
				catch (ClassNotFoundException e2) 
				{
					// ignore
				}
			}			
		}
	}
	
	public static String getVersion(Player player)
	{
		if (protocolHack)
		{
			try
			{
				Object handle = player.getClass().getMethod("getHandle").invoke(player);
				Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
				Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);
				int version = (int) networkManager.getClass().getMethod("getVersion").invoke(networkManager);
				
				switch (version) 
				{
				    case 47:
				    	return "MINECRAFT_1_8";
				    case 5:
				    	return "MINECRAFT_1_7_10";
				    case 4:
				    	return "MINECRAFT_1_7_5";
				}
			}
			catch (Exception e) 
			{
				return "UNKNOWN";
			}
		}
		else if (protocolSupport)
		{
			try
			{
				Class<?> clazz = Class.forName("protocolsupport.api.ProtocolSupportAPI");
				Method method = clazz.getDeclaredMethod("getProtocolVersion", Player.class);
				Object object = method.invoke(null, player);
				return object.toString();
			}
			catch (Exception e)
			{
				return "UNKNOWN";
			}
		}
		
		return "UNKNOWN";
	}
}
