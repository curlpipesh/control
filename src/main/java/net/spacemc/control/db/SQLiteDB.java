package net.spacemc.control.db;

import lombok.Getter;
import lombok.NonNull;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.punishment.Punishment;

import java.sql.*;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author audrey
 * @since 8/23/15.
 */
public class SQLiteDB extends Database {
    @Getter
    private int lastPunishmentId = 0;

    public SQLiteDB(@NonNull SpaceControl control, @NonNull String dbName) {
        super(control, dbName);
    }

    @Override
    public boolean connect() {
        if(!getDatabaseFile().exists()) {
            getControl().getLogger().warning("SQLite DB \"" + getDatabaseName() + "\" doesn't exist, creating...");
        }
        if(doesDBDriverClassExist()) {
            try {
                setConnection(DriverManager.getConnection("jdbc:sqlite:" + getDatabaseFile().getPath()));
                setConnected(true);
                return true;
            } catch(SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean disconnect() {
        boolean state;
        try {
            getConnection().close();
            setConnected(false);
            state = true;
        } catch(SQLException e) {
            e.printStackTrace();
            state = false;
        }
        return state;
    }

    @Override
    @SuppressWarnings("SqlNoDataSourceInspection")
    public boolean initialize() {
        boolean created = false;
        try {
            Statement create = getConnection().createStatement();
            create.execute("CREATE TABLE IF NOT EXISTS " + getDatabaseName()
                    + "(id INT PRIMARY KEY NOT NULL UNIQUE, type TEXT NOT NULL, issuer TEXT NOT NULL, target TEXT NOT NULL, reason TEXT NOT NULL, "
                    + "length INT NOT NULL, start TEXT NOT NULL, end TEXT NOT NULL)");
            create.close();
            created = true;
        } catch(SQLException e) {
            e.printStackTrace();
        }

        try {
            Statement s = getConnection().createStatement();
            s.execute("SELECT * FROM " + getDatabaseName() + " ORDER BY id desc limit 1");
            ResultSet set = s.getResultSet();
            while(set.next()) {
                lastPunishmentId = set.getInt("id");
            }
            s.close();
        } catch(SQLException e) {
            lastPunishmentId = 0;
            if(!e.getMessage().contains("no such column")) {
                e.printStackTrace();
            }
        }
        getControl().getLogger().info("[" + getDatabaseName() + "] Last id: " + lastPunishmentId);

        return created;
    }

    @Override
    public List<Punishment> getPunishments(@NonNull String t) {
        List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            PreparedStatement s = getConnection().prepareStatement(String.format("SELECT * FROM %s WHERE target = ?", getDatabaseName()));
            s.setString(1, t);
            s.execute();
            ResultSet resultSet = s.getResultSet();
            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                String type = resultSet.getString("type");
                String issuer = resultSet.getString("issuer");
                String target = resultSet.getString("target");
                String reason = resultSet.getString("reason");
                int lengthInMinutes = resultSet.getInt("length");
                String start = resultSet.getString("start");
                String end = resultSet.getString("end");
                punishments.add(new Punishment(getControl(), id, type, issuer, target, reason, lengthInMinutes, start, end));
            }
            s.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public List<Punishment> getPunishmentsBy(@NonNull String i) {
        List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            PreparedStatement s = getConnection().prepareStatement(String.format("SELECT * FROM %s WHERE issuer = ?", getDatabaseName()));
            s.setString(1, i);
            s.execute();
            ResultSet resultSet = s.getResultSet();
            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                String type = resultSet.getString("type");
                String issuer = resultSet.getString("issuer");
                String target = resultSet.getString("target");
                String reason = resultSet.getString("reason");
                int lengthInMinutes = resultSet.getInt("length");
                String start = resultSet.getString("start");
                String end = resultSet.getString("end");
                punishments.add(new Punishment(getControl(), id, type, issuer, target, reason, lengthInMinutes, start, end));
            }
            s.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public Optional<Punishment> insertPunishment(@NonNull Punishment p) {
        lastPunishmentId = p.getId() - 1;
        try {
            PreparedStatement s = getConnection().prepareStatement(String.format("INSERT INTO %s VALUES (?, ?, ?, ?, ?, ?, ?, ?)", getDatabaseName()));
            s.setInt(1, ++lastPunishmentId);
            s.setString(2, p.getType());
            s.setString(3, p.getIssuer());
            s.setString(4, p.getTarget());
            s.setString(5, p.getReason());
            s.setInt(6, p.getLength());
            s.setString(7, p.getStart());
            s.setString(8, p.getEnd());
            execute(s);
            return Optional.of(p);
        } catch(SQLException e) {
            e.printStackTrace();
            return Optional.<Punishment>empty();
        }
    }

    @Override
    public Optional<Punishment> insertPunishment(@NonNull String type, @NonNull String issuer, @NonNull String target, @NonNull String reason, @NonNull int lengthInMinutes) {
        Date now = new Date();
        Date end = (Date) now.clone();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(end);
        calendar.add(Calendar.MINUTE, lengthInMinutes);
        end = calendar.getTime();

        try {
            PreparedStatement s = getConnection().prepareStatement(String.format("INSERT INTO %s VALUES (?, ?, ?, ?, ?, ?, ?, ?)", getDatabaseName()));
            s.setInt(1, ++lastPunishmentId);
            s.setString(2, type);
            s.setString(3, issuer);
            s.setString(4, target);
            s.setString(5, reason);
            s.setInt(6, lengthInMinutes);
            s.setString(7, getControl().getFormat().format(now));
            s.setString(8, getControl().getFormat().format(end));
            execute(s);
            return Optional.of(new Punishment(getControl(), lastPunishmentId, type, issuer, target, reason, lengthInMinutes,
                    getControl().getFormat().format(now), getControl().getFormat().format(end)));
        } catch(SQLException e) {
            e.printStackTrace();
            return Optional.<Punishment>empty();
        }
    }

    @Override
    public List<Punishment> getExpiredPunishments() {
        List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            PreparedStatement s = getConnection().prepareStatement(String.format("SELECT * FROM %s", getDatabaseName()));
            s.execute();
            ResultSet resultSet = s.getResultSet();
            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                String type = resultSet.getString("type");
                String issuer = resultSet.getString("issuer");
                String target = resultSet.getString("target");
                String reason = resultSet.getString("reason");
                int lengthInMinutes = resultSet.getInt("length");
                String start = resultSet.getString("start");
                String end = resultSet.getString("end");
                try {
                    if(getControl().getFormat().parse(end).before(new Date())) {
                        punishments.add(new Punishment(getControl(), id, type, issuer, target, reason, lengthInMinutes, start, end));
                    }
                } catch(ParseException e) {
                    e.printStackTrace();
                }
            }
            s.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public boolean removePunishment(@NonNull Punishment p) {
        try {
            PreparedStatement s = getConnection().prepareStatement(String.format("DELETE FROM %s WHERE id = ?", getDatabaseName()));
            s.setInt(1, p.getId());
            return execute(s);
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removePunishments(@NonNull Punishment... ps) {
        int failed = 0;
        for(@NonNull Punishment p : ps) {
            if(!removePunishment(p)) {
                ++failed;
            }
        }
        return failed == 0;
    }

    @Override
    public List<Punishment> getAllPunishments() {
        List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            PreparedStatement s = getConnection().prepareStatement(String.format("SELECT * FROM %s", getDatabaseName()));
            s.execute();
            ResultSet resultSet = s.getResultSet();
            while(resultSet.next()) {
                int id = resultSet.getInt("id");
                String type = resultSet.getString("type");
                String issuer = resultSet.getString("issuer");
                String target = resultSet.getString("target");
                String reason = resultSet.getString("reason");
                int lengthInMinutes = resultSet.getInt("length");
                String start = resultSet.getString("start");
                String end = resultSet.getString("end");
                punishments.add(new Punishment(getControl(), id, type, issuer, target, reason, lengthInMinutes, start, end));
            }
            s.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }
}
