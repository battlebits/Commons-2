package br.com.battlebits.commons.bukkit.scheduler;

import org.bukkit.Bukkit;

import br.com.battlebits.commons.bukkit.event.update.UpdateEvent;
import br.com.battlebits.commons.bukkit.event.update.UpdateEvent.UpdateType;

public class UpdateScheduler implements Runnable {

	private long tickCount = 0;
	private long lastSecond = Long.MIN_VALUE;
	private long lastMinute = Long.MIN_VALUE;

	@Override
	public void run() {
		long tick = (tickCount++ % 20) + 1;
		Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateType.TICK, tick));
		if (lastSecond + 1000 <= System.currentTimeMillis()) {
			Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateType.SECOND));
			lastSecond = System.currentTimeMillis();
		}
		if (lastMinute + 60000 <= System.currentTimeMillis()) {
			Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateType.MINUTE));
			lastMinute = System.currentTimeMillis();
		}
	}
}
