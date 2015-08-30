package me.curlpipesh.control.punishment;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import me.curlpipesh.control.Control;

import java.text.ParseException;
import java.util.Date;

/**
 * @author audrey
 * @since 8/23/15.
 */
@Data
@ToString
public class Punishment {
    private Control control;
    private int id;
    private String type;
    private String issuer;
    private String target;
    private String reason;
    private int length;
    private String start;
    private String end;

    public Punishment(@NonNull Control control, @NonNull int id, @NonNull String type, @NonNull String issuer,
                      @NonNull String target, @NonNull String reason, @NonNull int length, @NonNull String start,
                      @NonNull String end) {
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
        try {
            return !end.equalsIgnoreCase("forever") && control.getFormat().parse(end).before(new Date());
        } catch(ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
}
