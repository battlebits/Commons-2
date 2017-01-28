package br.com.battlebits.commons.core.account;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import br.com.battlebits.commons.BattlebitsAPI;

public class AccountCommon {

	private HashMap<UUID, BattlePlayer> players;

	public AccountCommon() {
		players = new HashMap<>();
	}

	public void loadBattlePlayer(UUID uuid, BattlePlayer player) {
		if (player.getAccountVersion().ordinal() < BattlebitsAPI.getDefaultAccountVersion().ordinal()) {
			player.setXp(0);
			player.setLeague(League.UNRANKED);
			player.setDoubleXpMultiplier(0);
			player.setAccountVersion(BattlebitsAPI.getDefaultAccountVersion());
		}
		players.put(uuid, player);
	}

	public BattlePlayer getBattlePlayer(Player player) {
		return getBattlePlayer(player.getUniqueId());
	}
	
	public BattlePlayer getBattlePlayer(UUID uuid) {
		if (!players.containsKey(uuid)) {
			return null;
		}
		return players.get(uuid);
	}

	public void unloadBattlePlayer(UUID uuid) {
		if (players.containsKey(uuid))
			players.remove(uuid);
		else
			BattlebitsAPI.getLogger().log(Level.SEVERE, "NAO FOI POSSIVEL ENCONTRAR PLAYER " + uuid.toString());
	}

	public Collection<BattlePlayer> getPlayers() {
		return players.values();
	}
}
