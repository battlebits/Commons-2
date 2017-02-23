package br.com.battlebits.commons.bukkit.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.event.update.UpdateEvent;
import br.com.battlebits.commons.bukkit.event.update.UpdateEvent.UpdateType;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.Translate;

public class AntiAFK implements Listener {
	public Map<UUID, Location> locations = new HashMap<>();
	private static Map<UUID, Integer> afkMap = new HashMap<>();

	public void addTime(Player p) {
		if (!BukkitMain.getInstance().isAntiAfkEnabled())
			return;
		int time = afkMap.getOrDefault(p.getUniqueId(), 0);
		afkMap.put(p.getUniqueId(), ++time);
	}

	public static int getTime(Player p) {		
		return afkMap.getOrDefault(p.getUniqueId(), 0);
	}

	public void resetTimer(Player p) {
		afkMap.remove(p.getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.SECOND)
			return;
		if (!BukkitMain.getInstance().isAntiAfkEnabled())
			return;
		for (Player p : Bukkit.getOnlinePlayers()) {
			BattlePlayer player = BattlebitsAPI.getAccountCommon().getBattlePlayer(p.getUniqueId());
			if (player == null)
				return;
			if (!player.hasGroupPermission(Group.LIGHT)) {
				
				Location loc = locations.get(p.getUniqueId());
				if (loc == null) {
					locations.put(p.getUniqueId(), p.getLocation().clone());
					continue;
				}
				
				Location l = p.getLocation();
				if (loc.getX() == l.getX() && loc.getY() == l.getY() && loc.getZ() == l.getZ()) {
					addTime(p);
				} else {
					resetTimer(p);
				}
				
				int time = getTime(p);
				
				if (time == 240) {
					p.sendMessage("§%antiafk-timer%§");
					continue;
				}
				
				if (time >= 300) {
					p.kickPlayer(Translate.getTranslation(player.getLanguage(), "antiafk-kick"));
					continue;
				}
				
				locations.put(p.getUniqueId(), p.getLocation().clone());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLeave(PlayerQuitEvent e) {
		locations.remove(e.getPlayer().getUniqueId());
		afkMap.remove(e.getPlayer().getUniqueId());
	}

}
