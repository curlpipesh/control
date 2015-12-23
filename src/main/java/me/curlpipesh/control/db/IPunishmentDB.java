package me.curlpipesh.control.db;

import lombok.NonNull;
import me.curlpipesh.control.punishment.Punishment;
import me.curlpipesh.util.database.IDatabase;

import java.util.List;
import java.util.Optional;

/**
 * @author audrey
 * @since 8/28/15.
 */
@SuppressWarnings("unused")
public interface IPunishmentDB {
    List<Punishment> getPunishments(@NonNull String target);

    List<Punishment> getPunishmentsBy(@NonNull String issuer);

    Optional<Punishment> insertPunishment(@NonNull Punishment p);

    Optional<Punishment> insertPunishment(@NonNull String type, @NonNull String issuer, @NonNull String target, @NonNull String reason, @NonNull int lengthInMinutes);

    List<Punishment> getExpiredPunishments();

    boolean removePunishment(@NonNull Punishment p);

    boolean removePunishments(@NonNull Punishment... ps);

    List<Punishment> getAllPunishments();

    int getLastPunishmentId();

    void setLastPunishmentId(int i);

    IDatabase getDatabaseBackend();
}
