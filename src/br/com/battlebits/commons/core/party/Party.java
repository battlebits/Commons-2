package br.com.battlebits.commons.core.party;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import br.com.battlebits.commons.core.account.BattlePlayer;
import lombok.Getter;

@Getter
public class Party {
	private UUID uniqueId;
	private BattlePlayer partyOwner;
	private List<BattlePlayer> participants;

	public Party(UUID uniqueId, BattlePlayer player) {
		this.uniqueId = uniqueId;
		participants = new ArrayList<>();
		participants.add(player);
	}

	public void addParticipant(BattlePlayer player) {
		participants.remove(player);
	}

	public void removeParticipant(BattlePlayer player) {
		participants.remove(player);
	}

}