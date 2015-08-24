package net.spacemc.control.db;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.punishment.Punishment;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

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

    protected final boolean execute(String s) {
        try {
            Statement statement = connection.createStatement();
            statement.execute(s);
            statement.close();
            commit();
            return true;
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public final void commit() {
        try {
            getConnection().commit();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public abstract boolean connect();

    public abstract boolean disconnect();

    public abstract boolean initialize();

    public abstract List<Punishment> getPunishmentsForUUID(@NonNull UUID uuid);

    public abstract List<Punishment> getPunishmentsForIP(@NonNull String ip);

    public abstract List<Punishment> getPunishmentsByUUID(@NonNull UUID uuid);

    public abstract boolean insertPunishment(@NonNull Punishment p);

    public abstract boolean insertPunishment(@NonNull String type, @NonNull String issuer, @NonNull String target, @NonNull String reason, @NonNull int lengthInMinutes);

    public abstract List<Punishment> getExpiredPunishments();

    public abstract boolean removePunishment(@NonNull Punishment p);

    public abstract boolean removePunishments(@NonNull Punishment... ps);

    public abstract List<Punishment> getPunishmentsByType(String... types);
}
