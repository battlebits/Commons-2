package br.com.battlebits.commons.core.data.payload;

import br.com.battlebits.commons.core.server.ServerType;
import br.com.battlebits.commons.core.server.loadbalancer.server.BattleServer;
import br.com.battlebits.commons.core.server.loadbalancer.server.MinigameState;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Arquivo criado em 27/05/2017.
 * Desenvolvido por:
 *
 * @author Luãn Pereira.
 */
@Getter
@RequiredArgsConstructor
public class DataServerMessage<T> {

    private final String source;
    private final ServerType serverType;
    private final Action action;
    private final T payload;

    public enum Action {
        START, STOP, UPDATE, JOIN_ENABLE, JOIN, LEAVE,
    }

    @Getter
    @RequiredArgsConstructor
    public static class StartPayload {
        private final String serverAddress;
        private final BattleServer server;
    }

    @Getter
    @RequiredArgsConstructor
    public static class StopPayload {
        private final String serverId;
    }

    @Getter
    @RequiredArgsConstructor
    public static class UpdatePayload {
        private final int time;
        private final MinigameState state;
    }

    @Getter
    @RequiredArgsConstructor
    public static class JoinEnablePayload {
        private final boolean enable;
    }

    @Getter
    @RequiredArgsConstructor
    public static class JoinPayload {
        private final UUID uniqueId;
    }

    @Getter
    @RequiredArgsConstructor
    public static class LeavePayload {
        private final UUID uniqueId;
    }
}
