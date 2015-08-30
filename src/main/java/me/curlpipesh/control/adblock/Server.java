package me.curlpipesh.control.adblock;

import lombok.Getter;
import me.curlpipesh.control.Control;

import java.net.InetSocketAddress;

/**
 * @author audrey
 * @since 8/30/15.
 */
public class Server {
    @Getter
    private String ip;
    private int port;
    private String data;
    private Control control;

    public Server(Control control, final String ip, final String port) {
        this.control = control;
        this.ip = ip;
        this.port = Integer.parseInt(port);
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
            if(response.getDescription().contains(control.getConfig().getString("home"))) {
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
