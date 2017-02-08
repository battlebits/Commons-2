package br.com.battlebits.commons.api.vanish;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.bukkit.event.vanish.PlayerHideToPlayerEvent;
import br.com.battlebits.commons.bukkit.event.vanish.PlayerShowToPlayerEvent;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.permission.Group;

public class VanishAPI {
	private HashMap<UUID, Group> vanishedToGroup;

	private final static VanishAPI instance = new VanishAPI();

	public VanishAPI() {
		vanishedToGroup = new HashMap<>();
	}

	public void setPlayerVanishToGroup(Player player, Group group) {
		if (group == null)
			vanishedToGroup.remove(player.getUniqueId());
		else
			vanishedToGroup.put(player.getUniqueId(), group);
		for (Player online : Bukkit.getOnlinePlayers()) {
			if (online.getUniqueId().equals(player.getUniqueId()))
				continue;
			BattlePlayer onlineP = BattlebitsAPI.getAccountCommon().getBattlePlayer(online.getUniqueId());
			if (group != null && onlineP.getServerGroup().ordinal() <= group.ordinal()) {
				PlayerHideToPlayerEvent event = new PlayerHideToPlayerEvent(player, online);
				Bukkit.getPluginManager().callEvent(event);
				if (event.isCancelled()) {
					if (!online.canSee(player))
						online.showPlayer(player);
				} else if (online.canSee(player))
					online.hidePlayer(player);
				continue;
			}
			PlayerShowToPlayerEvent event = new PlayerShowToPlayerEvent(player, online);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				if (online.canSee(player))
					online.hidePlayer(player);
			} else if (!online.canSee(player))
				online.showPlayer(player);
		}
	}

	public void updateVanishToPlayer(Player player) {
		BattlePlayer bP = BattlebitsAPI.getAccountCommon().getBattlePlayer(player.getUniqueId());
		for (Player online : Bukkit.getOnlinePlayers()) {
			if (online.getUniqueId().equals(player.getUniqueId()))
				continue;
			Group group = vanishedToGroup.get(online.getUniqueId());
			if (group != null) {
				if (bP.getServerGroup().ordinal() <= group.ordinal()) {
					PlayerHideToPlayerEvent event = new PlayerHideToPlayerEvent(online, player);
					Bukkit.getPluginManager().callEvent(event);
					if (event.isCancelled()) {
						if (!player.canSee(online))
							player.showPlayer(online);
					} else if (player.canSee(online))
						player.hidePlayer(online);
					continue;
				}
			}
			PlayerShowToPlayerEvent event = new PlayerShowToPlayerEvent(online, player);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				if (player.canSee(online))
					player.hidePlayer(online);
			} else if (!player.canSee(online))
				player.showPlayer(online);
		}
	}

	public Group hidePlayer(Player player) {
		BattlePlayer bP = BattlebitsAPI.getAccountCommon().getBattlePlayer(player.getUniqueId());
		setPlayerVanishToGroup(player, bP.getServerGroup());
		return bP.getServerGroup().ordinal() - 1 >= 0 ? Group.values()[bP.getServerGroup().ordinal() - 1]
				: Group.NORMAL;
	}

	public void showPlayer(Player player) {
		setPlayerVanishToGroup(player, null);
	}

	public void updateVanish(Player player) {
		setPlayerVanishToGroup(player, getVanishedToGroup(player.getUniqueId()));
	}

	public Group getVanishedToGroup(UUID uuid) {
		return vanishedToGroup.get(uuid);
	}

	public void removeVanish(Player p) {
		vanishedToGroup.remove(p.getUniqueId());
	}

	public static VanishAPI getInstance() {
		return instance;
	}
}
