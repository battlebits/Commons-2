package br.com.battlebits.commons.bukkit.messesnger;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.admin.AdminMode;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.core.permission.Group;

public class BungeeCordMessenger implements PluginMessageListener {

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord"))
			return;
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		if (subchannel.equalsIgnoreCase("BungeeTeleport")) {
			String uuidStr = in.readUTF();
			if (!BattlebitsAPI.getAccountCommon().getBattlePlayer(player.getUniqueId())
					.hasGroupPermission(Group.TRIAL)) {
				player.sendMessage("§%command-teleport-no-access%§");
				return;
			}
			AdminMode.getInstance().setAdmin(player);
			UUID uuid = UUID.fromString(uuidStr);
			Player p = BukkitMain.getPlugin().getServer().getPlayer(uuid);
			player.chat("/tp " + p.getName());
		}
	}

}