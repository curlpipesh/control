package me.curlpipesh.control.db;

import lombok.NonNull;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.control.punishment.Punishment.PunishmentType;
import me.curlpipesh.util.database.IDatabase;

import java.util.List;
import java.util.Optional;

/**
 * Interface used to specify behaviour desired from a punishment database.
 *
 * @author audrey
 * @since 8/28/15.
 */
@SuppressWarnings("unused")
public interface IPunishmentDB {
    /**
     * Returns all punishments for the given target. The input string is used
     * just to <code>SELECT * FROM table WHERE target = ?</code>.
     *
     * @param target The target to search for
     * @return A {@link List} of all punishments for the given target
     */
    List<Punishment> getPunishments(@NonNull String target);

    /**
     * Returns all punishments for the given issuer. The input string is used
     * just to <code>SELECT * FROM table WHERE issuer = ?</code>.
     *
     * @param issuer The issuer to search for
     * @return A {@link List} of all punishments for the given issuer
     */
    List<Punishment> getPunishmentsBy(@NonNull String issuer);

    /**
     * Inserts the given punishment into the database, and returns an
     * {@link Optional} containing the punishment.
     * <p>
     * TODO: Why are we returning an Optional??
     *
     * @param p The punishment to insert
     * @return An Optional containing the inserted punishment
     */
    Optional<Punishment> insertPunishment(@NonNull Punishment p);

    /**
     * Inserts a punishment into the database with the given parameters.
     *
     * @param type The type of punishment
     * @param issuer The issuer of the punishment
     * @param target The target of the punishment
     * @param reason The reason for the punishment
     * @param lengthInMinutes Length of the punishment, in minutes
     * @return An Optional containing the inserted punishment
     */
    Optional<Punishment> insertPunishment(@NonNull PunishmentType type, @NonNull String issuer,
                                          @NonNull String target, @NonNull String reason, @NonNull int lengthInMinutes);

    /**
     * Returns all punishments from the database that have expired. Intended
     * only for use with the active punishments database.
     *
     * @return A list of expired punishments
     */
    List<Punishment> getExpiredPunishments();

    /**
     * Removes the given punishment from the database.
     *
     * @param p The punishment to remove
     * @return <code>true</code> if the punishment was removed,
     *         <code>false</code> otherwise.
     */
    boolean removePunishment(@NonNull Punishment p);

    /**
     * Removes the given punishments from the database.
     *
     * @param ps The punishments to remove
     * @return <code>true</code> if the punishments were removed,
     *         <code>false</code> otherwise.
     */
    boolean removePunishments(@NonNull Punishment... ps);

    /**
     * Returns all punishments currently stored in the database
     *
     * @return All punishments stored in the database
     */
    List<Punishment> getAllPunishments();

    /**
     * Returns the largest punishment id stored in the database. Punishments
     * are guaranteed to have a unique id.
     *
     * @return The largest punishment id stored in the database
     */
    int getLastPunishmentId();

    /**
     * Sets the last punishment id. Only should be used when the largest
     * inactive punishment id is larger than the largest active punishment id.
     *
     * @param i The id to set
     */
    void setLastPunishmentId(int i);

    /**
     * The database being used for the backend. The standard implementation
     * uses a SQLite database.
     *
     * @return The database backend being used.
     */
    IDatabase getDatabaseBackend();


    @SuppressWarnings("unused")
    enum DBMode {
        SQLITE, MYSQL
    }
}
