package br.com.battlebits.commons.util.string;

public class StringHourTime {
	public static String getHourTime(Integer i) {
		int minutes = i / 60;
		int seconds = i % 60;
		String disMinu = (minutes < 10 ? "" : "") + minutes;
		String disSec = (seconds < 10 ? "0" : "") + seconds;
		String formattedTime = disMinu + ":" + disSec;
		return formattedTime;
	}
}
