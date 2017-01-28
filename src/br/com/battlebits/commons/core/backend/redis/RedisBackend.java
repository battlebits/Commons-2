package br.com.battlebits.commons.core.backend.redis;

import br.com.battlebits.commons.core.backend.Backend;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@RequiredArgsConstructor
public class RedisBackend implements Backend {
	@NonNull
	private final String hostname, password;
	private final int port;

	@Getter
	private JedisPool pool;

	public RedisBackend() {
		this("localhost", "", 6379);
	}

	@Override
	public void startConnection() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(8);
		if (!password.isEmpty())
			pool = new JedisPool(config, hostname, port, 0, password);
		else
			pool = new JedisPool(config, hostname, port, 0);
	}

	@Override
	public void closeConnection() {
		if (pool != null) {
			pool.destroy();
		}
	}

	@Override
	public boolean isConnected() {
		return !pool.isClosed();
	}

	@Override
	@Deprecated
	public void recallConnection() throws Exception {
	}

}
