package br.com.battlebits.commons.util.timezone;

import br.com.battlebits.commons.BattlebitsAPI;

public class TimeZoneConversor {
	
	public static long getCurrentMillsTimeIn(TimeZone timeZone) {
		return convertTime(BattlebitsAPI.DEFAULT_TIME_ZONE, timeZone, System.currentTimeMillis());
	}

	public static long convertTime(TimeZone fromZone, TimeZone toZone, long time) {
		return time + ((fromZone.getAjust() - toZone.getAjust()) * 1000 * 60 * 60);
	}
}
