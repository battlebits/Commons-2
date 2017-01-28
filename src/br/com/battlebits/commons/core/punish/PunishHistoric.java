package br.com.battlebits.commons.core.punish;

import java.util.ArrayList;
import java.util.List;

public class PunishHistoric {
	private List<Ban> banHistory;
	private List<Mute> muteHistory;

	public PunishHistoric() {
		banHistory = new ArrayList<>();
		muteHistory = new ArrayList<>();
	}

	public Ban getActualBan() {
		for (Ban ban : banHistory) {
			if (ban.isUnbanned())
				continue;
			if (ban.hasExpired())
				continue;
			return ban;
		}
		return null;
	}

	public Mute getActualMute() {
		for (Mute mute : muteHistory) {
			if (mute.isUnmuted())
				continue;
			if (mute.hasExpired())
				continue;
			return mute;
		}
		return null;
	}

	public List<Ban> getBanHistory() {
		return banHistory;
	}

	public List<Mute> getMuteHistory() {
		return muteHistory;
	}
}
