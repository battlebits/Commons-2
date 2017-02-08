package br.com.battlebits.commons.bungee.listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bungee.BungeeMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.permission.Group;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class MultiserverTeleport implements Listener {

	@EventHandler
	public void onChat(ChatEvent event) {
		if (!(event.getSender() instanceof ProxiedPlayer))
			return;
		String[] message = event.getMessage().trim().split(" ");
		String command = message[0].toLowerCase();
		if (!command.startsWith("tp") && !command.startsWith("teleport") && !command.startsWith("teleportar"))
			return;
		BattlePlayer player = BattlePlayer.getPlayer(((ProxiedPlayer) event.getSender()).getUniqueId());
		if (!player.hasGroupPermission(Group.STAFF))
			return;
		String[] args = new String[message.length - 1];
		for (int i = 1; i < message.length; i++) {
			args[i - 1] = message[i];
		}
		if (args.length != 1)
			return;
		String target = args[0];
		ProxiedPlayer tplayer;
		if (target.length() == 32 || target.length() == 36) {
			tplayer = BungeeMain.getPlugin().getProxy().getPlayer(BattlebitsAPI.getUUIDOf(target));
		} else {
			tplayer = BungeeMain.getPlugin().getProxy().getPlayer(target);
		}
		if (tplayer == null)
			return;
		if (BattlePlayer.getPlayer(tplayer.getUniqueId()).getServerConnected().equals(player.getServerConnected()))
			return;
		event.setCancelled(true);
		((ProxiedPlayer) event.getSender())
				.connect(BungeeMain.getPlugin().getProxy().getServerInfo(tplayer.getServer().getInfo().getName()));
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("BungeeTeleport");
		out.writeUTF(tplayer.getUniqueId().toString());
		((ProxiedPlayer) event.getSender()).getServer().sendData("BungeeCord", out.toByteArray());
	}
}
