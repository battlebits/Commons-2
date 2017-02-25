package br.com.battlebits.commons.core.party;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PartyCommon {
	private Map<UUID, Party> partys;
	
	public PartyCommon() {
		this.partys = new HashMap<>();
	}

	public boolean inParty(UUID uuid) {
		return getParty(uuid) != null;
	}
	
	public Party getParty(UUID uuid) {
		return partys.values().stream().filter(p -> p.contains(uuid)).findFirst().orElse(null);
	}
	
	public Party getByOwner(UUID owner) {
		return partys.values().stream().filter(p -> p.getOwner().equals(owner)).findFirst().orElse(null);
	}
	
	public Party loadParty(Party party) {
		return partys.computeIfAbsent(party.getOwner(), v -> party);
	}
	
	public void removeParty(Party party) {
		partys.remove(party.getOwner());
	}
	
	public void removeParty(UUID owner) {
		partys.remove(owner);
	}

	public Collection<Party> getPartys() {
		return partys.values();
	}
}
