package net.spacemc.control.db;

import lombok.NonNull;
import net.spacemc.control.punishment.Punishment;

import java.util.List;
import java.util.Optional;

/**
 * @author audrey
 * @since 8/28/15.
 */
@SuppressWarnings("unused")
public interface IPunishmentDB extends IDatabase {
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
}
