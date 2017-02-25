package br.com.battlebits.commons.core.party;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class Party {
	@NonNull
	private UUID owner;
	
	private Set<UUID> members = new HashSet<>();
	private Set<UUID> promoted = new HashSet<>();

	public boolean isPromoted(UUID uuid) {
		return uuid.equals(owner) || promoted.contains(uuid);
	}
	
	public boolean contains(UUID uuid) {
		return uuid.equals(owner) || members.contains(uuid);
	}
	
	public void removeMember(UUID member) {
		members.remove(member);
	}
	
	public void addMember(UUID member) {
		members.add(member);
	}

	public abstract void init();
	public abstract void onOwnerJoin();
	public abstract void onOwnerLeave();
	public abstract void onMemberJoin(UUID member);
	public abstract void onMemberLeave(UUID member);
	public abstract void sendMessage(String id, String[]... replace);
}