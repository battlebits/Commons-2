package br.com.battlebits.commons.bukkit.injector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;

import br.com.battlebits.commons.api.admin.AdminMode;
import br.com.battlebits.commons.bukkit.BukkitMain;

public class ServerInfoInjector implements Injector {

	@Override
	public void inject(BukkitMain plugin) {
		plugin.getProcotolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, //
				PacketType.Status.Server.SERVER_INFO) {
			@Override
			public void onPacketSending(PacketEvent event) {
				PacketContainer container = event.getPacket();
				WrappedServerPing ping = container.getServerPings().read(0);
				ping.setPlayersOnline(ping.getPlayersOnline() -  AdminMode.getInstance().playersInAdmin());
			}
		});
	}

}
