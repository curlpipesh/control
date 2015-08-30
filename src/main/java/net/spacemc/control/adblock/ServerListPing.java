package net.spacemc.control.adblock;

import com.google.gson.Gson;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * @author audrey
 * @since 8/30/15.
 */
@SuppressWarnings("unused")
public class ServerListPing {
    private InetSocketAddress host;
    private int timeout;
    private Gson gson;

    public ServerListPing() {
        this.timeout = 7000;
        this.gson = new Gson();
    }

    public void setAddress(final InetSocketAddress host) {
        this.host = host;
    }

    public InetSocketAddress getAddress() {
        return this.host;
    }

    void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    int getTimeout() {
        return this.timeout;
    }

    public int readVarInt(final DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        int k;
        do {
            k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if(j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while((k & 0x80) == 0x80);
        return i;
    }

    public void writeVarInt(final DataOutputStream out, int paramInt) throws IOException {
        while((paramInt & 0xFFFFFF80) != 0x0) {
            out.writeByte((paramInt & 0x7F) | 0x80);
            paramInt >>>= 7;
        }
        out.writeByte(paramInt);
    }

    public StatusResponse fetchData() throws IOException {
        try(Socket socket = new Socket()) {
            socket.setSoTimeout(this.timeout);
            socket.connect(this.host, this.timeout);
            final OutputStream outputStream = socket.getOutputStream();
            final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            final InputStream inputStream = socket.getInputStream();
            final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            final ByteArrayOutputStream b = new ByteArrayOutputStream();
            final DataOutputStream handshake = new DataOutputStream(b);
            handshake.writeByte(0);
            this.writeVarInt(handshake, 4);
            this.writeVarInt(handshake, this.host.getHostString().length());
            handshake.writeBytes(this.host.getHostString());
            handshake.writeShort(this.host.getPort());
            this.writeVarInt(handshake, 1);
            this.writeVarInt(dataOutputStream, b.size());
            dataOutputStream.write(b.toByteArray());
            dataOutputStream.writeByte(1);
            dataOutputStream.writeByte(0);
            final DataInputStream dataInputStream = new DataInputStream(inputStream);
            final int size = this.readVarInt(dataInputStream);
            int id = this.readVarInt(dataInputStream);
            if(id == -1) {
                throw new IOException("Premature end of stream.");
            }
            if(id != 0) {
                throw new IOException("Invalid packetID");
            }
            final int length = this.readVarInt(dataInputStream);
            if(length == -1) {
                throw new IOException("Premature end of stream.");
            }
            if(length == 0) {
                throw new IOException("Invalid string length.");
            }
            final byte[] in = new byte[length];
            dataInputStream.readFully(in);
            final String json = new String(in);
            final long now = System.currentTimeMillis();
            dataOutputStream.writeByte(9);
            dataOutputStream.writeByte(1);
            dataOutputStream.writeLong(now);
            this.readVarInt(dataInputStream);
            id = this.readVarInt(dataInputStream);
            if(id == -1) {
                throw new IOException("Premature end of stream.");
            }
            if(id != 1) {
                throw new IOException("Invalid packetID");
            }
            final long pingtime = dataInputStream.readLong();
            final StatusResponse response = (StatusResponse) this.gson.fromJson(json, (Class) StatusResponse.class);
            response.setTime((int) (now - pingtime));
            dataOutputStream.close();
            outputStream.close();
            inputStreamReader.close();
            inputStream.close();
            socket.close();
            return response;
        }
    }

    public class StatusResponse {
        private String description;
        private Players players;
        private Version version;
        private String favicon;
        private int time;

        public String getDescription() {
            return this.description;
        }

        public Players getPlayers() {
            return this.players;
        }

        public Version getVersion() {
            return this.version;
        }

        public String getFavicon() {
            return this.favicon;
        }

        public int getTime() {
            return this.time;
        }

        public void setTime(final int time) {
            this.time = time;
        }
    }

    public class Players {
        private int max;
        private int online;
        private List<Player> sample;

        public int getMax() {
            return this.max;
        }

        public int getOnline() {
            return this.online;
        }

        public List<Player> getSample() {
            return this.sample;
        }
    }

    public class Player {
        private String name;
        private String id;

        public String getName() {
            return this.name;
        }

        public String getId() {
            return this.id;
        }
    }

    public class Version {
        private String name;
        private String protocol;

        public String getName() {
            return this.name;
        }

        public String getProtocol() {
            return this.protocol;
        }
    }
}
