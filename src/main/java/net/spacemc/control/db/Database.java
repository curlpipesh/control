package net.spacemc.control.db;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.punishment.Punishment;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * @author audrey
 * @since 8/23/15.
 */
public abstract class Database {
    @Getter
    @Setter
    private String databaseName;

    @Getter
    @Setter
    private File databaseFile;

    @Getter
    private final SpaceControl control;

    @Getter
    @Setter
    private Connection connection;

    @Getter
    @Setter
    private boolean connected = false;

    public Database(@NonNull SpaceControl control, @NonNull String databaseName) {
        this.databaseName = databaseName;
        this.control = control;
        databaseFile = new File(control.getDataFolder() + File.separator + databaseName + ".db");
    }

    protected final boolean doesDBDriverClassExist() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected final boolean execute(PreparedStatement s) {
        try {
            boolean state = s.execute();
            s.close();
            return state;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public abstract boolean connect();

    public abstract boolean disconnect();

    public abstract boolean initialize();

    public abstract List<Punishment> getPunishments(@NonNull String target);

    public abstract List<Punishment> getPunishmentsBy(@NonNull String issuer);

    public abstract Optional<Punishment> insertPunishment(@NonNull Punishment p);

    public abstract Optional<Punishment> insertPunishment(@NonNull String type, @NonNull String issuer, @NonNull String target, @NonNull String reason, @NonNull int lengthInMinutes);

    public abstract List<Punishment> getExpiredPunishments();

    public abstract boolean removePunishment(@NonNull Punishment p);

    public abstract boolean removePunishments(@NonNull Punishment... ps);

    public abstract List<Punishment> getAllPunishments();

    public abstract int getLastPunishmentId();

    public abstract void setLastPunishmentId(int i);
}
