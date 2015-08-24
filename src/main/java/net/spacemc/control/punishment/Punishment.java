package net.spacemc.control.punishment;

import lombok.Data;
import lombok.NonNull;
import net.spacemc.control.SpaceControl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author audrey
 * @since 8/23/15.
 */
@Data
public class Punishment {
    private SpaceControl control;
    private int id;
    private String type;
    private UUID issuer;
    private UUID target;
    private String targetIP;
    private String reason;
    private int length;
    private String start;
    private String end;

    public Punishment(@NonNull SpaceControl control, @NonNull int id, @NonNull String type, @NonNull String issuer,
                      @NonNull String target, @NonNull String reason, @NonNull int length, @NonNull String start,
                      @NonNull String end) {
        this.control = control;
        this.id = id;
        this.type = type;
        this.issuer = UUID.fromString(issuer);
        try {
            this.target = UUID.fromString(target);
        } catch(IllegalArgumentException e) {
            control.getLogger().warning("*** Likely from trying to parse an IP as a UUID; you can ignore this ***");
            e.printStackTrace();
            this.target = null;
            this.targetIP = target;
        }
        this.reason = reason;
        this.length = length;
        this.start = start;
        this.end = end;
    }

    public boolean over() {
        try {
            return control.getFormat().parse(end).before(new Date());
        } catch(ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}
