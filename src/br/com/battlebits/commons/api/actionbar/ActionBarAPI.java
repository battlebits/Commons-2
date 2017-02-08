package br.com.battlebits.commons.api.actionbar;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import br.com.battlebits.commons.bukkit.BukkitMain;

public class ActionBarAPI {

	public static void send(Player player, String text) {
		PacketContainer chatPacket = new PacketContainer(PacketType.Play.Server.CHAT);
		chatPacket.getChatComponents().write(0, WrappedChatComponent.fromJson("{\"text\":\"" + text + " \"}"));
		chatPacket.getBytes().write(0, (byte) 2);
		try {
			BukkitMain.getInstance().getProcotolManager().sendServerPacket(player, chatPacket);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Cannot send packet " + chatPacket, e);
		}
	}
}
