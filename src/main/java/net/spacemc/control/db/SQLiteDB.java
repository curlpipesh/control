package net.spacemc.control.db;

import lombok.Getter;
import lombok.NonNull;
import net.spacemc.control.SpaceControl;
import net.spacemc.control.punishment.Punishment;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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
        boolean init = execute("CREATE TABLE IF NOT EXISTS " + getDatabaseName()
                + "(id INT PRIMARY KEY NOT NULL, type TEXT NOT NULL, issuer TEXT NOT NULL, target TEXT NOT NULL, reason TEXT NOT NULL, "
                + "length INT NOT NULL, start TEXT NOT NULL, end TEXT NOT NULL)");
        commit();
        boolean lastPunishmentSet = false;
        try {
            Statement s = getConnection().createStatement();
            ResultSet set = s.getGeneratedKeys();
            if(set.last()) {
                lastPunishmentId = set.getInt("id");
                getControl().getLogger().info("[" + getDatabaseName() + "] Last id:" + lastPunishmentId);
                lastPunishmentSet = true;
            }
            set.close();
            s.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }

        return init && lastPunishmentSet;
    }

    @Override
    public List<Punishment> getPunishmentsForUUID(@NonNull UUID uuid) {
        List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            Statement s = getConnection().createStatement();
            s.execute(String.format("SELECT * FROM %s WHERE target = %s", getDatabaseName(), uuid.toString()));
            ResultSet resultSet = s.getResultSet();
            do {
                int id = resultSet.getInt("id");
                String type = resultSet.getString("type");
                String issuer = resultSet.getString("issuer");
                String target = resultSet.getString("target");
                String reason = resultSet.getString("reason");
                int lengthInMinutes = resultSet.getInt("length");
                String start = resultSet.getString("start");
                String end = resultSet.getString("end");
                punishments.add(new Punishment(getControl(), id, type, issuer, target, reason, lengthInMinutes, start, end));
            } while(resultSet.next());
            resultSet.close();
            s.close();
            commit();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public List<Punishment> getPunishmentsForIP(@NonNull String ip) {
        List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            Statement s = getConnection().createStatement();
            s.execute(String.format("SELECT * FROM %s WHERE target = %s", getDatabaseName(), ip));
            ResultSet resultSet = s.getResultSet();
            do {
                int id = resultSet.getInt("id");
                String type = resultSet.getString("type");
                String issuer = resultSet.getString("issuer");
                String target = resultSet.getString("target");
                String reason = resultSet.getString("reason");
                int lengthInMinutes = resultSet.getInt("length");
                String start = resultSet.getString("start");
                String end = resultSet.getString("end");
                punishments.add(new Punishment(getControl(), id, type, issuer, target, reason, lengthInMinutes, start, end));
            } while(resultSet.next());
            resultSet.close();
            s.close();
            commit();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public List<Punishment> getPunishmentsByUUID(@NonNull UUID uuid) {
        List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            Statement s = getConnection().createStatement();
            s.execute(String.format("SELECT * FROM %s WHERE issuer = %s", getDatabaseName(), uuid.toString()));
            ResultSet resultSet = s.getResultSet();
            do {
                int id = resultSet.getInt("id");
                String type = resultSet.getString("type");
                String issuer = resultSet.getString("issuer");
                String target = resultSet.getString("target");
                String reason = resultSet.getString("reason");
                int lengthInMinutes = resultSet.getInt("length");
                String start = resultSet.getString("start");
                String end = resultSet.getString("end");
                punishments.add(new Punishment(getControl(), id, type, issuer, target, reason, lengthInMinutes, start, end));
            } while(resultSet.next());
            resultSet.close();
            s.close();
            commit();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public boolean insertPunishment(@NonNull Punishment p) {
        lastPunishmentId = p.getId() - 1;
        return execute(String.format("INSERT INTO %s VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
                getDatabaseName(), ++lastPunishmentId, p.getType(), p.getIssuer(),
                p.getTarget(), p.getReason(), p.getLength(),
                p.getStart(), p.getEnd()));
    }

    @Override
    public boolean insertPunishment(@NonNull String type, @NonNull String issuer, @NonNull String target, @NonNull String reason, @NonNull int lengthInMinutes) {
        Date now = new Date();
        Date end = (Date) now.clone();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(end);
        calendar.add(Calendar.MINUTE, lengthInMinutes);
        end = calendar.getTime();
        return execute(String.format("INSERT INTO %s VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
                getDatabaseName(), ++lastPunishmentId, type, issuer, target, reason, lengthInMinutes, getControl().getFormat().format(now), getControl().getFormat().format(end)));
    }

    @Override
    public List<Punishment> getExpiredPunishments() {
        List<Punishment> punishments = new CopyOnWriteArrayList<>();
        try {
            Statement s = getConnection().createStatement();
            s.execute(String.format("SELECT * FROM %s", getDatabaseName()));
            ResultSet resultSet = s.getResultSet();
            do {
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
            } while(resultSet.next());
            resultSet.close();
            s.close();
            commit();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return punishments;
    }

    @Override
    public boolean removePunishment(@NonNull Punishment p) {
        return execute(String.format("DELETE FROM %s WHERE id = %s", getDatabaseName(), p.getId()));
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
    public List<Punishment> getPunishmentsByType(String... types) {
        List<Punishment> punishments = new CopyOnWriteArrayList<>();
        for(String t : types) {
            try {
                Statement s = getConnection().createStatement();
                s.execute(String.format("SELECT * FROM %s WHERE type = %s", getDatabaseName(), t));
                ResultSet resultSet = s.getResultSet();
                do {
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
                } while(resultSet.next());
                resultSet.close();
                s.close();
                commit();
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
        return punishments;
    }
}
