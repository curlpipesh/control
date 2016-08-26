package lgbt.audrey.control.adblock;

import lombok.Getter;
import lgbt.audrey.control.Control;
import lgbt.audrey.control.adblock.ServerListPing.StatusResponse;

import java.net.InetSocketAddress;

/**
 * Not actually mine. Used for reading information about a remote MC server.
 *
 * @author unknown
 * @since 8/30/15.
 */
public class Server {
    @Getter
    private final String ip;
    private final int port;
    private String data;
    private final Control control;

    public Server(final Control control, final String ip, final String port) {
        this.control = control;
        this.ip = ip;
        this.port = Integer.parseInt(port);
    }

    public boolean isOnline() {
        try {
            final ServerListPing ping = new ServerListPing();
            final InetSocketAddress address = new InetSocketAddress(ip, port);
            ping.setAddress(address);
            ping.setTimeout(500);
            final StatusResponse response = ping.fetchData();
            if(response == null) {
                return false;
            }
            if(response.getDescription().contains(control.getConfig().getString("home"))) {
                data = "HOME";
                return false;
            }
            data = String.format("Ver: %s, Players: %s/%s", response.getVersion().getName(), response.getPlayers().getOnline(), response.getPlayers().getMax());
        } catch(final Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SuppressWarnings("unused")
    public String getData() {
        return data;
    }
}
