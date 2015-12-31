package me.curlpipesh.control.db;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.curlpipesh.control.Control;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.util.database.IDatabase;
import me.curlpipesh.util.database.impl.SQLiteDatabase;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author audrey
 * @since 8/28/15.
 */
@SuppressWarnings("Duplicates")
public class PunishmentDB implements IPunishmentDB {
    @Getter
    @Setter
    private int lastPunishmentId;

    @Getter
    private final IDatabase databaseBackend;

    public PunishmentDB(@NonNull final Control control, @NonNull final String dbName, @NonNull final DBMode mode) {
        switch(mode) {
            case SQLITE:
                databaseBackend = new SQLiteDatabase(control, dbName, "CREATE TABLE IF NOT EXISTS " + dbName
                        + "(id INT PRIMARY KEY NOT NULL UNIQUE, type TEXT NOT NULL, issuer TEXT NOT NULL, target TEXT NOT NULL, "
                        + "reason TEXT NOT NULL, length INT NOT NULL, start TEXT NOT NULL, end TEXT NOT NULL)");
                break;
            default:
                throw new IllegalArgumentException("Unknown DB mode!");
        }
        // TODO: Make id AUTOINCREMENT(?) at some point
        databaseBackend.addInitTask(() -> {
            try {
                final PreparedStatement s = databaseBackend.getConnection()
                        .prepareStatement(String.format("SELECT * FROM %s WHERE id = (SELECT MAX(id) FROM %s)", databaseBackend.getDatabaseName(), databaseBackend.getDatabaseName()));
                final ResultSet r = s.executeQuery();
                r.next();
                lastPunishmentId = r.getInt("id") + 1;
                r.close();
            } catch(final Exception e) {
                e.printStackTrace();
                // Pointless spacing, ik, but.
                System.out.println("#############################################################");
                System.out.println("IF THIS IS NOT THE FIRST LAUNCH OR NO (IN)ACTIVE PUNISHMENTS:");
                System.out.println("Could not load last ID.                                      ");
                System.out.println("Expect things to be broken.                                  ");
                System.out.println("#############################################################");
                lastPunishmentId = 0;
            }
            System.out.println(String.format("%s: %s", databaseBackend.getDatabaseName(), lastPunishmentId));
        });
    }

    @SuppressWarnings("unused")
    public PunishmentDB(@NonNull final Control control, @NonNull final String dbName) {
        this(control, dbName, DBMode.SQLITE);
    }

    @Override
    public List<Punishment> getPunishments(@NonNull final String t) {
        final List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            final PreparedStatement s = databaseBackend.getConnection().prepareStatement(String.format("SELECT * FROM %s WHERE target = ?", databaseBackend.getDatabaseName()));
            s.setString(1, t);
            s.execute();
            final ResultSet resultSet = s.getResultSet();
            while(resultSet.next()) {
                final int id = resultSet.getInt("id");
                final String type = resultSet.getString("type");
                final String issuer = resultSet.getString("issuer");
                final String target = resultSet.getString("target");
                final String reason = resultSet.getString("reason");
                final int lengthInMinutes = resultSet.getInt("length");
                final long start = resultSet.getLong("start");
                final long end = resultSet.getLong("end");
                punishments.add(new Punishment(databaseBackend.getPlugin(), id, type, issuer, target, reason, lengthInMinutes, start, end));
            }
            s.close();
        } catch(final SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public List<Punishment> getPunishmentsBy(@NonNull final String i) {
        final List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            final PreparedStatement s = databaseBackend.getConnection().prepareStatement(String.format("SELECT * FROM %s WHERE issuer = ?", databaseBackend.getDatabaseName()));
            s.setString(1, i);
            s.execute();
            final ResultSet resultSet = s.getResultSet();
            while(resultSet.next()) {
                final int id = resultSet.getInt("id");
                final String type = resultSet.getString("type");
                final String issuer = resultSet.getString("issuer");
                final String target = resultSet.getString("target");
                final String reason = resultSet.getString("reason");
                final int lengthInMinutes = resultSet.getInt("length");
                final long start = resultSet.getLong("start");
                final long end = resultSet.getLong("end");
                punishments.add(new Punishment(databaseBackend.getPlugin(), id, type, issuer, target, reason, lengthInMinutes, start, end));
            }
            s.close();
        } catch(final SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public Optional<Punishment> insertPunishment(@NonNull final Punishment p) {
        lastPunishmentId = p.getId() - 1;
        try {
            final PreparedStatement s = databaseBackend.getConnection()
                    .prepareStatement(String.format("INSERT INTO %s (id, type, issuer, target, reason, length, start, end) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", databaseBackend.getDatabaseName()));
            ++lastPunishmentId;
            s.setInt(1, lastPunishmentId);
            s.setString(2, p.getType());
            s.setString(3, p.getIssuer());
            s.setString(4, p.getTarget());
            s.setString(5, p.getReason());
            s.setInt(6, p.getLength());
            s.setLong(7, p.getStart());
            s.setLong(8, p.getEnd());
            databaseBackend.execute(s);
            return Optional.of(p);
        } catch(final SQLException e) {
            e.printStackTrace();
            return Optional.<Punishment>empty();
        }
    }

    @Override
    public Optional<Punishment> insertPunishment(@NonNull final String type, @NonNull final String issuer, @NonNull final String target, @NonNull final String reason, @NonNull final int lengthInMinutes) {
        final long now = System.currentTimeMillis();

        final long then = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(lengthInMinutes);

        final long endFormatted = lengthInMinutes == Integer.MAX_VALUE ? Integer.MAX_VALUE : then;

        try {
            final PreparedStatement s = databaseBackend.getConnection()
                    .prepareStatement(String.format("INSERT INTO %s (id, type, issuer, target, reason, length, start, end) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", databaseBackend.getDatabaseName()));
            ++lastPunishmentId;
            s.setInt(1, lastPunishmentId);
            s.setString(2, type);
            s.setString(3, issuer);
            s.setString(4, target);
            s.setString(5, reason);
            s.setInt(6, lengthInMinutes);
            s.setLong(7, now);
            s.setLong(8, endFormatted);
            databaseBackend.execute(s);
            return Optional.of(new Punishment(databaseBackend.getPlugin(), lastPunishmentId, type, issuer, target, reason, lengthInMinutes,
                    now, endFormatted));
        } catch(final SQLException e) {
            e.printStackTrace();
            return Optional.<Punishment>empty();
        }
    }

    @Override
    public List<Punishment> getExpiredPunishments() {
        final List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            final PreparedStatement s = databaseBackend.getConnection().prepareStatement(String.format("SELECT * FROM %s", databaseBackend.getDatabaseName()));
            s.execute();
            final ResultSet resultSet = s.getResultSet();
            while(resultSet.next()) {
                final int id = resultSet.getInt("id");
                final String type = resultSet.getString("type");
                final String issuer = resultSet.getString("issuer");
                final String target = resultSet.getString("target");
                final String reason = resultSet.getString("reason");
                final int lengthInMinutes = resultSet.getInt("length");
                final long start = resultSet.getLong("start");
                final long end = resultSet.getLong("end");
                if(end == Integer.MAX_VALUE) {
                    continue;
                }
                if(end <= System.currentTimeMillis()) {
                    punishments.add(new Punishment(databaseBackend.getPlugin(), id, type, issuer, target, reason, lengthInMinutes, start, end));
                }
            }
            s.close();
        } catch(final SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public boolean removePunishment(@NonNull final Punishment p) {
        try {
            final PreparedStatement s = databaseBackend.getConnection().prepareStatement(String.format("DELETE FROM %s WHERE id = ?", databaseBackend.getDatabaseName()));
            s.setInt(1, p.getId());
            return databaseBackend.execute(s);
        } catch(final SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removePunishments(@NonNull final Punishment... ps) {
        int failed = 0;
        for(@NonNull final Punishment p : ps) {
            if(!removePunishment(p)) {
                ++failed;
            }
        }
        return failed == 0;
    }

    @Override
    public List<Punishment> getAllPunishments() {
        final List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            final PreparedStatement s = databaseBackend.getConnection().prepareStatement(String.format("SELECT * FROM %s", databaseBackend.getDatabaseName()));
            s.execute();
            final ResultSet resultSet = s.getResultSet();
            while(resultSet.next()) {
                final int id = resultSet.getInt("id");
                final String type = resultSet.getString("type");
                final String issuer = resultSet.getString("issuer");
                final String target = resultSet.getString("target");
                final String reason = resultSet.getString("reason");
                final int lengthInMinutes = resultSet.getInt("length");
                final long start = resultSet.getLong("start");
                final long end = resultSet.getLong("end");
                punishments.add(new Punishment(databaseBackend.getPlugin(), id, type, issuer, target, reason, lengthInMinutes, start, end));
            }
            s.close();
        } catch(final SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    @SuppressWarnings("unused")
    public enum DBMode {
        SQLITE, MYSQL
    }
}
