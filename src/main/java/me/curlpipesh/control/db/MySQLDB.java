package me.curlpipesh.control.db;

import lombok.Getter;
import lombok.NonNull;
import me.curlpipesh.control.Control;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author audrey
 * @since 12/14/15.
 */
@SuppressWarnings("Duplicates")
public class MySQLDB extends Database {
    @Getter
    private final String initializationStatement;

    public MySQLDB(@NonNull Control control, @NonNull String dbName, @NonNull String initializationStatement) {
        super(control, dbName);
        this.initializationStatement = initializationStatement;
    }

    @Override
    public boolean connect() {
        if(!getDatabaseFile().exists()) {
            getPlugin().getLogger().warning("MySQL DB \"" + getDatabaseName() + "\" doesn't exist, creating...");
        }
        if(doesDriverExist()) {
            try {
                setConnection(DriverManager.getConnection("jdbc:mysql:" + getDatabaseFile().getPath()));
                getConnection().setAutoCommit(true);
                setConnected(true);
                return true;
            } catch(SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            getPlugin().getLogger().warning("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
            getPlugin().getLogger().warning("MySQL DB driver doesn't exist! Do NOT expect any sort of functionality!!");
            getPlugin().getLogger().warning("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
            return false;
        }
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
            create.execute(initializationStatement);
            create.close();
            created = true;
        } catch(SQLException e) {
            e.printStackTrace();
        }

        getInitializationTasks().forEach(Runnable::run);

        return created;
    }

    @Override
    public boolean doesDriverExist() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return true;
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}
