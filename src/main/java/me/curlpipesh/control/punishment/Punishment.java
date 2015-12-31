package me.curlpipesh.control.punishment;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import me.curlpipesh.util.plugin.SkirtsPlugin;

/**
 * @author audrey
 * @since 8/23/15.
 */
@Data
@ToString
public class Punishment {
    private final SkirtsPlugin control;
    private final int id;
    private final String type;
    private final String issuer;
    private final String target;
    private final String reason;
    private final int length;
    private final long start;
    private final long end;

    public Punishment(@NonNull final SkirtsPlugin control, @NonNull final int id, @NonNull final String type, @NonNull final String issuer,
                      @NonNull final String target, @NonNull final String reason, @NonNull final int length, @NonNull final long start,
                      @NonNull final long end) {
        this.control = control;
        this.id = id;
        this.type = type;
        this.issuer = issuer;
        this.target = target;
        this.reason = reason;
        this.length = length;
        this.start = start;
        this.end = end;
    }

    public boolean over() {
        return end != Integer.MAX_VALUE && end <= System.currentTimeMillis();
        //!end.equalsIgnoreCase("forever") && Control.getFormat().parse(end).before(new Date());
    }
}
