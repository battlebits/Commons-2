package br.com.battlebits.commons.api.admin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import br.com.battlebits.commons.api.vanish.VanishAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.bukkit.event.admin.PlayerAdminModeEvent;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.permission.Group;
import br.com.battlebits.commons.core.translate.T;

public class AdminMode {
	private Set<UUID> admin;
	private static final AdminMode instance = new AdminMode();

	public AdminMode() {
		admin = new HashSet<UUID>();
	}

	public static AdminMode getInstance() {
		return instance;
	}

	public void setAdmin(Player p) {
		if (!admin.contains(p.getUniqueId()))
			admin.add(p.getUniqueId());
		PlayerAdminModeEvent event = new PlayerAdminModeEvent(p, PlayerAdminModeEvent.AdminMode.ADMIN, GameMode.CREATIVE);
		BukkitMain.getPlugin().getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;
		p.setGameMode(event.getGameMode());
		Group group = VanishAPI.getInstance().hidePlayer(p);
		Map<String, String> map = new HashMap<>();
		map.put("%invisible%", group.toString());
		p.sendMessage("§%command-admin-prefix%§ §%command-admin-enabled%§");
		p.sendMessage("§%command-vanish-prefix%§ " + T.t(BattlePlayer.getLanguage(p.getUniqueId()),
				"command-vanish-invisible", new String[] { "%invisible%", group.toString() }));
	}

	public void setPlayer(Player p) {
		PlayerAdminModeEvent event = new PlayerAdminModeEvent(p, PlayerAdminModeEvent.AdminMode.PLAYER, GameMode.SURVIVAL);
		BukkitMain.getPlugin().getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;
		if (admin.contains(p.getUniqueId())) {
			p.sendMessage("§%command-admin-prefix%§ §%command-admin-disabled%§");
			admin.remove(p.getUniqueId());
		}
		p.sendMessage("§%command-vanish-prefix%§ §%command-vanish-visible-all%§");
		p.setGameMode(event.getGameMode());
		VanishAPI.getInstance().showPlayer(p);
	}

	public boolean isAdmin(Player p) {
		return p != null && admin.contains(p.getUniqueId());
	}

	public int playersInAdmin() {
		return admin.size();
	}

	public void removeAdmin(Player p) {
		admin.remove(p.getUniqueId());
	}
}
