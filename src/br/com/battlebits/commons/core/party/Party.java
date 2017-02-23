package br.com.battlebits.commons.core.party;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Party {
	@NonNull
	private UUID owner;
	
	private Set<UUID> members = new HashSet<>();
	
	public boolean contains(UUID uuid) {
		return uuid.equals(owner) || members.contains(uuid);
	}
	
	public void removeMember(UUID member) {
		members.remove(member);
	}
	
	public void addMember(UUID member) {
		members.add(member);
	}
}