package net.spacemc.control.adblock;

import java.net.InetSocketAddress;

/**
 * @author audrey
 * @since 8/30/15.
 */
public class Server {
    private String ip;
    private int port;
    private String data;

    public Server(final String ip, final int port) {
        this.ip = ip;
        this.port = port;
    }

    public boolean isOnline() {
        try {
            final ServerListPing ping = new ServerListPing();
            final InetSocketAddress address = new InetSocketAddress(this.ip, this.port);
            ping.setAddress(address);
            ping.setTimeout(500);
            final ServerListPing.StatusResponse response = ping.fetchData();
            if(response == null) {
                return false;
            }
            if(response.getDescription().contains("Curspex")) {
                this.data = "HOME";
                return false;
            }
            this.data = String.format("Ver: %s, Players: %s/%s", response.getVersion().getName(), response.getPlayers().getOnline(), response.getPlayers().getMax());
        } catch(Exception e) {
            return false;
        }
        return true;
    }

    public String getData() {
        return this.data;
    }
}
