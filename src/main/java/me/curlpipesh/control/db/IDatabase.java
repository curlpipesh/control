package me.curlpipesh.control.db;

import java.util.List;

/**
 * Kind of just a lot of an ugly hack to try to get a field of type
 * {@link IPunishmentDB} to give us access to methods in {@link Database}, but
 * at least this way of doing things might allow us to genericise it into a
 * library one day.
 *
 * @author audrey
 * @since 8/28/15.
 */
public interface IDatabase {
    boolean connect();

    boolean disconnect();

    boolean initialize();

    List<Runnable> getInitializationTasks();

    boolean addInitTask(Runnable task);

    boolean doesDriverExist();
}
