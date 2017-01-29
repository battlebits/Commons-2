package br.com.battlebits.commons.core.friend;

import java.util.UUID;

import br.com.battlebits.commons.util.timezone.TimeZone;
import br.com.battlebits.commons.util.timezone.TimeZoneConversor;
import lombok.Getter;

@Getter
public class Blocked {

	private UUID uniqueId;
	private Long blockedTime;

	public Blocked(UUID blockedPlayer) {
		this.blockedTime = TimeZoneConversor.getCurrentMillsTimeIn(TimeZone.GMT0);
		this.uniqueId = blockedPlayer;
	}

}
