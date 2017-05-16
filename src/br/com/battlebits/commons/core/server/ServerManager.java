package br.com.battlebits.commons.core.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.core.server.loadbalancer.BaseBalancer;
import br.com.battlebits.commons.core.server.loadbalancer.server.BattleServer;
import br.com.battlebits.commons.core.server.loadbalancer.server.HungerGamesServer;
import br.com.battlebits.commons.core.server.loadbalancer.server.MinigameServer;
import br.com.battlebits.commons.core.server.loadbalancer.server.MinigameState;
import br.com.battlebits.commons.core.server.loadbalancer.type.LeastConnection;
import br.com.battlebits.commons.core.server.loadbalancer.type.MostConnection;

public class ServerManager {

	private Map<String, BattleServer> activeServers;

	private HashMap<ServerType, BaseBalancer<BattleServer>> balancers;

	public ServerManager() {
		balancers = new HashMap<>();

		balancers.put(ServerType.LOBBY, new LeastConnection<>());
		balancers.put(ServerType.PVP_FULLIRON, new MostConnection<>());
		balancers.put(ServerType.PVP_SIMULATOR, new MostConnection<>());

		balancers.put(ServerType.CUSTOMHG, new MostConnection<>());
		balancers.put(ServerType.DOUBLEKITHG, new MostConnection<>());
		balancers.put(ServerType.HUNGERGAMES, new MostConnection<>());

		activeServers = new HashMap<>();
	}

	public BaseBalancer<BattleServer> getBalancer(ServerType type) {
		return balancers.get(type);
	}

	public void putBalancer(ServerType type, BaseBalancer<BattleServer> balancer) {
		balancers.put(type, balancer);
	}

	public void addActiveServer(String serverAddress, String serverIp, ServerType type, int maxPlayers) {
		BattlebitsAPI.getLogger().info("Battlebits Server carregado. ServerId: " + serverIp);
		updateActiveServer(serverIp, type, new HashSet<>(), maxPlayers, true);
	}

	public void updateActiveServer(String serverId, ServerType type, Set<UUID> onlinePlayers, int maxPlayers, boolean canJoin) {
		updateActiveServer(serverId, type, onlinePlayers, maxPlayers, canJoin, 0, null);
	}

	public void updateActiveServer(String serverId, ServerType type, Set<UUID> onlinePlayers, int maxPlayers, boolean canJoin, int tempo, MinigameState state) {
		BattleServer server = activeServers.get(serverId);
		if (server == null) {
			if (type == ServerType.HUNGERGAMES || type == ServerType.DOUBLEKITHG || type == ServerType.FAIRPLAY || type == ServerType.CUSTOMHG) {
				server = new HungerGamesServer(serverId, type, onlinePlayers, true);
			} else {
				server = new BattleServer(serverId, type, onlinePlayers, maxPlayers, true);
			}
			activeServers.put(serverId.toLowerCase(), server);
		}
		server.setOnlinePlayers(onlinePlayers);
		server.setJoinEnabled(canJoin);
		if (state != null && server instanceof MinigameServer) {
			((MinigameServer) server).setState(state);
			((MinigameServer) server).setTime(tempo);
		}
		addToBalancers(serverId, server);
	}

	public BattleServer getServer(String str) {
		if (!activeServers.containsKey(str.toLowerCase()))
			return null;
		return activeServers.get(str.toLowerCase());
	}

	public void removeActiveServer(String str) {
		if (getServer(str) != null)
			removeFromBalancers(getServer(str));
		activeServers.remove(str.toLowerCase());
	}

	public void addToBalancers(String serverId, BattleServer server) {
		serverId = serverId.toLowerCase();
		BaseBalancer<BattleServer> balancer = getBalancer(server.getServerType());
		if (balancer == null)
			return;
		if (server.getServerType() == ServerType.HUNGERGAMES || server.getServerType() == ServerType.DOUBLEKITHG || server.getServerType() == ServerType.FAIRPLAY || server.getServerType() == ServerType.CUSTOMHG) {
			balancer.add(serverId, (HungerGamesServer) server);
		} else {
			balancer.add(serverId, server);
		}
	}

	public void removeFromBalancers(BattleServer serverId) {
		BaseBalancer<BattleServer> balancer = getBalancer(serverId.getServerType());
		if (balancer != null)
			balancer.remove(serverId.getServerId());
	}

}
