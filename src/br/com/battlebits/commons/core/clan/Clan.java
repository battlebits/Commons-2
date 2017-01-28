package br.com.battlebits.commons.core.clan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.data.DataClan;
import br.com.battlebits.commons.core.permission.Group;
import lombok.Getter;

@Getter
public class Clan {
	private UUID uniqueId;
	private String name;
	private String abbreviation;
	private ClanRank rank;
	private int xp = 0;
	private UUID owner;
	private Set<UUID> administrators;
	private HashMap<UUID, String> participants;
	private Set<UUID> invites;
	private Set<UUID> vips;
	private transient long cacheExpire;
	private transient long confirmDisband = Long.MAX_VALUE;

	
	public Clan(String name, String abbreviation, BattlePlayer owner) {
		this.uniqueId = DataClan.getNewUniqueId();
		this.owner = owner.getUniqueId();
		this.name = name;
		this.abbreviation = abbreviation;
		this.rank = ClanRank.INITIAL;
		participants = new HashMap<>();
		administrators = new HashSet<>();
		invites = new HashSet<>();
		vips = new HashSet<>();
		participants.put(owner.getUniqueId(), owner.getName());
		administrators.add(owner.getUniqueId());
		owner.setClanUniqueId(uniqueId);
	}
	
	public boolean isCacheExpired() {
		return System.currentTimeMillis() > cacheExpire;
	}

	public void updateCache() {
		this.cacheExpire = System.currentTimeMillis() + (60 * 5 * 1000);
	}

	public String getPlayerName(UUID uuid) {
		return participants.get(uuid);
	}

	public void addXp(int xp) {
		if (xp < 0)
			xp = 0;
		this.xp += xp;
		updateStatus();
	}

	public void changeAbbreviation(String str) {
		abbreviation = str;
		updateStatus();
	}

	public boolean isOwner(BattlePlayer player) {
		return isOwner(player.getUniqueId());
	}

	public boolean isOwner(UUID uuid) {
		return owner.equals(uuid);
	}

	public boolean isAdministrator(BattlePlayer player) {
		return isAdministrator(player.getUniqueId());
	}

	public boolean isAdministrator(UUID uuid) {
		return administrators.contains(uuid);
	}

	public boolean isParticipant(BattlePlayer player) {
		return isParticipant(player.getUniqueId());
	}

	public boolean isParticipant(UUID uuid) {
		return participants.containsKey(uuid);
	}

	public boolean isInvited(BattlePlayer player) {
		return isInvited(player.getUniqueId());
	}

	public boolean isInvited(UUID uuid) {
		return invites.contains(uuid);
	}

	public boolean confirm() {
		if (System.currentTimeMillis() < confirmDisband + 60000)
			return true;
		confirmDisband = System.currentTimeMillis();
		return false;
	}

	public boolean promote(UUID uuid) {
		if (!participants.containsKey(uuid))
			return false;
		if (administrators.contains(uuid))
			return false;
		administrators.add(uuid);
		updateStatus();
		return true;
	}

	public boolean demote(UUID uuid) {
		if (!participants.containsKey(uuid))
			return false;
		if (!administrators.contains(uuid))
			return false;
		administrators.remove(uuid);
		updateStatus();
		return true;
	}

	public boolean updatePlayer(BattlePlayer player) {
		participants.put(player.getUniqueId(), player.getName());
		if (player.hasGroupPermission(Group.LIGHT)) {
			if (!vips.contains(player.getUniqueId()))
				vips.add(player.getUniqueId());
			return false;
		} else {
			if (vips.contains(player.getUniqueId()))
				vips.remove(player.getUniqueId());
		}
		if (getSlots() >= getParticipants().size())
			return false;
		if (isOwner(player))
			return false;
		UUID uuid = player.getUniqueId();
		if (isAdministrator(player)) {
			ArrayList<UUID> random = new ArrayList<>();
			for (UUID unique : getParticipants().keySet()) {
				if (!isAdministrator(unique)) {
					random.add(unique);
				}
			}
			if (random.size() > 0) {
				int i = random.size() - 1 > 0 ? new Random().nextInt(random.size() - 1) : 0;
				uuid = random.get(i);
			}
			if (uuid == player.getUniqueId())
				demote(uuid);
		}
		removeParticipant(player.getUniqueId());
		updateStatus();
		return true;
	}

	public void addParticipant(BattlePlayer player) {
		invites.remove(player.getUniqueId());
		participants.put(player.getUniqueId(), player.getName());
		player.setClanUniqueId(uniqueId);
		updateStatus();
		if (player.hasGroupPermission(Group.LIGHT)) {
			if (!vips.contains(player.getUniqueId()))
				vips.add(player.getUniqueId());
		}
	}

	public boolean removeParticipant(UUID uuid) {
		if (owner == uuid)
			return false;
		if (administrators.contains(uuid))
			return false;
		participants.remove(uuid);
		vips.remove(uuid);
		updateStatus();
		return true;
	}

	public void invite(BattlePlayer player) {
		if (invites.contains(player.getUniqueId()))
			return;
		invites.add(player.getUniqueId());
	}

	public void removeInvite(UUID uuid) {
		if (!invites.contains(uuid))
			return;
		invites.remove(uuid);
	}

	public int getSlots() {
		return 5 + (2 * rank.ordinal()) + vips.size();
	}

	public void updateStatus() {
		// TODO Update Clan
	}

}
