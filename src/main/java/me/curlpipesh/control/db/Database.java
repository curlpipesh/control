package me.curlpipesh.control.db;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.curlpipesh.control.Control;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author audrey
 * @since 8/23/15.
 */
public abstract class Database implements IDatabase {
    @Getter
    @Setter
    private String databaseName;

    @Getter
    @Setter
    private File databaseFile;

    @Getter
    private final Control control;

    @Getter
    @Setter
    private Connection connection;

    @Getter
    @Setter
    private boolean connected = false;

    @Getter
    private final List<Runnable> initializationTasks;

    public Database(@NonNull Control control, @NonNull String databaseName) {
        this.databaseName = databaseName;
        this.control = control;
        databaseFile = new File(control.getDataFolder() + File.separator + databaseName + ".db");
        initializationTasks = new CopyOnWriteArrayList<>();
    }

    public boolean addInitTask(Runnable task) {
        return initializationTasks.add(task);
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
}
