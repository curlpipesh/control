package me.curlpipesh.control.network;

import com.ikeirnez.communicationsframework.api.HookType;
import com.ikeirnez.communicationsframework.api.authentication.SimpleConnectionAuthentication;
import com.ikeirnez.communicationsframework.api.config.connection.ClientConnectionConfig;
import com.ikeirnez.communicationsframework.api.connection.ClientConnection;
import com.ikeirnez.communicationsframework.api.connection.ConnectionManager;
import com.ikeirnez.communicationsframework.api.connection.ConnectionManagerFactory;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.net.InetSocketAddress;

/**
 * @author audrey
 * @since 2/3/16.
 */
public class NetworkClient {
    @Getter
    private final ConnectionManager connectionManager = ConnectionManagerFactory.getNewNettyConnectionManager(getClass().getClassLoader());

    @Getter
    private final ClientConnection clientConnection;

    public NetworkClient(final Plugin control, final String host, final String port, final char[] key) {
        connectionManager.addHook(HookType.CONNECTED,
                connection -> control.getLogger().info("Authenticated with message server"));
        connectionManager.addHook(HookType.AUTHENTICATION_FAILED,
                connection -> control.getLogger().info("Couldn't authenticate with message server"));
        connectionManager.addHook(HookType.RECONNECTED,
                connection -> control.getLogger().info("Re-authenticated with message server"));
        connectionManager.addHook(HookType.LOST_CONNECTION,
                connection -> control.getLogger().info("Lost connection with message server"));

        final ClientConnectionConfig config = new ClientConnectionConfig(new InetSocketAddress(host, Integer.parseInt(port)));
        config.setAuthentication(new SimpleConnectionAuthentication(new String(key)));
        // create the connection instance and populate it with our data
        clientConnection = connectionManager.newClientConnection(config);
    }

    public void connect() {
        // attempt connecting asynchronously
        clientConnection.connect();
    }
}
