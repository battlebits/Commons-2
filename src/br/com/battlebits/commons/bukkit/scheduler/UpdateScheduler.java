package br.com.battlebits.commons.bukkit.scheduler;

import org.bukkit.Bukkit;

import br.com.battlebits.commons.bukkit.event.update.UpdateEvent;
import br.com.battlebits.commons.bukkit.event.update.UpdateEvent.UpdateType;

public class UpdateScheduler implements Runnable {

	private long currentTick = 0;
	private long lastSecond = Long.MIN_VALUE;
	private long lastMinute = Long.MIN_VALUE;

	@Override
	public void run() {
		currentTick++;		
		Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateType.TICK, currentTick));
		if (lastSecond + 1000 <= System.currentTimeMillis()) {
			Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateType.SECOND, currentTick));
			lastSecond = System.currentTimeMillis();
		}
		if (lastMinute + 60000 <= System.currentTimeMillis()) {
			Bukkit.getPluginManager().callEvent(new UpdateEvent(UpdateType.MINUTE, currentTick));
			lastMinute = System.currentTimeMillis();
		}
	}
}
