package br.com.battlebits.commons.core.clan;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

import br.com.battlebits.commons.BattlebitsAPI;

public class ClanCommon {

	/**
	 * Sistema de Clans que irá fazer mais jogadores entrar no Battle para criar
	 * seus próprios clans e continuar a jogar no Battle
	 * 
	 * Sistema de clans do Battle, cada clan possui informações para
	 * rankeamento, etc
	 * 
	 */

	private HashMap<UUID, Clan> clans;

	public ClanCommon() {
		clans = new HashMap<>();
	}

	public void loadClan(Clan clan) {
		clans.put(clan.getUniqueId(), clan);
	}

	public void unloadClan(UUID uniqueId) {
		if (clans.containsKey(uniqueId))
			clans.remove(uniqueId);
		else
			BattlebitsAPI.getLogger().log(Level.SEVERE, "NAO FOI POSSIVEL ENCONTRAR CLAN " + uniqueId.toString());
	}

	public Clan getClan(UUID uniqueId) {
		return clans.get(uniqueId);
	}

	public Clan getClan(String clanName) {
		for (Clan clan : clans.values()) {
			if (clan.getName().equalsIgnoreCase(clanName))
				return clan;
		}
		return null;
	}

	public Collection<Clan> getClans() {
		return clans.values();
	}

}
