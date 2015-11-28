package me.curlpipesh.control.db;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.curlpipesh.control.Control;
import me.curlpipesh.control.punishment.Punishment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author audrey
 * @since 8/28/15.
 */
public class PunishmentDB extends SQLiteDB implements IPunishmentDB {
    @Getter
    @Setter
    private int lastPunishmentId = 0;

    public PunishmentDB(@NonNull Control control, @NonNull String dbName) {
        // TODO: Make id autoincrement at some point
        super(control, dbName, "CREATE TABLE IF NOT EXISTS " + dbName
                + "(id INT PRIMARY KEY NOT NULL UNIQUE, type TEXT NOT NULL, issuer TEXT NOT NULL, target TEXT NOT NULL, "
                + "reason TEXT NOT NULL, length INT NOT NULL, start TEXT NOT NULL, end TEXT NOT NULL)");
        addInitTask(() -> {
            try {
                PreparedStatement s = getConnection()
                        .prepareStatement(String.format("SELECT * FROM %s WHERE id = (SELECT MAX(id) FROM %s)", getDatabaseName(), getDatabaseName()));
                ResultSet r = s.executeQuery();
                r.next();
                PunishmentDB.this.lastPunishmentId = r.getInt("id") + 1;
                r.close();
            } catch(Exception e) {
                e.printStackTrace();
                System.out.println("################################");
                System.out.println("IF THIS IS NOT THE FIRST LAUNCH:");
                System.out.println("Could not load last ID.");
                System.out.println("Expect things to be broken.");
                System.out.println("################################");
                PunishmentDB.this.lastPunishmentId = 0;
            }
            System.out.println(String.format("%s: %s", getDatabaseName(), lastPunishmentId));
        });
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
            PreparedStatement s = getConnection()
                    .prepareStatement(String.format("INSERT INTO %s (id, type, issuer, target, reason, length, start, end) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", getDatabaseName()));
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

        String endFormatted = lengthInMinutes == Integer.MAX_VALUE ? "Forever" : getControl().getFormat().format(end);

        try {
            PreparedStatement s = getConnection()
                    .prepareStatement(String.format("INSERT INTO %s (id, type, issuer, target, reason, length, start, end) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", getDatabaseName()));
            s.setInt(1, ++lastPunishmentId);
            s.setString(2, type);
            s.setString(3, issuer);
            s.setString(4, target);
            s.setString(5, reason);
            s.setInt(6, lengthInMinutes);
            s.setString(7, getControl().getFormat().format(now));
            s.setString(8, endFormatted);
            execute(s);
            return Optional.of(new Punishment(getControl(), lastPunishmentId, type, issuer, target, reason, lengthInMinutes,
                    getControl().getFormat().format(now), endFormatted));
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
                if(end.equalsIgnoreCase("forever")) {
                    continue;
                }
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
