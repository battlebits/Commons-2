package br.com.battlebits.commons.core.backend.redis;

import java.util.logging.Level;

import br.com.battlebits.commons.BattlebitsAPI;
import lombok.AllArgsConstructor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

@AllArgsConstructor
public class PubSubListener implements Runnable {
	private JedisPubSub jpsh;

	@Override
	public void run() {
		boolean broken = false;
		try (Jedis rsc = BattlebitsAPI.getRedis().getPool().getResource()) {
			try {
				rsc.subscribe(jpsh, "account-field");
			} catch (Exception e) {
				BattlebitsAPI.getLogger().log(Level.INFO, "PubSub error, attempting to recover.", e);
				try {
					jpsh.unsubscribe();
				} catch (Exception e1) {

				}
				broken = true;
			}
		}

		if (broken) {
			run();
		}
	}

	public void addChannel(String... channel) {
		jpsh.subscribe(channel);
	}

	public void removeChannel(String... channel) {
		jpsh.unsubscribe(channel);
	}

	public void poison() {
		jpsh.unsubscribe();
	}
}
