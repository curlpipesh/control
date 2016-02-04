package me.curlpipesh.control.network;

import com.ikeirnez.communicationsframework.api.packets.Packet;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;

/**
 * @author audrey
 * @since 2/3/16.
 */
@Value
@ToString
@RequiredArgsConstructor
public class PunishmentPacket implements Packet {
    private static final long serialVersionUID = 2752354484667658345L;

    private final boolean isIP;
    private final String punishmentType;
    private final String issuer;
    private final String target;
    private final String reason;
    private final int lengthInMinutes;
}
